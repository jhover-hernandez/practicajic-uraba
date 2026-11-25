# PRACTICAJIC URABÁ

Plataforma web de prácticas profesionales del Politécnico Colombiano Jaime Isaza Cadavid – Regional Urabá.

| Carpeta | Contenido |
|---|---|
| `backend/` | API REST · Java 17, Spring Boot 3, Spring Security, JPA, Flyway, PostgreSQL |
| `frontend/` | React 18 + Vite + Tailwind |
| `docker-compose.yml` | PostgreSQL y Mailpit para desarrollo |
| `docker-compose.piloto.yml` | Despliegue completo del piloto (base, backend, frontend y copia diaria) |
| `MANUAL_USUARIO.md` | Guía de uso por rol |
| `iniciar.bat` | Arranque rápido en Windows (backend, frontend y navegador) |
| `schema.sql` | Esquema inicial (Tarea 6). La fuente de verdad ahora son las migraciones en `backend/src/main/resources/db/migration/` |

## Puesta en marcha (desarrollo)

Comandos para PowerShell de Windows. Ejecútalos uno por línea: PowerShell 5.1 no acepta `&&`.

1. **Base de datos**, con una de estas dos opciones:
   - **PostgreSQL instalado en el equipo** (solo la primera vez):
     ```powershell
     & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -c "CREATE USER practicajic WITH PASSWORD 'practicajic';" -c "CREATE DATABASE practicajic OWNER practicajic;"
     ```
   - **Docker**: abre Docker Desktop y luego ejecuta `docker compose up -d`. Esto levanta además Mailpit, una bandeja de correos de prueba en http://localhost:8025.
     Antes apaga el PostgreSQL local, porque los dos usan el puerto 5432.
2. **Backend** (puerto 8080), en una terminal:
   ```powershell
   cd backend
   $env:APP_CORREO_HABILITADO = "false"   # solo si no usas Docker: el enlace de recuperación se escribe en la consola
   mvn spring-boot:run
   ```
   - Flyway crea las tablas al arrancar.
   - Se crea el administrador inicial `admin@practicajic.local` / `Admin12345` (solo en el perfil `dev`).
   - Swagger: http://localhost:8080/swagger-ui.html
3. **Frontend** (puerto 5173), en otra terminal:
   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

Atajo en Windows: con la base de datos encendida, `iniciar.bat` hace los pasos 2 y 3 y abre el navegador.

Pruebas del backend: `cd backend && mvn test` (usan H2 en memoria, no necesitan PostgreSQL).
`PilotoCompletoTest` recorre los criterios de aceptación del SRS (sección 8) de punta a punta.

## Despliegue del piloto (Tarea 16)

En el servidor, con Docker instalado:

```bash
cp .env.ejemplo .env        # cambia todas las claves; APP_JWT_SECRETO con: openssl rand -base64 48
docker compose -f docker-compose.piloto.yml up -d --build
```

- La plataforma queda en el puerto 80. Nginx sirve el frontend y redirige `/api` al backend.
- Las hojas de vida se guardan en el volumen `piloto-archivos`; la base en `piloto-postgres`.
- RNF-08: el servicio `respaldo` deja cada 24 h un `pg_dump` comprimido en `./respaldos` y borra los de más de 14 días.
  Restaurar: `gunzip -c respaldos/ARCHIVO.sql.gz | docker compose -f docker-compose.piloto.yml exec -T postgres psql -U practicajic practicajic`
- Con HTTPS (recomendado) pon `APP_COOKIE_SEGURA=true`.

## Módulos del piloto (Tareas 10 a 14)

| Módulo | Requisitos | Backend (`/api/...`) | Pantalla |
|---|---|---|---|
| Empresas | RF-06 a RF-11, RN-01, RN-04 | `empresa/perfil`, `empresa/ofertas`, `empresa/ofertas/{id}/postulantes`, `empresa/postulaciones/{id}/...` | Mis ofertas, Postulantes, Prácticas activas, Perfil |
| Estudiantes | RF-12 a RF-16, RNF-04 | `estudiante/perfil`, `estudiante/hoja-vida`, `estudiante/competencias`, `estudiante/portafolio` | Mi perfil digital |
| Convocatorias y postulaciones | RF-17 a RF-27, RN-02, RN-05 a RN-08, RNF-07 | `estudiante/convocatorias`, `estudiante/postulaciones`, `catalogos/*`, `admin/catalogos/*` | Convocatorias, Mis postulaciones, Áreas y competencias |
| Comité evaluador | RF-28 a RF-34 | `comite/ofertas`, `comite/candidatos`, `comite/acta`, `comite/historial`, `admin/ofertas/{id}/evaluador`, `admin/postulaciones/{id}/evaluador` | Ofertas pendientes, Candidatos, Actas, Historial |
| Seguimiento y panel | RF-35 a RF-40 | `admin/practicas`, `empresa/practicas`, `estudiante/practicas`, `admin/panel`, `admin/informes/{tipo}` | Prácticas y seguimiento, Panel, Informes |

Decisiones de implementación:

- **Matrices (RF-29, RF-31):** pesos fijos de la propuesta (25/20/20/15/10/10 y 30/25/15/15/10/5). Una oferta solo se aprueba con el puntaje mínimo `APP_PUNTAJE_MINIMO` (70 por defecto). RN-09 (pesos ajustables por convocatoria) queda pendiente.
- **Competencias coincidentes (RF-25):** cuenta las competencias de la oferta que el estudiante registró, en cualquier nivel.
- **Flujo de la postulación:** solo avanza al estado siguiente. Sale a "No seleccionado" desde revisión, preselección o entrevista. Pasar a Entrevista exige fecha. Seleccionar respeta las vacantes.
- **Aceptar la práctica (RF-27, RN-08):** al aceptar, las demás postulaciones abiertas pasan a "No seleccionado". El administrador formaliza después la práctica.
- **Evaluación final (RF-37):** exige calificación de 0 a 5. Finaliza la práctica y la postulación.
- **Cierre automático (RF-20):** al arrancar el backend y cada día a las 00:05. Las búsquedas también ocultan las ofertas vencidas.
- **Acta (RF-33) y PDF del panel (RF-40):** se imprimen o se guardan como PDF desde el navegador. Los informes Excel son CSV con BOM UTF-8 y punto y coma, que Excel en español abre directamente.
- **Avisos por correo:** validaciones, decisiones del comité, cambios de estado, entrevistas y formalización. Con `APP_CORREO_HABILITADO=false` se escriben en el log.

## Tarea 9 · Autenticación y roles

| Requisito | Implementación |
|---|---|
| RF-01 Registro por tipo de usuario | `POST /api/auth/registro/estudiante` y `/registro/empresa`. Crea el usuario y el perfil básico en estado **Pendiente** |
| RF-02 Iniciar y cerrar sesión | `POST /api/auth/login`, `/logout`. Bloqueo de 15 min tras 5 intentos fallidos |
| RF-03 Recuperar contraseña | `POST /api/auth/recuperar` envía un enlace de un solo uso (30 min); `/restablecer` define la nueva. Siempre responde igual, para no revelar qué correos existen |
| RF-04 / RNF-03 Acceso por rol | `SeguridadConfig`: `/api/admin/**` → ADMIN, `/api/empresa/**` → EMPRESA, `/api/estudiante/**` → ESTUDIANTE, `/api/comite/**` → COMITE. En el frontend, `RutaProtegida` |
| RF-05 Gestión de usuarios | `/api/admin/usuarios`: listar con filtros, crear (admin/comité), editar, activar o desactivar. Pantalla *Usuarios y roles* |
| RNF-01 BCrypt | `BCryptPasswordEncoder(12)` |
| RNF-02 JWT que expira y se renueva | Token de acceso de 15 min. Token de renovación de 7 días en cookie `HttpOnly; SameSite=Strict`, que rota en cada uso y detecta reutilización |
| RNF-05 Registro de actividades | Tabla `registro_actividad` con registro, creación, edición, (des)activación y restablecimiento |
| RNF-06 Política de datos | Casilla obligatoria al registrarse; se guarda `usuario.fecha_aceptacion_politica` |
| RNF-11 Mensajes en español | `ManejadorErrores` y manejadores de 401 y 403 |

### Convenciones para agregar funciones nuevas

- Ubiquen los endpoints bajo el prefijo del rol (`/api/empresa/...`, `/api/comite/...`); así quedan protegidos automáticamente.
  Si una ruta la comparten varios roles, usen `@PreAuthorize("hasAnyRole('EMPRESA','COMITE')")`.
- Usuario actual en el controlador: `@AuthenticationPrincipal UsuarioAutenticado actual`.
- Acciones de validación, aprobación o cambio de estado: `actividadService.registrar(...)` (RNF-05).
- Errores de negocio: `throw new NegocioException(HttpStatus.X, "Mensaje claro en español")`.
- En el frontend, agreguen la entrada del menú en `auth/roles.js` y su página en el objeto `PAGINAS` de `App.jsx`.

### Variables de entorno para el piloto (perfil distinto de `dev`)

`APP_JWT_SECRETO` (Base64, ≥ 256 bits: `openssl rand -base64 48`), `APP_ADMIN_CORREO`, `APP_ADMIN_CONTRASENA`,
`DB_URL`, `DB_USUARIO`, `DB_CONTRASENA`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USUARIO`, `MAIL_CONTRASENA`,
`APP_CORREO_HABILITADO=true`, `APP_FRONTEND_URL`, `APP_CORS_ORIGENES`. Con HTTPS, `APP_COOKIE_SEGURA` queda en `true` (valor por defecto).
