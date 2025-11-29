# FREE LMS - Руководство по развертыванию

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
| CPU | 2 cores |
| RAM | 4 GB |
| Disk | 20 GB SSD |
| OS | Ubuntu 22.04+ / CentOS 8+ |

### Рекомендуемые требования (Production)

| Ресурс | Значение |
|--------|----------|
| CPU | 4+ cores |
| RAM | 8+ GB |
| Disk | 100 GB+ NVMe SSD |
| Network | 1 Gbps |

### Целевые метрики

| Метрика | Значение |
|---------|----------|
| Пользователи | 100,000 |
| Concurrent users | 1,000 |
| Организации | 200 |
| RPS | 1,000+ |
| Latency p95 | < 200ms |
| Uptime | 99.9% |

---

## Архитектура развертывания

### Модульный монолит

```
                    ┌─────────────────────────────────┐
                    │         Load Balancer           │
                    │      (Nginx / Cloud LB)         │
                    └───────────────┬─────────────────┘
                                    │
                    ┌───────────────▼─────────────────┐
                    │     FREE LMS Application        │
                    │         (x3 replicas)           │
                    │         Port 8080               │
                    └───────────────┬─────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
   ┌────▼────┐                 ┌────▼────┐                 ┌────▼────┐
   │PostgreSQL│                │  Redis  │                 │  Kafka  │
   │ Primary  │                │ Cluster │                 │ Cluster │
   │ + Replica│                │  (x3)   │                 │  (x3)   │
   └──────────┘                └─────────┘                 └─────────┘
```

### Компоненты

| Компонент | Описание | Порт |
|-----------|----------|------|
| Application | FREE LMS монолит | 8080 |
| PostgreSQL | Основная БД | 5432 |
| Redis | Кэш и сессии | 6379 |
| Kafka | Event streaming | 9092 |
| Elasticsearch | Поиск (опционально) | 9200 |
| MongoDB | Документы (опционально) | 27017 |
| MinIO | Файловое хранилище | 9000 |

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
cd free-lms

# Создание .env файла
cat > .env << 'EOF'
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=freelms
DB_USER=freelms_prod
DB_PASSWORD=<STRONG_PASSWORD_HERE>

# JWT (generate with: openssl rand -base64 32)
JWT_SECRET=<256_BIT_SECRET_HERE>
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# Kafka
KAFKA_SERVERS=kafka:9092

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# External Services (optional)
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...
EOF

chmod 600 .env
```

### 3. Запуск production

```bash
# Сборка и запуск
docker-compose -f docker-compose.monolith.yml up -d --build

# Проверка статуса
docker-compose -f docker-compose.monolith.yml ps

# Просмотр логов
docker-compose -f docker-compose.monolith.yml logs -f app
```

### 4. Healthcheck

```bash
# Проверка приложения
curl http://localhost:8080/actuator/health

# Ожидаемый ответ
{"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}
```

---

## Kubernetes развертывание

### 1. Подготовка кластера

```bash
# Создание namespace
kubectl create namespace freelms

# Создание secrets
kubectl create secret generic freelms-secrets \
  --namespace=freelms \
  --from-literal=DB_USER=freelms_prod \
  --from-literal=DB_PASSWORD=<PASSWORD> \
  --from-literal=JWT_SECRET=<256_BIT_SECRET>

# ConfigMap
kubectl apply -f - <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: freelms-config
  namespace: freelms
data:
  SPRING_PROFILES_ACTIVE: "prod"
  SERVER_PORT: "8080"
  DB_HOST: "postgres-service"
  REDIS_HOST: "redis-service"
  KAFKA_SERVERS: "kafka-service:9092"
EOF
```

### 2. Развертывание приложения

```yaml
# app-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: freelms-app
  namespace: freelms
spec:
  replicas: 3
  selector:
    matchLabels:
      app: freelms-app
  template:
    metadata:
      labels:
        app: freelms-app
    spec:
      containers:
      - name: app
        image: freelms/app:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: freelms-config
        - secretRef:
            name: freelms-secrets
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

```bash
# Применение
kubectl apply -f app-deployment.yaml
kubectl apply -f app-service.yaml

# Проверка
kubectl get pods -n freelms
kubectl logs -n freelms -l app=freelms-app -f
```

### 3. Настройка Ingress

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
        - api.smartup24.com
      secretName: freelms-tls
  rules:
    - host: api.smartup24.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: freelms-service
                port:
                  number: 8080
```

---

## Настройка окружения

### Production профиль

```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

logging:
  level:
    root: WARN
    com.freelms: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus

server:
  error:
    include-stacktrace: never
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

# ServiceMonitor для FREE LMS
kubectl apply -f - <<EOF
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: freelms-monitor
  namespace: monitoring
spec:
  selector:
    matchLabels:
      app: freelms-app
  endpoints:
    - port: http
      path: /actuator/prometheus
EOF
```

### Ключевые метрики

| Метрика | Alert threshold |
|---------|-----------------|
| Response time p95 | > 200ms |
| Error rate | > 1% |
| CPU usage | > 80% |
| Memory usage | > 85% |
| Database connections | > 90% pool |

### Health Endpoints

```bash
# Liveness (для перезапуска)
curl http://localhost:8080/actuator/health/liveness

# Readiness (для трафика)
curl http://localhost:8080/actuator/health/readiness

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

---

## Безопасность

### Checklist перед production

- [ ] Изменены все пароли по умолчанию
- [ ] JWT secret - 256 bit, криптографически случайный
- [ ] TLS/HTTPS настроен
- [ ] CORS ограничен доверенными доменами
- [ ] Actuator endpoints защищены
- [ ] Database credentials в secrets
- [ ] Network policies настроены
- [ ] Image scanning выполнен

### Генерация безопасного JWT secret

```bash
openssl rand -base64 32
```

### Network Policy (Kubernetes)

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: freelms-network-policy
  namespace: freelms
spec:
  podSelector:
    matchLabels:
      app: freelms-app
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: postgres
    - podSelector:
        matchLabels:
          app: redis
    - podSelector:
        matchLabels:
          app: kafka
```

---

## Масштабирование

### Горизонтальное масштабирование

```yaml
# HPA для автомасштабирования
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: freelms-hpa
  namespace: freelms
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: freelms-app
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Рекомендации по масштабированию

| Компонент | Стратегия |
|-----------|-----------|
| Application | Горизонтальное (replicas) |
| PostgreSQL | Primary + Read Replicas |
| Redis | Cluster mode |
| Kafka | 3+ brokers |

### Connection pooling

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

### Приложение не запускается

```bash
# Проверка логов
docker-compose -f docker-compose.monolith.yml logs app

# Kubernetes
kubectl logs -n freelms -l app=freelms-app --previous
kubectl describe pod -n freelms <pod-name>
```

### Проблемы с базой данных

```bash
# Проверка подключения
docker exec -it postgres psql -U freelms -d freelms -c "SELECT 1"

# Проверка connections
docker exec -it postgres psql -U freelms -d freelms -c "SELECT count(*) FROM pg_stat_activity"
```

### High latency

1. Проверьте метрики CPU/Memory
2. Проверьте connection pool exhaustion
3. Проверьте slow queries в PostgreSQL
4. Проверьте Redis cache hit ratio

---

## Контакты поддержки

- DevOps: devops@smartup24.com
- Website: www.smartup24.com
- Email: opensource@smartup24.com
