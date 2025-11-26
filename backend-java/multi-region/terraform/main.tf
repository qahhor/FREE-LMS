# ============================================================================
# FREE LMS - Multi-Region Infrastructure
# Terraform configuration for multi-region deployment
# ============================================================================

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
  }

  backend "s3" {
    bucket         = "freelms-terraform-state"
    key            = "multi-region/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "freelms-terraform-locks"
  }
}

# ============================================================================
# Variables
# ============================================================================

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "regions" {
  description = "AWS regions for deployment"
  type = map(object({
    role     = string
    cidr     = string
    azs      = list(string)
  }))
  default = {
    "us-east-1" = {
      role = "primary"
      cidr = "10.0.0.0/16"
      azs  = ["us-east-1a", "us-east-1b", "us-east-1c"]
    }
    "eu-west-1" = {
      role = "secondary"
      cidr = "10.1.0.0/16"
      azs  = ["eu-west-1a", "eu-west-1b", "eu-west-1c"]
    }
    "ap-south-1" = {
      role = "secondary"
      cidr = "10.2.0.0/16"
      azs  = ["ap-south-1a", "ap-south-1b", "ap-south-1c"]
    }
  }
}

# ============================================================================
# Provider Configuration
# ============================================================================

provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"
}

provider "aws" {
  alias  = "eu_west_1"
  region = "eu-west-1"
}

provider "aws" {
  alias  = "ap_south_1"
  region = "ap-south-1"
}

# ============================================================================
# VPC Module for Each Region
# ============================================================================

module "vpc_us_east" {
  source = "./modules/vpc"
  providers = {
    aws = aws.us_east_1
  }

  name        = "freelms-${var.environment}-us-east"
  cidr        = var.regions["us-east-1"].cidr
  azs         = var.regions["us-east-1"].azs
  environment = var.environment
  region_role = "primary"
}

module "vpc_eu_west" {
  source = "./modules/vpc"
  providers = {
    aws = aws.eu_west_1
  }

  name        = "freelms-${var.environment}-eu-west"
  cidr        = var.regions["eu-west-1"].cidr
  azs         = var.regions["eu-west-1"].azs
  environment = var.environment
  region_role = "secondary"
}

module "vpc_ap_south" {
  source = "./modules/vpc"
  providers = {
    aws = aws.ap_south_1
  }

  name        = "freelms-${var.environment}-ap-south"
  cidr        = var.regions["ap-south-1"].cidr
  azs         = var.regions["ap-south-1"].azs
  environment = var.environment
  region_role = "secondary"
}

# ============================================================================
# VPC Peering Between Regions
# ============================================================================

resource "aws_vpc_peering_connection" "us_to_eu" {
  provider    = aws.us_east_1
  vpc_id      = module.vpc_us_east.vpc_id
  peer_vpc_id = module.vpc_eu_west.vpc_id
  peer_region = "eu-west-1"
  auto_accept = false

  tags = {
    Name = "freelms-us-east-to-eu-west"
  }
}

resource "aws_vpc_peering_connection_accepter" "eu_accept" {
  provider                  = aws.eu_west_1
  vpc_peering_connection_id = aws_vpc_peering_connection.us_to_eu.id
  auto_accept               = true
}

# ============================================================================
# EKS Clusters
# ============================================================================

module "eks_us_east" {
  source = "./modules/eks"
  providers = {
    aws = aws.us_east_1
  }

  cluster_name    = "freelms-${var.environment}-us-east"
  vpc_id          = module.vpc_us_east.vpc_id
  subnet_ids      = module.vpc_us_east.private_subnet_ids
  environment     = var.environment
  node_count      = 5
  node_type       = "m6i.xlarge"
}

module "eks_eu_west" {
  source = "./modules/eks"
  providers = {
    aws = aws.eu_west_1
  }

  cluster_name    = "freelms-${var.environment}-eu-west"
  vpc_id          = module.vpc_eu_west.vpc_id
  subnet_ids      = module.vpc_eu_west.private_subnet_ids
  environment     = var.environment
  node_count      = 3
  node_type       = "m6i.xlarge"
}

# ============================================================================
# RDS PostgreSQL with Read Replicas
# ============================================================================

module "rds_primary" {
  source = "./modules/rds"
  providers = {
    aws = aws.us_east_1
  }

  identifier          = "freelms-${var.environment}-primary"
  vpc_id              = module.vpc_us_east.vpc_id
  subnet_ids          = module.vpc_us_east.private_subnet_ids
  instance_class      = "db.r6g.xlarge"
  allocated_storage   = 100
  multi_az            = true
  backup_retention    = 30
  environment         = var.environment
}

module "rds_replica_eu" {
  source = "./modules/rds-replica"
  providers = {
    aws = aws.eu_west_1
  }

  identifier          = "freelms-${var.environment}-replica-eu"
  source_db_arn       = module.rds_primary.db_instance_arn
  vpc_id              = module.vpc_eu_west.vpc_id
  subnet_ids          = module.vpc_eu_west.private_subnet_ids
  instance_class      = "db.r6g.large"
  environment         = var.environment
}

# ============================================================================
# ElastiCache Redis Global Datastore
# ============================================================================

resource "aws_elasticache_global_replication_group" "freelms" {
  provider                           = aws.us_east_1
  global_replication_group_id_suffix = "freelms-${var.environment}"
  primary_replication_group_id       = module.redis_us_east.replication_group_id
}

module "redis_us_east" {
  source = "./modules/redis"
  providers = {
    aws = aws.us_east_1
  }

  cluster_id         = "freelms-${var.environment}-us-east"
  vpc_id             = module.vpc_us_east.vpc_id
  subnet_ids         = module.vpc_us_east.private_subnet_ids
  node_type          = "cache.r6g.large"
  num_cache_clusters = 3
  environment        = var.environment
}

# ============================================================================
# Route53 Health Checks and DNS Failover
# ============================================================================

resource "aws_route53_health_check" "us_east" {
  fqdn              = "api-us-east.freelms.com"
  port              = 443
  type              = "HTTPS"
  resource_path     = "/actuator/health"
  failure_threshold = 3
  request_interval  = 10

  tags = {
    Name = "freelms-us-east-health"
  }
}

resource "aws_route53_health_check" "eu_west" {
  fqdn              = "api-eu-west.freelms.com"
  port              = 443
  type              = "HTTPS"
  resource_path     = "/actuator/health"
  failure_threshold = 3
  request_interval  = 10

  tags = {
    Name = "freelms-eu-west-health"
  }
}

resource "aws_route53_record" "api_primary" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "api.freelms.com"
  type    = "A"

  alias {
    name                   = module.alb_us_east.dns_name
    zone_id                = module.alb_us_east.zone_id
    evaluate_target_health = true
  }

  failover_routing_policy {
    type = "PRIMARY"
  }

  set_identifier  = "primary"
  health_check_id = aws_route53_health_check.us_east.id
}

resource "aws_route53_record" "api_secondary" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "api.freelms.com"
  type    = "A"

  alias {
    name                   = module.alb_eu_west.dns_name
    zone_id                = module.alb_eu_west.zone_id
    evaluate_target_health = true
  }

  failover_routing_policy {
    type = "SECONDARY"
  }

  set_identifier  = "secondary"
  health_check_id = aws_route53_health_check.eu_west.id
}

# ============================================================================
# S3 Cross-Region Replication
# ============================================================================

resource "aws_s3_bucket" "uploads_primary" {
  provider = aws.us_east_1
  bucket   = "freelms-${var.environment}-uploads-us-east"

  versioning {
    enabled = true
  }
}

resource "aws_s3_bucket" "uploads_replica" {
  provider = aws.eu_west_1
  bucket   = "freelms-${var.environment}-uploads-eu-west"

  versioning {
    enabled = true
  }
}

resource "aws_s3_bucket_replication_configuration" "uploads" {
  provider = aws.us_east_1
  bucket   = aws_s3_bucket.uploads_primary.id
  role     = aws_iam_role.replication.arn

  rule {
    id     = "replicate-all"
    status = "Enabled"

    destination {
      bucket        = aws_s3_bucket.uploads_replica.arn
      storage_class = "STANDARD"
    }
  }
}

# ============================================================================
# Outputs
# ============================================================================

output "eks_cluster_endpoints" {
  value = {
    us_east = module.eks_us_east.cluster_endpoint
    eu_west = module.eks_eu_west.cluster_endpoint
  }
}

output "rds_endpoints" {
  value = {
    primary     = module.rds_primary.endpoint
    replica_eu  = module.rds_replica_eu.endpoint
  }
  sensitive = true
}
