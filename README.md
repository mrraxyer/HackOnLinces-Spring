# HackOnLinces — Backend API

**Backend REST API** del sistema de gestión de OnLinces, el club de programación del TecNM en Celaya.

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Contexto funcional](#contexto-funcional)
- [Stack tecnológico](#stack-tecnológico)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Requisitos previos](#requisitos-previos)
- [Configuración del entorno](#configuración-del-entorno)
- [Ejecución con Docker (recomendado)](#ejecución-con-docker-recomendado)
- [Ejecución local con Maven](#ejecución-local-con-maven)
- [Para el equipo de frontend](#para-el-equipo-de-frontend)
- [API Endpoints](#api-endpoints)
- [Modelos de datos](#modelos-de-datos)
- [Roles y permisos](#roles-y-permisos)
- [Troubleshooting](#troubleshooting)

---

## Descripción general

**HackOnLinces** es el backend API de gestión para **OnLinces**, el club de programación del Instituto Tecnológico de Celaya (ITcelaya). El sistema centraliza la administración de participantes, eventos y competencias del club.

Funcionalidades principales:

- Registro y autenticación de miembros internos (Google OAuth2) y externos (email/password)
- Validación automática de dominio `@itcelaya.edu.mx` para usuarios internos
- Gestión de roles y permisos para diferentes tipos de usuarios
- Sistema de admisión con cartas de intención y carga de archivos
- Panel administrativo con métricas, dashboards y revisión de candidatos
- API REST documentada con Swagger/OpenAPI
- Almacenamiento de archivos para documentos y evidencias

---

## Contexto funcional

### Tipos de usuarios

| Tipo | Descripción |
|------|-------------|
| **INTERNAL** | Miembros del ITcelaya (`@itcelaya.edu.mx`) — estudiantes, profesores, staff |
| **EXTERNAL** | Participantes externos de otras instituciones |

### Estados de cuenta

| Estado | Significado |
|--------|-------------|
| **PENDING** | Esperando revisión |
| **APPROVED** | Aprobado para participar |
| **REJECTED** | Rechazado, sin acceso |

### Flujo de admisión

```
1. REGISTRO           → Estado: PENDING, Rol: GUEST
2. CARTA DE INTENCIÓN → Usuario externo sube PDF
3. REVISIÓN ADMIN
   ├─ APROBADA        → Estado: APPROVED, Rol: PARTICIPANT
   ├─ RECHAZADA       → Estado: REJECTED
   └─ REENVÍO         → Usuario puede volver a intentar
4. PARTICIPACIÓN      → Acceso a hackathons y eventos
```

### Roles

| Rol | Descripción |
|-----|-------------|
| **ADMIN** | Crear usuarios, revisar admisiones, ver dashboard |
| **JUDGE** | Acceso a info de participantes, calificación |
| **SPEAKER** | Acceso a recursos del evento |
| **PARTICIPANT** | Acceso completo a eventos del club |
| **GUEST** | Puede enviar carta de intención |

---

## Stack tecnológico

| Componente | Versión |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.5.13 |
| PostgreSQL | 16 |
| JWT (JJWT) | 0.12.6 |
| Springdoc OpenAPI | 2.8.5 |
| Maven | 3.9+ |

---

## Estructura del proyecto

```
src/main/java/mx/itcelaya/hackonlinces/HackOnLinces/
├── config/           # SecurityConfig, OpenApiConfig, DataInitializer
├── controller/       # AuthController, AdminController, SubmissionController
├── service/          # AuthService, AdminService, SubmissionService, UserService
│   └── strategy/     # RegistrationFactory + estrategias de registro
├── repository/       # UserRepository, SubmissionRepository, ...
├── entity/           # User, Role, UserRole, Submission, Document, AuthProvider
├── dto/
│   ├── request/      # RegisterRequest, LoginRequest, CreateUserRequest, ...
│   └── response/     # AuthResponse, AdminUserResponse, DashboardResponse, ...
├── mapper/           # UserMapper, SubmissionMapper
├── security/         # JwtUtil, JwtAuthFilter, AppUserDetails, OAuth2SuccessHandler
├── enums/            # AccountStatus, UserType, RoleName, SubmissionStatus, ...
└── exception/        # GlobalExceptionHandler, excepciones personalizadas

src/main/resources/
├── application.yaml
├── application-dev.yaml
└── application-prod.yaml

docker/
├── Dockerfile
└── docker-compose.yml
```

---

## Requisitos previos

### Con Docker (recomendado)
- Docker 20.10+
- Docker Compose v2+

### Sin Docker
- Java 21+
- Maven 3.9+
- PostgreSQL 16+

---

## Configuración del entorno

Crea el archivo `.env` en la **raíz del proyecto** (al mismo nivel que `pom.xml`):

```dotenv
# PostgreSQL
POSTGRES_DB=HackOnLinces
POSTGRES_USER=postgres
POSTGRES_PASSWORD=tu_password_segura

# Credenciales de la app (pueden ser iguales a POSTGRES_*)
DB_USERNAME=postgres
DB_PASSWORD=tu_password_segura

# Spring
SPRING_PROFILES_ACTIVE=dev
PORT=8080

# JWT
JWT_SECRET=genera_una_clave_de_al_menos_256_bits
JWT_EXPIRATION=86400000

# Google OAuth2 (requerido para login de usuarios INTERNAL)
GOOGLE_CLIENT_ID=tu_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu_client_secret

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# Uploads y configuración
UPLOAD_DIR=uploads
MAX_FILE_SIZE=100MB
MAX_REQUEST_SIZE=500MB
SUBMISSION_MAX_ATTEMPTS=3
INTERNAL_EMAIL_DOMAIN=itcelaya.edu.mx

# JVM (opcional)
JAVA_OPTS=-Xmx512m
```

> **Generar JWT_SECRET seguro:**
> ```bash
> # Linux/Mac
> openssl rand -base64 32
>
> # Windows (PowerShell)
> [Convert]::ToBase64String((1..32 | ForEach-Object { [byte](Get-Random -Maximum 256) }))
> ```

---

## Ejecución con Docker (recomendado)

El `docker-compose.yml` levanta dos servicios: **PostgreSQL** (`hackonlinces_db`) y la **aplicación** (`hackonlinces_app`). La app espera a que la base de datos esté healthy antes de arrancar.

La imagen se construye con un **multi-stage Dockerfile**: primero compila con Maven, luego genera una imagen final ligera con solo el JRE. El proceso corre bajo un usuario sin privilegios (`appuser`).

### Levantar el proyecto

```bash
# Clonar el repositorio
git clone https://github.com/tu-repo/HackOnLinces.git
cd HackOnLinces

# Crear el archivo de entorno (editar con tus valores)
cp .env.example .env

# Construir la imagen y levantar los servicios
docker compose -f docker/docker-compose.yml up -d --build
```

### Ver logs

```bash
# Todos los servicios
docker compose -f docker/docker-compose.yml logs -f

# Solo la aplicación
docker compose -f docker/docker-compose.yml logs -f app

# Solo la base de datos
docker compose -f docker/docker-compose.yml logs -f db
```

### Detener y limpiar

```bash
# Detener sin borrar datos
docker compose -f docker/docker-compose.yml down

# Detener y eliminar volúmenes (borra la BD)
docker compose -f docker/docker-compose.yml down -v

# Reconstruir desde cero si cambiaste código
docker compose -f docker/docker-compose.yml up -d --build --force-recreate
```

### Verificar estado de los contenedores

```bash
docker compose -f docker/docker-compose.yml ps
```

Deberías ver algo así cuando todo está OK:

```
NAME                  STATUS          PORTS
hackonlinces_db       healthy         5432/tcp
hackonlinces_app      running         0.0.0.0:8080->8080/tcp
```

> **Nota sobre puertos:** La base de datos usa `expose` (solo accesible dentro de la red Docker `hackonlinces-net`), no `ports`. La app es el único servicio expuesto al host en el puerto configurado con `PORT` (default: `8080`).

---

## Ejecución local con Maven

Usa este método si prefieres correr la app directamente sin Docker, con una instancia de PostgreSQL ya instalada en tu máquina.

### 1. Preparar la base de datos

```bash
# Crear la base de datos
createdb -U postgres HackOnLinces

# O desde psql
psql -U postgres -c 'CREATE DATABASE "HackOnLinces";'
```

### 2. Configurar variables de entorno

Asegúrate de tener el `.env` creado (ver sección anterior) o exporta las variables manualmente:

```bash
export SPRING_PROFILES_ACTIVE=dev
export POSTGRES_DB=HackOnLinces
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=tu_password
# ... resto de variables
```

### 3. Compilar

```bash
# Compilar y descargar dependencias (primera vez puede tardar)
mvn clean package -DskipTests
```

### 4. Ejecutar

```bash
# Opción A: ejecutar el JAR generado
java -jar target/HackOnLinces-0.0.1-SNAPSHOT.jar

# Opción B: ejecutar directamente con Maven (útil para desarrollo)
mvn spring-boot:run

# Opción C: ejecutar con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Comandos Maven útiles

```bash
mvn test                          # Ejecutar tests
mvn clean package -DskipTests    # Compilar sin tests
mvn dependency:tree               # Ver árbol de dependencias
mvn clean                         # Limpiar archivos compilados
```

---

## Para el equipo de frontend

Esta sección resume todo lo que necesitas para integrar el frontend con este backend.

**Base URL:** `http://localhost:8080/api/v1`

**Swagger UI (documentación interactiva):** `http://localhost:8080/api/v1/swagger-ui.html`
Todos los endpoints están documentados ahí con ejemplos de request/response. Puedes probarlos desde el navegador usando el botón **Authorize 🔒** con tu token JWT.

---

### Autenticación

Hay dos flujos dependiendo del tipo de usuario:

**Usuarios externos — email + password:**

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "estudiante@unam.mx",
  "password": "Password123!"
}
```

La respuesta incluye un campo `token`. Guárdalo y envíalo en cada request protegido:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Usuarios internos — Google OAuth2 (`@itcelaya.edu.mx`):**

Redirige al usuario a:

```
GET /oauth2/authorization/google
```

Google autentica, el backend valida el dominio institucional y redirige al frontend con el JWT en la URL de callback. Asegúrate de que el origen del frontend esté en `CORS_ALLOWED_ORIGINS` del `.env` del backend.

---

### Estructura de respuesta estándar

Todos los endpoints envuelven su respuesta en este formato:

```json
{
  "success": true,
  "message": "Descripción del resultado",
  "data": { },
  "timestamp": "2025-06-01T10:00:00"
}
```

En caso de error:

```json
{
  "success": false,
  "message": "Descripción del error",
  "timestamp": "2025-06-01T10:00:00"
}
```

Para errores de validación (`400`), se agrega un objeto `errors` con los campos que fallaron:

```json
{
  "success": false,
  "message": "Error de validación en los campos enviados",
  "errors": {
    "email": "El email no tiene un formato válido",
    "password": "La contraseña debe tener entre 8 y 100 caracteres"
  },
  "timestamp": "2025-06-01T10:00:00"
}
```

---

### Códigos HTTP a manejar

| Código | Cuándo ocurre |
|--------|---------------|
| `200` | Operación exitosa (GET, PATCH) |
| `201` | Recurso creado (POST) |
| `400` | Datos de entrada inválidos |
| `401` | Token JWT ausente, expirado o inválido |
| `403` | Sin permisos para esta acción |
| `404` | Recurso no encontrado |
| `409` | Conflicto — el recurso ya existe o el estado no lo permite |

---

### Registro de usuario externo

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "juan@gmail.com",
  "fullName": "Juan Pérez",
  "instituteName": "UNAM",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

La cuenta se crea con `accountStatus: PENDING` y `roles: ["GUEST"]`. El usuario puede loguearse de inmediato pero necesita enviar su carta de intención y ser aprobado por el admin para acceder como participante.

---

### Enviar carta de intención

```http
POST /api/v1/submissions
Authorization: Bearer <token>
Content-Type: multipart/form-data

file:   (PDF obligatorio)
reason: "Texto opcional de motivación"
```

Restricciones a validar desde el formulario antes de llamar al endpoint:

- Solo usuarios con `userType: EXTERNAL` pueden enviarla.
- No se puede enviar si ya hay una submission en estado `PENDING` — mostrar mensaje de espera.
- No se puede enviar si la cuenta ya está `APPROVED`.
- Hay un límite máximo de intentos (default: 3). Si se alcanza, el backend responde `403`.

---

### Listar submissions del usuario actual

```http
GET /api/v1/submissions
Authorization: Bearer <token>
```

Devuelve todas las submissions del usuario autenticado ordenadas por número de intento, útil para mostrar el historial de revisiones.

---

### Filtros disponibles en el listado de usuarios (panel admin)

```
GET /api/v1/admin/users
GET /api/v1/admin/users?status=PENDING
GET /api/v1/admin/users?userType=EXTERNAL
GET /api/v1/admin/users?search=juan
GET /api/v1/admin/users?status=APPROVED&userType=EXTERNAL&search=juan
```

Todos los parámetros son opcionales y combinables. El parámetro `search` busca por nombre completo o email (sin distinción de mayúsculas/minúsculas).

---

## API Endpoints

**Base URL:** `http://localhost:8080/api/v1`

### Autenticación (`/auth`)

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/auth/register` | Registro de usuario externo | No |
| `POST` | `/auth/login` | Login con email y password | No |
| `GET` | `/oauth2/authorization/google` | Inicio de flujo OAuth2 Google | No |

### Submissions (`/submissions`)

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/submissions` | Enviar carta de intención (multipart) | JWT |
| `GET` | `/submissions` | Listar mis submissions | JWT |
| `GET` | `/submissions/{id}` | Detalle de una submission | JWT |

### Administración (`/admin`) — requiere rol `ADMIN`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `GET` | `/admin/dashboard` | Métricas del sistema | JWT + ADMIN |
| `GET` | `/admin/users` | Listar usuarios (filtros opcionales) | JWT + ADMIN |
| `POST` | `/admin/users` | Crear usuario manualmente | JWT + ADMIN |
| `PATCH` | `/admin/users/{id}/review` | Aprobar o rechazar usuario | JWT + ADMIN |
| `PATCH` | `/admin/users/{id}/role` | Cambiar rol de usuario | JWT + ADMIN |
| `GET` | `/admin/waitlist` | Usuarios externos pendientes | JWT + ADMIN |
| `GET` | `/admin/submissions` | Cartas pendientes de revisión | JWT + ADMIN |
| `GET` | `/admin/submissions/{id}` | Detalle de cualquier carta | JWT + ADMIN |
| `PATCH` | `/admin/submissions/{id}/review` | Revisar carta de intención | JWT + ADMIN |

---

## Modelos de datos

### User

```json
{
  "id": 1,
  "fullName": "Juan Pérez",
  "email": "juan@gmail.com",
  "instituteName": "UNAM",
  "userType": "EXTERNAL",
  "accountStatus": "PENDING",
  "roles": ["GUEST"],
  "createdAt": "2025-06-01T10:00:00"
}
```

### Submission

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "userId": 5,
  "attemptNumber": 1,
  "status": "PENDING",
  "reason": null,
  "reviewedById": null,
  "reviewedAt": null,
  "sentAt": "2025-06-01T10:30:00",
  "documents": [
    {
      "id": "doc-uuid",
      "originalName": "carta.pdf",
      "mimeType": "application/pdf",
      "size": 204800,
      "uploadedAt": "2025-06-01T10:30:00"
    }
  ]
}
```

### Dashboard (solo ADMIN)

```json
{
  "users": {
    "total": 42,
    "pending": 10,
    "approved": 28,
    "rejected": 4,
    "internal": 15,
    "external": 27,
    "admins": 2,
    "judges": 3,
    "speakers": 5,
    "participants": 20,
    "guests": 12,
    "registeredToday": 3,
    "registeredThisWeek": 11
  },
  "submissions": {
    "total": 35,
    "pending": 8,
    "approved": 20,
    "rejected": 5,
    "resubmitRequired": 2
  }
}
```

---

## Roles y permisos

| Acción | ADMIN | JUDGE | SPEAKER | PARTICIPANT | GUEST |
|--------|:-----:|:-----:|:-------:|:-----------:|:-----:|
| Ver dashboard | ✅ | ❌ | ❌ | ❌ | ❌ |
| Gestionar usuarios | ✅ | ❌ | ❌ | ❌ | ❌ |
| Revisar submissions | ✅ | ❌ | ❌ | ❌ | ❌ |
| Enviar submission | ✅ | ✅ | ✅ | ✅ | ✅ |
| Ver perfil propio | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## Troubleshooting

### La app no conecta a la BD en Docker

```bash
# Verificar que la BD está healthy
docker compose -f docker/docker-compose.yml ps

# Ver logs de la BD
docker compose -f docker/docker-compose.yml logs db

# Reiniciar solo la BD
docker compose -f docker/docker-compose.yml restart db
```

### Puerto 8080 en uso

```bash
# Ver qué proceso lo ocupa
lsof -i :8080                    # Linux/Mac
netstat -ano | findstr :8080     # Windows

# O cambiar el puerto en .env
PORT=8081
```

### Error CORS desde el frontend

Verifica que `CORS_ALLOWED_ORIGINS` en `.env` incluye exactamente el origen del frontend (con protocolo y puerto):

```dotenv
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### Reconstruir imagen después de cambios en código

```bash
docker compose -f docker/docker-compose.yml up -d --build --force-recreate app
```

---

## Variables de entorno de referencia

| Variable | Default | Descripción |
|----------|-----|-------------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Perfil activo (`dev` o `prod`) |
| `PORT` | `8080` | Puerto expuesto |
| `JWT_EXPIRATION` | `86400000` | Expiración JWT en ms (24h) |
| `SUBMISSION_MAX_ATTEMPTS` | `5` | Intentos máximos de submission |
| `INTERNAL_EMAIL_DOMAIN` | `itcelaya.edu.mx` | Dominio institucional |
| `UPLOAD_DIR` | `uploads` | Directorio de archivos (en Docker: `/app/uploads`) |
| `JAVA_OPTS` | —   | Opciones JVM (ej. `-Xmx512m`) |

---

**Versión:** `0.0.1-SNAPSHOT` · **Última actualización:** Marzo 2026
**OnLinces** — Club de Programación del TecNM en Celaya