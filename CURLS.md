# BTG Fund Management — Colección de cURLs para Pruebas

> **Base URL Local:** `http://localhost:8080`  
> **Base URL Producción (AWS EC2):** `http://34.201.44.132:8080`  
> **Autenticación:** JWT Bearer Token (header `Authorization: Bearer <TOKEN>`)  
> **Content-Type:** `application/json`

> ⚡ La aplicación está desplegada en **AWS EC2 (us-east-1)** con **MongoDB Atlas M0**. Puedes reemplazar `localhost:8080` por `34.201.44.132:8080` en cualquier comando para probar el entorno de producción.

---

## Tabla de Contenido

1. [Pre-requisitos](#1-pre-requisitos)
2. [Health Check](#2-health-check)
3. [Autenticación](#3-autenticación)
   - 3.1 Registro de usuario
   - 3.2 Login con usuario existente
   - 3.3 Login como administrador
4. [Fondos](#4-fondos)
   - 4.1 Listar todos los fondos
   - 4.2 Suscribirse a un fondo
   - 4.3 Cancelar suscripción
5. [Transacciones](#5-transacciones)
   - 5.1 Historial de transacciones
6. [Casos de Error (Validaciones)](#6-casos-de-error-validaciones)
7. [Flujo Completo de Prueba](#7-flujo-completo-de-prueba)
8. [Comandos PowerShell (Automatizados)](#8-comandos-powershell-automatizados)

---

## 1. Pre-requisitos

Elige el entorno donde quieres probar:

**Opción A — Producción en AWS (sin instalación local):**
```bash
# La app ya está corriendo. Solo reemplaza localhost por la IP de AWS:
# http://34.201.44.132:8080
curl -s http://34.201.44.132:8080/actuator/health
```

**Opción B — Local con Docker Compose:**
```bash
# Levantar MongoDB y la aplicación con Docker Compose
docker-compose up -d
```

**Opción C — Local con Maven (requiere MongoDB local en puerto 27017):**
```bash
./mvnw spring-boot:run
```

**Datos precargados al iniciar (DataLoader):**

| Email | Contraseña | Rol |
|-------|------------|-----|
| `carlos.martinez@example.com` | `password123` | USER |
| `maria.lopez@example.com` | `password123` | USER |
| `andres.garcia@example.com` | `password123` | USER |
| `laura.rodriguez@example.com` | `password123` | USER |
| `admin@btgpactual.com` | `password123` | ADMIN |

**Fondos disponibles:**

| Código | Nombre | Monto Mínimo (COP) | Categoría |
|--------|--------|---------------------|-----------|
| `FPV_BTG_PACTUAL_RECAUDADORA` | FPV BTG Pactual Recaudadora | $75.000 | FPV |
| `FPV_BTG_PACTUAL_ECOPETROL` | FPV BTG Pactual Ecopetrol | $125.000 | FPV |
| `DEUDAPRIVADA` | Deuda Privada | $50.000 | FIC |
| `FDO-ACCIONES` | Fondo Acciones | $250.000 | FIC |
| `FPV_BTG_PACTUAL_DINAMICA` | FPV BTG Pactual Dinámica | $100.000 | FPV |

> **Nota:** Cada usuario nuevo inicia con un saldo de **$500.000 COP**.

---

## 2. Health Check

Verifica que la aplicación esté corriendo correctamente.

```bash
curl -s http://localhost:8080/actuator/health
```

**Respuesta esperada (200 OK):**
```json
{
  "status": "UP"
}
```

---

## 3. Autenticación

Los endpoints de autenticación (`/api/auth/**`) son **públicos** y no requieren token.

### 3.1 Registro de un nuevo usuario

Crea un nuevo usuario en el sistema. Retorna un token JWT para uso inmediato.

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo.usuario@example.com",
    "password": "miPassword123"
  }'
```

**Respuesta esperada (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "USER"
}
```

### 3.2 Login con usuario existente (rol USER)

Inicia sesión con uno de los usuarios precargados.

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "carlos.martinez@example.com",
    "password": "password123"
  }'
```

**Respuesta esperada (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "un-uuid-generado",
  "role": "USER"
}
```

> **Importante:** Copiar el valor de `token` de la respuesta y usarlo en los siguientes requests reemplazando `<TOKEN>`.

### 3.3 Login como administrador

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@btgpactual.com",
    "password": "password123"
  }'
```

**Respuesta esperada (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "un-uuid-generado",
  "role": "ADMIN"
}
```

---

## 4. Fondos

> **Todos los endpoints de esta sección requieren autenticación.**  
> Reemplazar `<TOKEN>` con el token JWT obtenido en el paso de login.

### 4.1 Listar todos los fondos disponibles

Obtiene la lista completa de fondos con su monto mínimo de inversión y categoría.

```bash
curl -s http://localhost:8080/api/funds \
  -H "Authorization: Bearer <TOKEN>"
```

### 4.2 Listar suscripciones del usuario autenticado

Retorna todas las suscripciones del usuario (activas y canceladas).

```bash
curl -s http://localhost:8080/api/funds/subscriptions \
  -H "Authorization: Bearer <TOKEN>"
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": "uuid-de-la-suscripcion",
    "fundCode": "DEUDAPRIVADA",
    "fundName": "Deuda Privada",
    "status": "ACTIVE",
    "subscribedAt": "2026-03-12T10:30:00Z"
  },
  {
    "id": "otro-uuid",
    "fundCode": "FPV_BTG_PACTUAL_RECAUDADORA",
    "fundName": "FPV BTG Pactual Recaudadora",
    "status": "CANCELLED",
    "subscribedAt": "2026-03-12T09:00:00Z"
  }
]
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": "uuid",
    "code": "FPV_BTG_PACTUAL_RECAUDADORA",
    "name": "FPV BTG Pactual Recaudadora",
    "minAmount": 75000,
    "category": "FPV"
  },
  {
    "id": "uuid",
    "code": "DEUDAPRIVADA",
    "name": "Deuda Privada",
    "minAmount": 50000,
    "category": "FIC"
  }
]
```

### 4.3 Suscribirse a un fondo

Vincula al usuario autenticado a un fondo. El monto mínimo se descuenta del saldo del usuario.

```bash
# Suscripción al fondo "Deuda Privada" (monto mínimo: $50.000 COP)
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "fundCode": "DEUDAPRIVADA"
  }'
```

**Respuesta esperada (201 Created):**
```json
{
  "id": "uuid-de-la-suscripcion",
  "fundCode": "DEUDAPRIVADA",
  "fundName": "Deuda Privada",
  "status": "ACTIVE",
  "subscribedAt": "2026-03-12T10:30:00"
}
```

```bash
# Suscripción al fondo "FPV BTG Pactual Recaudadora" (monto mínimo: $75.000 COP)
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "fundCode": "FPV_BTG_PACTUAL_RECAUDADORA"
  }'
```

```bash
# Suscripción al fondo "FPV BTG Pactual Ecopetrol" (monto mínimo: $125.000 COP)
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "fundCode": "FPV_BTG_PACTUAL_ECOPETROL"
  }'
```

```bash
# Suscripción al fondo "FPV BTG Pactual Dinámica" (monto mínimo: $100.000 COP)
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "fundCode": "FPV_BTG_PACTUAL_DINAMICA"
  }'
```

```bash
# Suscripción al fondo "Fondo Acciones" (monto mínimo: $250.000 COP)
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "fundCode": "FDO-ACCIONES"
  }'
```

### 4.4 Cancelar suscripción a un fondo

Cancela una suscripción activa. El monto invertido se devuelve al saldo del usuario.

> Reemplazar `<SUBSCRIPTION_ID>` con el `id` obtenido en la respuesta de suscripción (sección 4.2).

```bash
curl -s -X DELETE http://localhost:8080/api/funds/unsubscribe/<SUBSCRIPTION_ID> \
  -H "Authorization: Bearer <TOKEN>"
```

**Respuesta esperada (204 No Content):**  
Sin cuerpo de respuesta; código HTTP 204 indica operación exitosa.

---

## 5. Transacciones

### 5.1 Consultar historial de transacciones

Obtiene el historial de suscripciones y cancelaciones del usuario autenticado.  
El parámetro `limit` es opcional (por defecto 50, máximo 100).

```bash
# Últimas 20 transacciones
curl -s "http://localhost:8080/api/transactions/history?limit=20" \
  -H "Authorization: Bearer <TOKEN>"
```

**Respuesta esperada (200 OK):**
```json
[
  {
    "transactionId": "uuid",
    "type": "SUBSCRIPTION",
    "fundCode": "DEUDAPRIVADA",
    "fundName": "Deuda Privada",
    "amount": 50000,
    "balanceAfter": 450000,
    "description": "Apertura del fondo Deuda Privada",
    "createdAt": "2026-03-12T10:30:00"
  },
  {
    "transactionId": "uuid",
    "type": "CANCELLATION",
    "fundCode": "DEUDAPRIVADA",
    "fundName": "Deuda Privada",
    "amount": 50000,
    "balanceAfter": 500000,
    "description": "Cancelación del fondo Deuda Privada",
    "createdAt": "2026-03-12T10:35:00"
  }
]
```

```bash
# Sin límite explícito (retorna hasta 50 por defecto)
curl -s "http://localhost:8080/api/transactions/history" \
  -H "Authorization: Bearer <TOKEN>"
```

```bash
# Máximo permitido: 100 transacciones
curl -s "http://localhost:8080/api/transactions/history?limit=100" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 6. Casos de Error (Validaciones)

### 6.1 Registro con email inválido o contraseña muy corta

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "",
    "password": "short"
  }'
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Errores de validación",
  "fieldErrors": { ... }
}
```

### 6.2 Registro con email ya existente

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "carlos.martinez@example.com",
    "password": "password123"
  }'
```

**Respuesta esperada (400/409):**
```json
{
  "message": "El email ya está registrado"
}
```

### 6.3 Login con credenciales incorrectas

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "carlos.martinez@example.com",
    "password": "contraseñaIncorrecta"
  }'
```

**Respuesta esperada (401 Unauthorized):**
```json
{
  "message": "Credenciales inválidas"
}
```

### 6.4 Acceso sin token de autenticación

```bash
curl -s http://localhost:8080/api/funds
```

**Respuesta esperada (401 Unauthorized):**
```json
{
  "message": "Unauthorized"
}
```

### 6.5 Suscripción a un fondo inexistente

```bash
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "fundCode": "FONDO_INEXISTENTE"
  }'
```

**Respuesta esperada (404 Not Found):**
```json
{
  "message": "Fondo no encontrado: FONDO_INEXISTENTE"
}
```

### 6.6 Saldo insuficiente para suscripción

Si el usuario ya gastó su saldo en otros fondos, al intentar suscribirse a uno nuevo:

```bash
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "fundCode": "FDO-ACCIONES"
  }'
```

**Respuesta esperada (400 Bad Request):**
```json
{
  "message": "No tiene saldo disponible para vincularse al fondo Fondo Acciones"
}
```

### 6.7 Token expirado o inválido

```bash
curl -s http://localhost:8080/api/funds \
  -H "Authorization: Bearer token_invalido_12345"
```

**Respuesta esperada (401 Unauthorized)**

---

## 7. Flujo Completo de Prueba

A continuación se presenta un flujo end-to-end para probar todas las funcionalidades:

```bash
# ============================================================
# PASO 1: Verificar que la aplicación esté activa
# ============================================================
curl -s http://localhost:8080/actuator/health

# ============================================================
# PASO 2: Registrar un nuevo usuario
# ============================================================
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "prueba.tecnica@btg.com",
    "password": "password123"
  }'
# >> Copiar el "token" de la respuesta

# ============================================================
# PASO 3: Login (alternativa con usuario precargado)
# ============================================================
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "carlos.martinez@example.com",
    "password": "password123"
  }'
# >> Copiar el "token" de la respuesta y usarlo como <TOKEN>

# ============================================================
# PASO 4: Consultar fondos disponibles
# ============================================================
curl -s http://localhost:8080/api/funds \
  -H "Authorization: Bearer <TOKEN>"

# ============================================================
# PASO 5: Suscribirse al fondo "Deuda Privada" ($50.000)
# Saldo: $500.000 → $450.000
# ============================================================
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"fundCode": "DEUDAPRIVADA"}'
# >> Copiar el "id" de la respuesta como <SUBSCRIPTION_ID_1>

# ============================================================
# PASO 6: Suscribirse al fondo "FPV Recaudadora" ($75.000)
# Saldo: $450.000 → $375.000
# ============================================================
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"fundCode": "FPV_BTG_PACTUAL_RECAUDADORA"}'
# >> Copiar el "id" de la respuesta como <SUBSCRIPTION_ID_2>

# ============================================================
# PASO 7: Suscribirse al fondo "FPV Ecopetrol" ($125.000)
# Saldo: $375.000 → $250.000
# ============================================================
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"fundCode": "FPV_BTG_PACTUAL_ECOPETROL"}'

# ============================================================
# PASO 8: Consultar historial (debe mostrar 3 suscripciones)
# ============================================================
curl -s "http://localhost:8080/api/transactions/history?limit=10" \
  -H "Authorization: Bearer <TOKEN>"

# ============================================================
# PASO 9: Cancelar suscripción al fondo "Deuda Privada"
# Saldo: $250.000 → $300.000
# ============================================================
curl -s -X DELETE http://localhost:8080/api/funds/unsubscribe/<SUBSCRIPTION_ID_1> \
  -H "Authorization: Bearer <TOKEN>"

# ============================================================
# PASO 10: Consultar historial (debe mostrar 3 suscripciones + 1 cancelación)
# ============================================================
curl -s "http://localhost:8080/api/transactions/history?limit=10" \
  -H "Authorization: Bearer <TOKEN>"

# ============================================================
# PASO 11: Intentar suscripción con saldo insuficiente
# Fondo "Fondo Acciones" requiere $250.000 pero el saldo es $300.000
# Esto funcionará:
# ============================================================
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"fundCode": "FDO-ACCIONES"}'
# Saldo: $300.000 → $50.000

# ============================================================
# PASO 12: Ahora sí, intentar otra suscripción (saldo insuficiente)
# ============================================================
curl -s -X POST http://localhost:8080/api/funds/subscribe \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"fundCode": "FPV_BTG_PACTUAL_DINAMICA"}'
# >> Debe retornar error de saldo insuficiente ($50.000 < $100.000)
```

---

## 8. Comandos PowerShell (Automatizados)

Para ejecutar el flujo completo en **Windows PowerShell** de forma automatizada:

```powershell
# ---- Login y obtener token automáticamente ----
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"carlos.martinez@example.com","password":"password123"}'

$token = $loginResponse.token
$headers = @{ Authorization = "Bearer $token" }
Write-Host "Token obtenido: $($token.Substring(0, 20))..."

# ---- Listar fondos ----
Write-Host "`n=== FONDOS DISPONIBLES ==="
Invoke-RestMethod -Uri "http://localhost:8080/api/funds" -Headers $headers | Format-Table

# ---- Suscribirse a un fondo ----
Write-Host "`n=== SUSCRIPCIÓN A DEUDAPRIVADA ==="
$subscription = Invoke-RestMethod -Uri "http://localhost:8080/api/funds/subscribe" `
  -Method POST `
  -ContentType "application/json" `
  -Headers $headers `
  -Body '{"fundCode":"DEUDAPRIVADA"}'
$subscription | Format-List
$subscriptionId = $subscription.id

# ---- Suscribirse a otro fondo ----
Write-Host "`n=== SUSCRIPCIÓN A FPV_BTG_PACTUAL_RECAUDADORA ==="
Invoke-RestMethod -Uri "http://localhost:8080/api/funds/subscribe" `
  -Method POST `
  -ContentType "application/json" `
  -Headers $headers `
  -Body '{"fundCode":"FPV_BTG_PACTUAL_RECAUDADORA"}' | Format-List

# ---- Consultar historial ----
Write-Host "`n=== HISTORIAL DE TRANSACCIONES ==="
Invoke-RestMethod -Uri "http://localhost:8080/api/transactions/history?limit=10" `
  -Headers $headers | Format-Table

# ---- Cancelar suscripción ----
Write-Host "`n=== CANCELAR SUSCRIPCIÓN ==="
Invoke-RestMethod -Uri "http://localhost:8080/api/funds/unsubscribe/$subscriptionId" `
  -Method DELETE `
  -Headers $headers
Write-Host "Suscripción $subscriptionId cancelada exitosamente (204 No Content)"

# ---- Historial final ----
Write-Host "`n=== HISTORIAL FINAL ==="
Invoke-RestMethod -Uri "http://localhost:8080/api/transactions/history?limit=10" `
  -Headers $headers | Format-Table
```

---

## Resumen de Endpoints

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| `GET` | `/actuator/health` | No | Health check |
| `POST` | `/api/auth/register` | No | Registro de usuario |
| `POST` | `/api/auth/login` | No | Inicio de sesión |
| `GET` | `/api/funds` | Sí | Listar fondos disponibles |
| `GET` | `/api/funds/subscriptions` | Sí | Listar suscripciones del usuario |
| `POST` | `/api/funds/subscribe` | Sí | Suscribirse a un fondo |
| `DELETE` | `/api/funds/unsubscribe/{id}` | Sí | Cancelar suscripción |
| `GET` | `/api/transactions/history?limit=N` | Sí | Historial de transacciones |
