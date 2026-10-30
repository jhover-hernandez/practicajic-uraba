-- Tarea 9 · Autenticación y roles
-- Complementa el esquema inicial con lo que piden RF-01 a RF-05 y RNF-01, RNF-02, RNF-05 y RNF-06.

-- Nombre visible (necesario para administradores y miembros del comité, que no tienen perfil propio)
ALTER TABLE usuario ADD COLUMN nombre VARCHAR(150);

-- RNF-06: aceptación explícita de la política de tratamiento de datos (Ley 1581 de 2012)
ALTER TABLE usuario ADD COLUMN fecha_aceptacion_politica TIMESTAMP;

-- RNF-02: tokens de renovación de sesión. Solo se guarda el hash SHA-256, nunca el token.
CREATE TABLE refresh_token (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT      NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token_hash      VARCHAR(64) NOT NULL UNIQUE,
    expira          TIMESTAMP   NOT NULL,
    revocado        BOOLEAN     NOT NULL DEFAULT FALSE,
    fecha_creacion  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- RF-03: enlaces de recuperación de contraseña (un solo uso, con vencimiento)
CREATE TABLE token_recuperacion (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT      NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token_hash      VARCHAR(64) NOT NULL UNIQUE,
    expira          TIMESTAMP   NOT NULL,
    usado           BOOLEAN     NOT NULL DEFAULT FALSE,
    fecha_creacion  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- RNF-05: registro de actividades (quién, qué, cuándo)
CREATE TABLE registro_actividad (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      REFERENCES usuario(id) ON DELETE SET NULL,   -- quién (NULL = sistema)
    accion      VARCHAR(50) NOT NULL,                                   -- qué
    entidad     VARCHAR(50) NOT NULL,
    entidad_id  BIGINT,
    detalle     TEXT,
    fecha       TIMESTAMP   NOT NULL DEFAULT NOW()                      -- cuándo
);

CREATE INDEX idx_refresh_token_usuario  ON refresh_token (usuario_id);
CREATE INDEX idx_token_recup_usuario    ON token_recuperacion (usuario_id);
CREATE INDEX idx_actividad_fecha        ON registro_actividad (fecha);
