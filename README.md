# PRACTICAJIC URABÁ

Plataforma web que conecta a los estudiantes del **Politécnico Colombiano Jaime Isaza Cadavid – Regional Urabá** con empresas de la región para gestionar las prácticas profesionales: registro, publicación de ofertas, postulación, evaluación del comité, vinculación y seguimiento.

**Entrega del piloto:** 19 de diciembre de 2026.

## Stack

| Capa | Tecnología |
| --- | --- |
| Frontend | React + Vite + Tailwind (con Node.js) |
| Backend | Java 17 + Spring Boot (API REST, Spring Security + JWT) |
| Base de datos | PostgreSQL |
| Despliegue | Docker |

## Estructura del repositorio

```
practicajic-uraba/
├── docs/        Propuesta, SRS, diagrama ER, casos de uso, Kanban y explicación del prototipo
├── prototipo/   Prototipo UX/UI navegable (abrir prototipo/index.html en el navegador)
├── database/    schema.sql: esquema inicial de referencia (la fuente de verdad son las migraciones Flyway del backend)
├── frontend/    Aplicación React + Vite + Tailwind
├── backend/     API REST Spring Boot 3 + Spring Security + JPA + Flyway
├── docker-compose.yml  PostgreSQL y Mailpit para desarrollo
└── iniciar.bat  Enciende backend y frontend con doble clic (Windows)
```

## Roles del sistema

- **Estudiante:** perfil digital, hoja de vida, competencias, búsqueda de convocatorias y postulaciones.
- **Empresa:** registro, publicación de ofertas y gestión de postulantes.
- **Comité evaluador:** califica ofertas y candidatos con matrices de 100 puntos.
- **Administrador institucional:** valida usuarios, asigna evaluadores, formaliza prácticas y consulta indicadores.

## Ver el prototipo

Descarga el repositorio y abre `prototipo/index.html` con doble clic. Usa la barra verde superior para cambiar de pantalla.

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

Pruebas del backend: `cd backend && mvn test` (usan H2 en memoria, no necesitan PostgreSQL).

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

### Reglas para los demás módulos (Tareas 10 a 14)

- Ubiquen los endpoints bajo el prefijo del rol (`/api/empresa/...`, `/api/comite/...`); así quedan protegidos automáticamente.
  Si una ruta la comparten varios roles, usen `@PreAuthorize("hasAnyRole('EMPRESA','COMITE')")`.
- Usuario actual en el controlador: `@AuthenticationPrincipal UsuarioAutenticado actual`.
- Acciones de validación, aprobación o cambio de estado: `actividadService.registrar(...)` (RNF-05).
- Errores de negocio: `throw new NegocioException(HttpStatus.X, "Mensaje claro en español")`.
- En el frontend, agreguen la página al objeto `PAGINAS` en `App.jsx` y quiten la `tarea` del menú en `auth/roles.js`.

### Variables de entorno para el piloto (perfil distinto de `dev`)

`APP_JWT_SECRETO` (Base64, ≥ 256 bits: `openssl rand -base64 48`), `APP_ADMIN_CORREO`, `APP_ADMIN_CONTRASENA`,
`DB_URL`, `DB_USUARIO`, `DB_CONTRASENA`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USUARIO`, `MAIL_CONTRASENA`,
`APP_CORREO_HABILITADO=true`, `APP_FRONTEND_URL`, `APP_CORS_ORIGENES`. Con HTTPS, `APP_COOKIE_SEGURA` queda en `true` (valor por defecto).

## Cómo trabajar en equipo

1. Clona el repositorio:
   ```bash
   git clone https://github.com/jhover-hernandez/practicajic-uraba.git
   cd practicajic-uraba
   ```
2. Antes de empezar, trae los últimos cambios:
   ```bash
   git pull origin main
   ```
3. Crea una rama para tu tarea (una por tarjeta del Kanban):
   ```bash
   git checkout -b feature/modulo-empresas
   ```
4. Guarda y sube tus cambios:
   ```bash
   git add .
   git commit -m "Agrega registro de empresas"
   git push origin feature/modulo-empresas
   ```
5. En GitHub abre un **Pull Request** hacia `main` para que el equipo lo revise antes de unirlo.

No subas contraseñas, archivos `.env` ni la carpeta `node_modules`; el `.gitignore` ya los excluye.


Docente líder de práctica (APIT): Alexis Enrique Cantillo Viera.
