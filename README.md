# BTG Pactual Fund Management

API REST para gestión de fondos de inversión, suscripciones e historial de transacciones. Desarrollada con **Clean Architecture**, **Spring Boot 3.x** y **MongoDB**.

## Stack

- **Java 21**
- **Spring Boot 3.2** (Web, Data MongoDB, Validation, Security)
- **MongoDB** (DocumentDB compatible en AWS)
- **JWT** (Spring Security)
- **JUnit 5, Mockito, AssertJ**
- **Lombok, MapStruct**

## Estructura (Clean Architecture)

```
src/main/java/com/btgpactual/fund/
├── domain/           # Entidades, excepciones, puertos (repository, notification)
├── application/      # Casos de uso (SubscriptionService)
├── adapter/
│   ├── persistence/  # MongoDB (repositorios y adapters)
│   ├── notification/ # Email/SMS (Strategy)
│   └── web/          # REST, DTOs, Security (JWT)
└── config/           # Spring config, DataLoader (seed)
```

## Requisitos

- JDK 21
- Maven 3.9+
- MongoDB 6+ (local o Atlas)

## Configuración

Variables de entorno (o `application.yml`):

| Variable | Descripción | Por defecto |
|----------|-------------|-------------|
| `MONGODB_URI` | URI de MongoDB | `mongodb://localhost:27017/btg_funds` |
| `JWT_SECRET` | Secreto JWT (mín. 256 bits) | *(valor en yml)* |
| `PORT` | Puerto del servidor | `8080` |

## Ejecución local

```bash
# MongoDB en ejecución (local o Docker)
docker run -d -p 27017:27017 mongo:7

# Compilar y ejecutar
./mvnw spring-boot:run
```

La aplicación carga automáticamente los 5 fondos en la base de datos (DataLoader).

## API

### Autenticación

**Registro** (saldo inicial COP 500.000):

```http
POST /api/auth/register
Content-Type: application/json

{ "email": "user@example.com", "password": "password123" }
```

**Login:**

```http
POST /api/auth/login
Content-Type: application/json

{ "email": "user@example.com", "password": "password123" }
```

Respuesta: `{ "token": "eyJ...", "userId": "...", "role": "USER" }`

En las peticiones protegidas enviar: `Authorization: Bearer <token>`.

### Fondos y transacciones

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/funds/subscribe` | Suscribirse a un fondo (`body: { "fundCode": "FPV_BTG_PACTUAL_RECAUDADORA" }`) |
| DELETE | `/api/funds/unsubscribe/{id}` | Cancelar suscripción (id = subscriptionId) |
| GET | `/api/transactions/history?limit=50` | Historial de transacciones del usuario |

### Roles

- **USER:** subscribe, unsubscribe, history.
- **ADMIN:** mismo acceso (extensible a más operaciones).

## Tests

```bash
./mvnw test
```

Tests unitarios del servicio de suscripciones (subscribe, unsubscribe, validaciones). Cobertura objetivo ≥ 80%.

## Despliegue en AWS (Producción)

La aplicación está desplegada y disponible públicamente en AWS:

| Entorno | URL | Estado |
|---------|-----|--------|
| **Producción (AWS EC2)** | `http://34.201.44.132:8080` | ✅ Activo |
| **Local (Docker Compose)** | `http://localhost:8080` | desarrollo |

**Infraestructura utilizada:**
- **EC2 t2.micro** (Amazon Linux 2023) — capa gratuita de AWS
- **MongoDB Atlas M0** — capa gratuita, región us-east-1
- **Amazon ECR** — registro de imagen Docker

Health check en producción:
```bash
curl http://34.201.44.132:8080/actuator/health
# Respuesta: {"status":"UP"}
```

---

## Despliegue en AWS (CloudFormation)

1. **Crear imagen Docker** y subirla a ECR:

```bash
docker build -t btg-fund-management .
# Tag y push a tu ECR
```

2. **Desplegar stack** (DocumentDB + ECS Fargate):

```bash
aws cloudformation create-stack \
  --stack-name btg-fund-management \
  --template-body file://aws-stack.yaml \
  --parameters \
    ParameterKey=JwtSecret,ParameterValue=TU_SECRETO_JWT_256_BITS \
    ParameterKey=DocumentDBMasterPassword,ParameterValue=TU_PASSWORD_DOCDB \
  --capabilities CAPABILITY_NAMED_IAM
```

3. **Parámetros del stack:**

- `JwtSecret`: secreto para firmar JWT (mínimo 256 bits).
- `DocumentDBMasterPassword`: contraseña del usuario maestro de DocumentDB.
- `EnvironmentName`: dev | staging | prod.
- `AppName`: nombre de la aplicación (por defecto `btg-fund-management`).

El template crea:

- VPC, subnets públicas/privadas.
- **DocumentDB** (MongoDB compatible) en subnets privadas.
- **ECS Fargate**: cluster, task definition, servicio con la imagen de la API.
- CloudWatch Logs para la aplicación.

La variable `MONGODB_URI` se inyecta en el task con el endpoint de DocumentDB y TLS.
