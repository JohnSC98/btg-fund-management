# agend.md — Guía de desarrollo para BTG Fund Management

Este documento define lineamientos de trabajo para mantener el proyecto consistente con **Clean Architecture**, seguro por defecto y mantenible en el tiempo.

## 1) Principios generales

- Priorizar cambios pequeños, claros y reversibles.
- Mantener separación estricta por capas: `domain` → `application` → `adapter`.
- Escribir código orientado a casos de uso, no al framework.
- Evitar sobreingeniería: resolver el problema con la menor complejidad posible.
- No romper contratos públicos (endpoints, DTOs, respuestas) sin versionar.

## 2) Clean Code (obligatorio)

### Nombres y estructura
- Usar nombres explícitos de dominio (`Subscription`, `Fund`, `TransactionHistory`).
- Métodos cortos, con una sola responsabilidad.
- Clases cohesionadas: si una clase crece demasiado, dividir por responsabilidad.
- Evitar abreviaturas ambiguas y variables de una sola letra.

### Reglas de implementación
- Validar entradas en borde (`adapter.web`) y reglas de negocio en `domain/application`.
- Extraer lógica repetida en funciones privadas o servicios específicos.
- Evitar `if/else` anidados cuando se pueda usar guard clauses.
- Usar excepciones de dominio (`domain.exception`) para reglas de negocio.
- No mezclar lógica de persistencia con lógica de negocio.

### Pruebas
- Todo caso de uso nuevo debe venir con tests unitarios.
- Cubrir casos felices + casos inválidos + bordes de negocio.
- Preferir tests legibles (Given/When/Then implícito).
- Mockear puertos, no detalles internos.

## 3) Seguridad (obligatorio)

### Autenticación y autorización
- Toda ruta sensible debe requerir JWT válido.
- Validar rol/permisos antes de ejecutar casos de uso críticos.
- Nunca confiar en `userId` enviado por cliente si ya viene en el token.

### JWT
- Usar secreto robusto (mínimo 256 bits) por variable de entorno.
- Definir expiración corta para access token.
- Rechazar tokens expirados, mal firmados o con issuer/audience inválido (si aplica).
- No registrar el token completo en logs.

### Gestión de secretos y datos
- No hardcodear secretos en código ni en tests.
- Usar variables de entorno para `JWT_SECRET`, `MONGODB_URI` y credenciales.
- No exponer stack traces ni mensajes internos en respuestas HTTP.
- Sanitizar mensajes de error al cliente; detalle técnico solo en logs internos.

### Seguridad de API
- Validar payloads con Bean Validation en DTOs.
- Limitar datos devueltos en responses (principio de mínima exposición).
- Implementar rate limiting cuando el entorno lo requiera.
- Revisar CORS según entorno (dev/staging/prod), evitando configuraciones abiertas en producción.

## 4) Skills técnicos esperados por stack

### Spring Boot / Spring Security
- Manejo sólido de inyección de dependencias y configuración por perfiles.
- Diseño REST consistente: códigos HTTP correctos, DTOs claros, validación robusta.
- Uso correcto de filtros/cadenas de seguridad y manejo de excepciones global.
- Entender límites entre `@Service`, `@Component`, adapters y puertos de dominio.

### MongoDB (DocumentDB compatible)
- Modelado por agregado y patrón de acceso real.
- Índices para consultas frecuentes (`email`, `userId`, `fundCode`, timestamps).
- Evitar consultas costosas sin paginación o límites.
- Versionar cambios de estructura de documentos sin romper datos existentes.

### JWT
- Generación, firma y validación de tokens según buenas prácticas.
- Manejo de claims mínimos: `sub`, `role`, `iat`, `exp` (y otros si se justifican).
- Rotación de secretos y estrategia de revocación según criticidad del entorno.

## 5) Convenciones del proyecto

- Mantener organización actual:
  - `domain`: entidades, puertos, excepciones.
  - `application`: casos de uso.
  - `adapter`: web, persistence, notification.
  - `config`: wiring y configuración transversal.
- Registrar eventos relevantes de negocio sin filtrar datos sensibles.
- Preservar compatibilidad con despliegue en AWS y DocumentDB.

## 6) Checklist antes de merge

- Compila sin errores (`mvn clean test`).
- Tests de la funcionalidad nueva/presente en verde.
- Sin secretos hardcodeados ni logs sensibles.
- Endpoints protegidos correctamente por rol/JWT.
- Cambios alineados a Clean Architecture y sin acoplamientos cruzados.
