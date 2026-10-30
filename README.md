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
├── database/    schema.sql con las 15 tablas de PostgreSQL
├── frontend/    Aplicación React (por crear)
└── backend/     API Spring Boot (por crear)
```

## Roles del sistema

- **Estudiante:** perfil digital, hoja de vida, competencias, búsqueda de convocatorias y postulaciones.
- **Empresa:** registro, publicación de ofertas y gestión de postulantes.
- **Comité evaluador:** califica ofertas y candidatos con matrices de 100 puntos.
- **Administrador institucional:** valida usuarios, asigna evaluadores, formaliza prácticas y consulta indicadores.

## Ver el prototipo

Descarga el repositorio y abre `prototipo/index.html` con doble clic. Usa la barra verde superior para cambiar de pantalla.

## Crear la base de datos

```bash
createdb -U postgres practicajic
psql -U postgres -d practicajic -f database/schema.sql
```

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
