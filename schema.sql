-- PRACTICAJIC URABÁ · Esquema inicial PostgreSQL
-- Basado en el documento SRS (sección 6) y el diagrama entidad-relación.
-- Ejecutar con: psql -U postgres -d practicajic -f schema.sql

CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    correo          VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(100) NOT NULL,           -- BCrypt (RNF-01)
    rol             VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN','EMPRESA','ESTUDIANTE','COMITE')),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE estudiante (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE RESTRICT,
    documento       VARCHAR(20)  NOT NULL UNIQUE,
    nombres         VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    programa        VARCHAR(120) NOT NULL,
    semestre        SMALLINT     NOT NULL,
    municipio       VARCHAR(60),
    disponibilidad  VARCHAR(60),
    estado          VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                    CHECK (estado IN ('PENDIENTE','VALIDADO','RECHAZADO','INACTIVO'))
);

CREATE TABLE hoja_vida (
    id                   BIGSERIAL PRIMARY KEY,
    estudiante_id        BIGINT NOT NULL REFERENCES estudiante(id) ON DELETE CASCADE,
    archivo              VARCHAR(255) NOT NULL,       -- nombre generado por el sistema (RNF-04)
    version              INTEGER      NOT NULL DEFAULT 1,
    fecha_actualizacion  TIMESTAMP    NOT NULL DEFAULT NOW(),
    activa               BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE portafolio (
    id             BIGSERIAL PRIMARY KEY,
    estudiante_id  BIGINT NOT NULL REFERENCES estudiante(id) ON DELETE CASCADE,
    tipo           VARCHAR(30)  NOT NULL,            -- proyecto, repositorio, certificación
    titulo         VARCHAR(150) NOT NULL,
    url            VARCHAR(255),
    descripcion    TEXT
);

CREATE TABLE competencia (
    id           BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(80) NOT NULL UNIQUE,
    categoria    VARCHAR(10) NOT NULL CHECK (categoria IN ('TECNICA','BLANDA')),
    descripcion  TEXT
);

CREATE TABLE estudiante_competencia (
    id              BIGSERIAL PRIMARY KEY,
    estudiante_id   BIGINT NOT NULL REFERENCES estudiante(id) ON DELETE CASCADE,
    competencia_id  BIGINT NOT NULL REFERENCES competencia(id) ON DELETE RESTRICT,
    nivel           VARCHAR(15) NOT NULL CHECK (nivel IN ('BASICO','INTERMEDIO','AVANZADO')),
    UNIQUE (estudiante_id, competencia_id)
);

CREATE TABLE empresa (
    id                 BIGSERIAL PRIMARY KEY,
    usuario_id         BIGINT NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE RESTRICT,
    nit                VARCHAR(20)  NOT NULL UNIQUE,
    razon_social       VARCHAR(150) NOT NULL,
    nombre_comercial   VARCHAR(150),
    sector             VARCHAR(80),
    tamano             VARCHAR(20)  CHECK (tamano IN ('MICRO','PEQUENA','MEDIANA','GRANDE')),
    municipio          VARCHAR(60)  NOT NULL,
    contacto           VARCHAR(150),
    estado_validacion  VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                       CHECK (estado_validacion IN ('PENDIENTE','VALIDADA','RECHAZADA','INACTIVA'))
);

CREATE TABLE area (
    id      BIGSERIAL PRIMARY KEY,
    nombre  VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE oferta_practica (
    id                 BIGSERIAL PRIMARY KEY,
    empresa_id         BIGINT NOT NULL REFERENCES empresa(id) ON DELETE RESTRICT,
    area_id            BIGINT NOT NULL REFERENCES area(id) ON DELETE RESTRICT,
    titulo             VARCHAR(150) NOT NULL,
    descripcion        TEXT NOT NULL,
    actividades        TEXT NOT NULL,
    modalidad          VARCHAR(15)  NOT NULL CHECK (modalidad IN ('PRESENCIAL','REMOTA','HIBRIDA')),
    municipio          VARCHAR(60)  NOT NULL,
    duracion_meses     SMALLINT     NOT NULL,
    vacantes           SMALLINT     NOT NULL DEFAULT 1,
    incentivo          DECIMAL(12,2),
    tipo_vinculacion   VARCHAR(60),
    fecha_publicacion  DATE,
    fecha_cierre       DATE,
    estado             VARCHAR(25)  NOT NULL DEFAULT 'BORRADOR'
                       CHECK (estado IN ('BORRADOR','EN_REVISION','AJUSTES_SOLICITADOS','APROBADA','CERRADA','RECHAZADA'))
);

CREATE TABLE oferta_competencia (
    id               BIGSERIAL PRIMARY KEY,
    oferta_id        BIGINT NOT NULL REFERENCES oferta_practica(id) ON DELETE CASCADE,
    competencia_id   BIGINT NOT NULL REFERENCES competencia(id) ON DELETE RESTRICT,
    nivel_requerido  VARCHAR(15) NOT NULL CHECK (nivel_requerido IN ('BASICO','INTERMEDIO','AVANZADO')),
    UNIQUE (oferta_id, competencia_id)
);

-- Matriz de evaluación de empresas: 25 + 20 + 20 + 15 + 10 + 10 = 100
CREATE TABLE evaluacion_oferta (
    id                     BIGSERIAL PRIMARY KEY,
    oferta_id              BIGINT NOT NULL REFERENCES oferta_practica(id) ON DELETE RESTRICT,
    evaluador_id           BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    pertinencia            DECIMAL(5,2) NOT NULL CHECK (pertinencia BETWEEN 0 AND 25),
    calidad_plan           DECIMAL(5,2) NOT NULL CHECK (calidad_plan BETWEEN 0 AND 20),
    condiciones            DECIMAL(5,2) NOT NULL CHECK (condiciones BETWEEN 0 AND 20),
    incentivo_economico    DECIMAL(5,2) NOT NULL CHECK (incentivo_economico BETWEEN 0 AND 15),
    acompanamiento         DECIMAL(5,2) NOT NULL CHECK (acompanamiento BETWEEN 0 AND 10),
    experiencia_laboral    DECIMAL(5,2) NOT NULL CHECK (experiencia_laboral BETWEEN 0 AND 10),
    puntaje_total          DECIMAL(5,2) NOT NULL,
    decision               VARCHAR(20)  NOT NULL CHECK (decision IN ('APROBAR','AJUSTES','RECHAZAR')),
    observaciones          TEXT,
    fecha                  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE postulacion (
    id                        BIGSERIAL PRIMARY KEY,
    oferta_id                 BIGINT NOT NULL REFERENCES oferta_practica(id) ON DELETE RESTRICT,
    estudiante_id             BIGINT NOT NULL REFERENCES estudiante(id) ON DELETE RESTRICT,
    fecha                     TIMESTAMP NOT NULL DEFAULT NOW(),
    estado                    VARCHAR(25) NOT NULL DEFAULT 'POSTULADO'
                              CHECK (estado IN ('POSTULADO','EN_REVISION','PRESELECCIONADO','ENTREVISTA',
                                                'SELECCIONADO','NO_SELECCIONADO','PRACTICA_INICIADA','PRACTICA_FINALIZADA')),
    competencias_coincidentes SMALLINT NOT NULL DEFAULT 0,
    UNIQUE (oferta_id, estudiante_id)                 -- RN-06: no postularse dos veces
);

-- Matriz de evaluación del estudiante: 30 + 25 + 15 + 15 + 10 + 5 = 100
CREATE TABLE evaluacion (
    id                     BIGSERIAL PRIMARY KEY,
    postulacion_id         BIGINT NOT NULL REFERENCES postulacion(id) ON DELETE RESTRICT,
    evaluador_id           BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    puntaje_tecnico        DECIMAL(5,2) NOT NULL CHECK (puntaje_tecnico BETWEEN 0 AND 30),
    puntaje_perfil         DECIMAL(5,2) NOT NULL CHECK (puntaje_perfil BETWEEN 0 AND 25),
    puntaje_academico      DECIMAL(5,2) NOT NULL CHECK (puntaje_academico BETWEEN 0 AND 15),
    puntaje_portafolio     DECIMAL(5,2) NOT NULL CHECK (puntaje_portafolio BETWEEN 0 AND 15),
    puntaje_blandas        DECIMAL(5,2) NOT NULL CHECK (puntaje_blandas BETWEEN 0 AND 10),
    puntaje_entrevista     DECIMAL(5,2) NOT NULL CHECK (puntaje_entrevista BETWEEN 0 AND 5),
    puntaje_final          DECIMAL(5,2) NOT NULL,
    concepto               TEXT,
    fecha                  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE practica (
    id                 BIGSERIAL PRIMARY KEY,
    postulacion_id     BIGINT NOT NULL UNIQUE REFERENCES postulacion(id) ON DELETE RESTRICT,
    fecha_inicio       DATE NOT NULL,
    fecha_fin          DATE,
    tipo_vinculacion   VARCHAR(60),
    incentivo          DECIMAL(12,2),
    tutor_empresa      VARCHAR(150),
    tutor_institucion  VARCHAR(150),
    estado             VARCHAR(20) NOT NULL DEFAULT 'INICIADA'
                       CHECK (estado IN ('INICIADA','FINALIZADA','CANCELADA'))
);

CREATE TABLE seguimiento (
    id              BIGSERIAL PRIMARY KEY,
    practica_id     BIGINT NOT NULL REFERENCES practica(id) ON DELETE CASCADE,
    responsable_id  BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    fecha           DATE NOT NULL DEFAULT CURRENT_DATE,
    tipo            VARCHAR(30) NOT NULL,             -- visita, informe, evaluación final
    descripcion     TEXT NOT NULL,
    calificacion    DECIMAL(3,1) CHECK (calificacion BETWEEN 0 AND 5)
);

-- Índices para las búsquedas más frecuentes
CREATE INDEX idx_oferta_estado     ON oferta_practica (estado);
CREATE INDEX idx_oferta_area       ON oferta_practica (area_id);
CREATE INDEX idx_oferta_municipio  ON oferta_practica (municipio);
CREATE INDEX idx_postulacion_est   ON postulacion (estudiante_id);
CREATE INDEX idx_postulacion_estado ON postulacion (estado);
CREATE INDEX idx_seguimiento_prac  ON seguimiento (practica_id);

-- Vista para el panel administrativo (RF-38)
CREATE VIEW v_indicadores AS
SELECT
  (SELECT COUNT(*) FROM empresa    WHERE estado_validacion = 'VALIDADA') AS empresas_validadas,
  (SELECT COUNT(*) FROM estudiante WHERE estado = 'VALIDADO')            AS estudiantes_validados,
  (SELECT COUNT(*) FROM oferta_practica WHERE estado = 'APROBADA')       AS convocatorias_abiertas,
  (SELECT COUNT(*) FROM postulacion)                                     AS postulaciones,
  (SELECT COUNT(*) FROM practica)                                        AS practicas;

-- Datos iniciales (catálogos)
INSERT INTO area (nombre) VALUES
  ('Desarrollo de software'), ('Páginas web'), ('Bases de datos'), ('Soporte tecnológico');

INSERT INTO competencia (nombre, categoria, descripcion) VALUES
  ('Java', 'TECNICA', 'Programación orientada a objetos con Java'),
  ('Spring Boot', 'TECNICA', 'Desarrollo de APIs REST con Spring Boot'),
  ('JavaScript', 'TECNICA', 'Programación web del lado del cliente'),
  ('React', 'TECNICA', 'Construcción de interfaces con React'),
  ('HTML y CSS', 'TECNICA', 'Maquetación y estilos de páginas web'),
  ('Diseño responsive', 'TECNICA', 'Interfaces adaptables a celular y computador'),
  ('WordPress', 'TECNICA', 'Administración de sitios en WordPress'),
  ('SQL', 'TECNICA', 'Consultas y manipulación de datos'),
  ('PostgreSQL', 'TECNICA', 'Diseño y administración de bases PostgreSQL'),
  ('Power BI', 'TECNICA', 'Tableros e informes de datos'),
  ('Git', 'TECNICA', 'Control de versiones con Git y GitHub'),
  ('Linux', 'TECNICA', 'Administración básica de sistemas Linux'),
  ('Redes', 'TECNICA', 'Configuración y soporte de redes'),
  ('Mesa de ayuda', 'TECNICA', 'Atención de incidencias y soporte a usuarios'),
  ('Trabajo en equipo', 'BLANDA', 'Colaboración con otras personas'),
  ('Comunicación', 'BLANDA', 'Expresión clara oral y escrita'),
  ('Responsabilidad', 'BLANDA', 'Cumplimiento de compromisos y tiempos'),
  ('Resolución de problemas', 'BLANDA', 'Análisis y solución de situaciones');
