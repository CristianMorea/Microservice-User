

#  👤 Usuario Service


# Visión General
 Microservicio de gestión de usuarios para la plataforma e-commerce.
> Maneja registro, autenticación JWT, perfiles, roles 
## Descripcion

Usuario Service es el microservicio encargado de toda la gestión de identidad dentro de la plataforma e-commerce. Provee:

Registro y autenticación de usuarios vía email/password
Login con Google (OAuth2)
Generación y validación de JWT para comunicación entre microservicios
Gestión de perfiles de usuario
Sistema de roles (CLIENTE,  ADMIN)
Registro automático en Eureka para Service Discovery
## 🛠 Tecnologías Clave

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 21 | Lenguaje |
| Spring Boot | 4.0.2 | Framework principal |
| Spring Security | 7.0.2 | Autenticación y autorización |
| Spring Data JPA | 4.0.2 | Persistencia |
| Hibernate | 7.2.1 | ORM |
| MySQL | 8.4 | Base de datos |
| JWT (JJWT) | 0.12.6 | Tokens de autenticación |
| Docker | - | Contenedorización |
| Eureka Client | 5.0.0 | Service Discovery |
| SpringDoc OpenAPI | 2.8.6 | Documentación Swagger |
| JUnit 5 + Mockito | - | Tests unitarios |
| Testcontainers | 1.19.7 | Tests de integración con MySQL real |
| JaCoCo | 0.8.11 | Cobertura de código |
| Bucket4j | 7.6.0 | Rate limiting en /login |
| Caffeine | 3.1.8 | Cache JWT en memoria |
| Spring Cache | - | Abstracción de cache |
##  Arquitectura

##  Arquitectura del microservicio

![Arquitectura](docs/img/arquitectura%20del%20microservicio.png)

## Flujo de autenticación JWT

![Arquitectura](docs/img/flujo%20autenticacion.png)



## Flujo de registro

![Arquitectura](docs/img/Flujo%20registro.png)


## Estructura de Archivos



![Arquitectura](docs/img/usuarios%201.png)


![Arquitectura](docs/img/usuarios%202.png)



![Arquitectura](docs/img/usuarios%203.png)


## Datos semilla (DataInitializer)

Al arrancar el servicio se insertan automáticamente:

| Rol | Nivel |
|---|---|
| CLIENTE | 1 |
| VENDEDOR | 2 |
| ADMIN | 3 |

## Requisitos previos

- Java 21+
- Maven 3.8+
- Docker Desktop (para la BD y Testcontainers)

## 🔗 Endpoints

**Context path:** `/usuario`

### 🔓 Autenticación — Público

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/usuarios/register` | Registrar nuevo usuario |
| `POST` | `/usuarios/login` | Login — retorna JWT |
| `GET` | `/usuarios/oauth2/success` | Callback OAuth2 exitoso |
| `GET` | `/usuarios/oauth2/failure` | Callback OAuth2 fallido |

#### Ejemplo registro
```json
POST /usuario/usuarios/register

// Request:
{
    "email": "juan@gmail.com",
    "passwordHash": "Pass123!",
    "nombre": "Juan Pérez",
    "telefono": "300123456",
    "direccion": "Calle 123"
}

// Response 201:
{
    "usuarioId": 1,
    "email": "juan@gmail.com",
    "nombre": "Juan Pérez"
}
```



#### Ejemplo login
```json

POST /usuario/usuarios/login
{
    "email": "juan@gmail.com",
    "password": "Pass123!"
}

// Response:
{
    "usuarioId": 1,
    "email": "juan@gmail.com",
    "nombre": "Juan Pérez",
    "rol": "CLIENTE",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 👤 Perfil — Requiere JWT

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/Perfil` | Ver mi perfil |
| `PUT` | `/Perfil/editar` | Editar mi perfil |
| `PUT` | `/Perfil/cambiar-password` | Cambiar mi contraseña |

### 🛡️ Admin — Requiere rol ADMIN

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/admin/listar` | Listar usuarios paginado |
| `GET` | `/admin/{id}` | Ver perfil de cualquier usuario |
| `DELETE` | `/admin/{id}` | Soft delete de usuario |
| `POST` | `/admin/{id}/roles` | Asignar rol |
| `DELETE` | `/admin/{id}/roles/{rolNombre}` | Quitar rol |

#### Paginación en `/admin/listar`

```
GET /admin/listar                        → página 0, 10 por página
GET /admin/listar?pagina=1               → segunda página
GET /admin/listar?pagina=0&tamaño=25     → 25 por página (máx 100)
```

### Códigos de respuesta

| Código | Significado |
|---|---|
| `200` | OK |
| `201` | Creado exitosamente |
| `204` | Sin contenido (DELETE exitoso) |
| `400` | Error de negocio |
| `401` | No autenticado |
| `403` | Sin permisos |
| `404` | Recurso no encontrado |
| `409` | Conflicto (duplicado) |
| `500` | Error interno |
## 🔐 Autenticación

El servicio usa **JWT Bearer Token** con firma **HS256**.

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Contenido del token (claims)

```json
{
    "sub": "juan@gmail.com",
    "usuarioId": 1,
    "roles": "CLIENTE",
    "iat": 1709123456,
    "exp": 1709127056
}
```

## 👥 Roles del sistema

| Rol | Nivel | Asignado | Acceso |
|---|---|---|---|
| `CLIENTE` | 1 | Automático al registrarse | Gestión de su propio perfil |
| `VENDEDOR` | 2 | Solo por ADMIN | Gestión de productos (futuro) |
| `ADMIN` | 3 | Solo por ADMIN | Acceso total |

### Qué se testea

| Capa | Clase | Qué cubre |
|---|---|---|
| Unitario | `UsuarioServiceTest` | register, login, perfil, password, eliminar |
| Unitario | `RolServiceTest` | asignar, quitar, validaciones de negocio |
| Unitario | `JwtServiceTest` | generate, validate, claims, token expirado |
| Integración | `UsuarioControllerTest` | endpoints /register y /login |
| Integración | `PerfilControllerTest` | ver, editar, cambiar password con JWT |
| Integración | `AdminControllerTest` | listar, ver, eliminar, gestión de roles |
| E2E | `RegistroLoginFlowTest` | flujos completos con MySQL real |
| E2E | `AdminFlowTest` | flujos admin completos |
| E2E | `TokenSecurityTest` | tokens manipulados, expirados, sin Bearer |

---
## ⚡ Optimizaciones de rendimiento

### Cache JWT (Caffeine)
Los tokens validados se cachean en memoria para evitar recalcular
la firma HS256 en cada request:
- TTL: 5 minutos de inactividad
- Máximo: 10,000 tokens simultáneos
- Solo cachea tokens válidos (`unless = "#result == false"`)
- Se invalida automáticamente al cambiar password (`@CacheEvict`)

### JOIN FETCH — elimina problema N+1
Todos los queries que cargan usuarios traen roles y perfil
en una sola query:
- `findActiveUserWithRoles()` → login
- `findByIdWithDetails()` → obtenerPerfil, editarPerfil
- `findAllActivosWithDetails()` → listado paginado admin

Sin esta optimización, 100 usuarios = 201 queries.
Con JOIN FETCH = 2 queries siempre.

### Índices MySQL
```sql
CREATE INDEX idx_usuario_email ON Usuario(email);
CREATE INDEX idx_usuario_deleted_at ON Usuario(deleted_at);
CREATE INDEX idx_usuario_email_active ON Usuario(email, is_active);
```

### entityManager.refresh()
En `RolServiceImpl` se usa `entityManager.refresh(usuario)` en vez
de `entityManager.clear()` — refresca solo la entidad necesaria
sin invalidar todo el contexto de Hibernate.


## 🛡️ Seguridad adicional

### Rate Limiting en /login
Protección contra fuerza bruta en el endpoint de login:
- Máximo 5 intentos por IP por minuto
- Algoritmo: Token Bucket (Bucket4j)
- Soporta proxies: X-Forwarded-For, X-Real-IP
- Retorna HTTP 429 Too Many Requests si se excede

### Correlation ID
Cada request recibe un identificador único X-Correlation-ID:
- Generado en CorrelationIdFilter (primer filtro)
- Disponible en todos los logs via MDC
- Propagado en el header de respuesta
- Incluido en todas las respuestas de error
- Se limpia en finally para evitar memory leaks

```json
Ejemplo error con correlationId

{
    "status": 404,
    "error": "Usuario no encontrado",
    "timestamp": "2026-03-13T15:05:12",
    "correlationId": "a3f2c1d4-9b8e-4f2a-b1c3-d4e5f6a7b8c9"
}
```
## 💡 Decisiones de diseño

**¿Por qué soft delete?**
Los usuarios eliminados mantienen su historial de órdenes y pagos. Borrarlos físicamente rompería la integridad referencial con los otros microservicios.

**¿Por qué JWT stateless?**
No hay sesiones compartidas entre microservicios. Cada servicio valida el token localmente sin consultar la BD, permitiendo escalar independientemente.

**¿Por qué tabla `usuario_rol` en vez de `@ManyToMany` simple?**
La tabla intermedia permite almacenar metadatos: quién asignó el rol (`assigned_by`), cuándo (`assigned_at`) y cuándo expira (`expires_at`). Útil para auditoría y roles temporales.

**¿Por qué `@EmbeddedId` en `UsuarioRol`?**
La PK compuesta `(usuario_id, rol_id)` garantiza a nivel de BD que un usuario no puede tener el mismo rol dos veces, sin depender solo de validaciones en código.

**¿Por qué `DataInitializer`?**
Los roles y el admin son datos críticos. Crearlos en código (idempotente) es más confiable que scripts SQL manuales, especialmente en múltiples entornos.
