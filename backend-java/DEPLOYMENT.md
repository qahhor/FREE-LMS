# Smartup LMS - Руководство по развертыванию

## Содержание

1. [Требования](#требования)
2. [Архитектура развертывания](#архитектура-развертывания)
3. [Docker развертывание](#docker-развертывание)
4. [Kubernetes развертывание](#kubernetes-развертывание)
5. [Настройка окружения](#настройка-окружения)
6. [Мониторинг](#мониторинг)
7. [Безопасность](#безопасность)
8. [Масштабирование](#масштабирование)

---

## Требования

### Минимальные требования (Development)

| Ресурс | Значение |
|--------|----------|
| CPU | 4 cores |
| RAM | 16 GB |
| Disk | 50 GB SSD |
| OS | Ubuntu 22.04+ / CentOS 8+ |

### Рекомендуемые требования (Production)

| Ресурс | Значение |
|--------|----------|
| CPU | 16+ cores |
| RAM | 64+ GB |
| Disk | 500 GB+ NVMe SSD |
| Network | 1 Gbps |

### Целевые метрики

| Метрика | Значение |
|---------|----------|
| Пользователи | 100,000 |
| Concurrent users | 1,000 |
| Организации | 200 |
| RPS | 1,000+ |
| Latency p95 | < 500ms |
| Uptime | 99.9% |

---

## Архитектура развертывания

```
                    ┌─────────────────────────────────┐
                    │         Load Balancer           │
                    │      (Nginx / Cloud LB)         │
                    └───────────────┬─────────────────┘
                                    │
                    ┌───────────────▼─────────────────┐
                    │       API Gateway (x3)          │
                    │         + Rate Limiting         │
                    └───────────────┬─────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
   ┌────▼────┐                 ┌────▼────┐                 ┌────▼────┐
   │  Auth   │                 │ Course  │                 │ + 17    │
   │  (x2)   │                 │  (x3)   │                 │services │
   └────┬────┘                 └────┬────┘                 └────┬────┘
        │                           │                           │
        └───────────────────────────┼───────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
   ┌────▼────┐                 ┌────▼────┐                 ┌────▼────┐
   │PostgreSQL│                │  Redis  │                 │  Kafka  │
   │ Primary  │                │ Cluster │                 │ Cluster │
   │ + Replica│                │  (x3)   │                 │  (x3)   │
   └──────────┘                └─────────┘                 └─────────┘
```

---

## Docker развертывание

### 1. Подготовка сервера

```bash
# Обновление системы
sudo apt update && sudo apt upgrade -y

# Установка Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Установка Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 2. Настройка environment

```bash
cd backend-java

# Создание .env файла
cat > .env << 'EOF'
# Database
DB_USER=freelms_prod
DB_PASSWORD=<STRONG_PASSWORD_HERE>

# JWT (generate with: openssl rand -base64 32)
JWT_SECRET=<256_BIT_SECRET_HERE>

# Service Discovery
EUREKA_USER=eureka_admin
EUREKA_PASSWORD=<STRONG_PASSWORD_HERE>

# Config Server
CONFIG_USER=config_admin
CONFIG_PASSWORD=<STRONG_PASSWORD_HERE>

# Redis
REDIS_PASSWORD=<STRONG_PASSWORD_HERE>

# External Services
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
EOF

chmod 600 .env
```

### 3. Запуск production

```bash
# Сборка образов
docker-compose -f docker-compose.prod.yml build

# Запуск
docker-compose -f docker-compose.prod.yml up -d

# Проверка статуса
docker-compose -f docker-compose.prod.yml ps

# Просмотр логов
docker-compose -f docker-compose.prod.yml logs -f gateway-service
```

### 4. Healthcheck

```bash
# Проверка всех сервисов
for port in 8761 8888 8000 8081 8082 8083; do
  echo "Checking port $port..."
  curl -s http://localhost:$port/actuator/health | jq .status
done
```

---

## Kubernetes развертывание

### 1. Подготовка кластера

```bash
# Создание namespace
kubectl apply -f k8s/namespace.yaml

# Создание secrets (ВАЖНО: замените значения!)
kubectl create secret generic freelms-secrets \
  --namespace=freelms \
  --from-literal=DB_USER=freelms_prod \
  --from-literal=DB_PASSWORD=<PASSWORD> \
  --from-literal=JWT_SECRET=<256_BIT_SECRET> \
  --from-literal=REDIS_PASSWORD=<PASSWORD>

# Применение ConfigMap
kubectl apply -f k8s/configmap.yaml
```

### 2. Развертывание инфраструктуры

```bash
# PostgreSQL
kubectl apply -f k8s/postgres-statefulset.yaml

# Redis (используйте Helm для production)
helm install redis bitnami/redis \
  --namespace=freelms \
  --set auth.password=<PASSWORD> \
  --set replica.replicaCount=3

# Kafka (используйте Helm для production)
helm install kafka bitnami/kafka \
  --namespace=freelms \
  --set replicaCount=3
```

### 3. Развертывание сервисов

```bash
# Применение всех deployments
kubectl apply -f k8s/

# Проверка статуса
kubectl get pods -n freelms
kubectl get services -n freelms
kubectl get hpa -n freelms
```

### 4. Настройка Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: freelms-ingress
  namespace: freelms
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
    - hosts:
        - api.smartup.uz
      secretName: freelms-tls
  rules:
    - host: api.smartup.uz
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: gateway-service
                port:
                  number: 80
```

---

## Настройка окружения

### Production профиль

Каждый сервис должен запускаться с:

```bash
SPRING_PROFILES_ACTIVE=production
```

### Ключевые настройки

| Параметр | Development | Production |
|----------|-------------|------------|
| `logging.level.root` | DEBUG | WARN |
| `spring.jpa.show-sql` | true | false |
| `management.endpoints.web.exposure.include` | * | health,metrics |
| `server.error.include-stacktrace` | always | never |

---

## Мониторинг

### Prometheus + Grafana

```bash
# Установка через Helm
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace=monitoring \
  --create-namespace

# Добавление ServiceMonitor для Smartup LMS
kubectl apply -f - <<EOF
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: freelms-monitor
  namespace: monitoring
spec:
  selector:
    matchLabels:
      app.kubernetes.io/part-of: freelms
  endpoints:
    - port: http
      path: /actuator/prometheus
EOF
```

### Ключевые метрики

| Метрика | Alert threshold |
|---------|-----------------|
| Response time p95 | > 500ms |
| Error rate | > 1% |
| CPU usage | > 80% |
| Memory usage | > 85% |
| Database connections | > 90% pool |

---

## Безопасность

### Checklist перед production

- [ ] Изменены все пароли по умолчанию
- [ ] JWT secret - 256 bit, криптографически случайный
- [ ] TLS/HTTPS настроен
- [ ] Rate limiting включён
- [ ] CORS ограничен доверенными доменами
- [ ] Actuator endpoints защищены
- [ ] Database credentials в secrets
- [ ] Network policies настроены
- [ ] Pod Security Policies включены
- [ ] Image scanning выполнен

### Генерация безопасного JWT secret

```bash
openssl rand -base64 32
```

---

## Масштабирование

### Горизонтальное масштабирование

HPA настроен для автоматического масштабирования:

| Сервис | Min | Max | CPU target |
|--------|-----|-----|------------|
| gateway | 3 | 10 | 70% |
| auth | 2 | 5 | 70% |
| course | 3 | 8 | 70% |
| enrollment | 2 | 6 | 70% |
| gamification | 2 | 5 | 70% |

### Масштабирование БД

Для 100,000 пользователей рекомендуется:

- **PostgreSQL**: Primary + 2 Read Replicas
- **Redis**: 3-node cluster
- **Kafka**: 3 brokers

### Connection pooling

Рекомендуемые настройки HikariCP:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
```

---

## Troubleshooting

### Сервис не запускается

```bash
# Проверка логов
kubectl logs -n freelms <pod-name> --previous

# Проверка events
kubectl describe pod -n freelms <pod-name>

# Проверка ресурсов
kubectl top pods -n freelms
```

### Проблемы с базой данных

```bash
# Проверка подключения
kubectl exec -it -n freelms postgres-auth-0 -- psql -U freelms_prod -d freelms_auth -c "SELECT 1"

# Проверка connections
kubectl exec -it -n freelms postgres-auth-0 -- psql -U freelms_prod -d freelms_auth -c "SELECT count(*) FROM pg_stat_activity"
```

### High latency

1. Проверьте метрики CPU/Memory
2. Проверьте connection pool exhaustion
3. Проверьте slow queries в PostgreSQL
4. Проверьте Redis cache hit ratio

---

## Контакты поддержки

- DevOps: devops@smartup.uz
- On-call: +7-XXX-XXX-XXXX
- Slack: #freelms-ops
