-- Tareas 10 a 14 · Empresas, estudiantes, convocatorias, comité y seguimiento
-- Completa el esquema inicial con lo que piden RF-06 a RF-40 y RNF-04.

-- RF-07 y RF-16: motivo que recibe la empresa o el estudiante cuando el registro se rechaza
ALTER TABLE empresa    ADD COLUMN motivo_rechazo TEXT;
ALTER TABLE estudiante ADD COLUMN motivo_rechazo TEXT;

-- RNF-04: el archivo se guarda con nombre generado; se conserva el original solo para mostrarlo
ALTER TABLE hoja_vida ADD COLUMN nombre_original VARCHAR(255);
ALTER TABLE hoja_vida ADD COLUMN tamano_bytes    BIGINT;

-- RF-08 y RF-09: perfil buscado y horario de la necesidad
ALTER TABLE oferta_practica ADD COLUMN perfil  TEXT;
ALTER TABLE oferta_practica ADD COLUMN horario VARCHAR(100);
-- RF-30: última observación del comité, la que ve la empresa
ALTER TABLE oferta_practica ADD COLUMN observaciones_comite TEXT;
ALTER TABLE oferta_practica ADD COLUMN fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW();
-- RF-34: evaluador asignado por el administrador
ALTER TABLE oferta_practica ADD COLUMN evaluador_id BIGINT;
ALTER TABLE oferta_practica ADD CONSTRAINT fk_oferta_evaluador
    FOREIGN KEY (evaluador_id) REFERENCES usuario(id) ON DELETE SET NULL;

-- RF-34: evaluador del candidato
ALTER TABLE postulacion ADD COLUMN evaluador_id BIGINT;
ALTER TABLE postulacion ADD CONSTRAINT fk_postulacion_evaluador
    FOREIGN KEY (evaluador_id) REFERENCES usuario(id) ON DELETE SET NULL;
-- RF-26: entrevista programada por la empresa
ALTER TABLE postulacion ADD COLUMN fecha_entrevista TIMESTAMP;
ALTER TABLE postulacion ADD COLUMN lugar_entrevista VARCHAR(200);
-- Nota de la empresa o del comité en el último cambio de estado
ALTER TABLE postulacion ADD COLUMN observaciones TEXT;
-- RF-27: momento en que el estudiante acepta la práctica ofrecida
ALTER TABLE postulacion ADD COLUMN fecha_aceptacion TIMESTAMP;

-- RF-32: el comité recomienda o no al candidato
ALTER TABLE evaluacion ADD COLUMN recomendado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE evaluacion ADD CONSTRAINT uq_evaluacion_evaluador UNIQUE (postulacion_id, evaluador_id);

CREATE INDEX idx_oferta_empresa     ON oferta_practica (empresa_id);
CREATE INDEX idx_postulacion_oferta ON postulacion (oferta_id);
CREATE INDEX idx_hoja_vida_est      ON hoja_vida (estudiante_id);
