-- =========================================================================
-- MIGRACIÓN: agrega la tabla de solicitudes de cambio de plan
-- =========================================================================
-- Úsalo si ya tienes una base de datos MySQL existente (perfil "mysql",
-- ddl-auto=validate) y no quieres perder los datos actuales. Si en cambio
-- usas el script completo database_saas.sql (DROP/CREATE), no necesitas
-- correr esto: ya viene incluido ahí.
-- =========================================================================

USE logistock_db;

CREATE TABLE IF NOT EXISTS solicitudes_cambio_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    plan_actual VARCHAR(20) NOT NULL,
    plan_solicitado VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion DATETIME NULL,
    CONSTRAINT fk_solicitud_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT ck_solicitud_plan_actual CHECK (plan_actual IN ('BASICO', 'PREMIUM')),
    CONSTRAINT ck_solicitud_plan_solicitado CHECK (plan_solicitado IN ('BASICO', 'PREMIUM')),
    CONSTRAINT ck_solicitud_estado CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA'))
);

CREATE INDEX idx_solicitud_usuario ON solicitudes_cambio_plan(usuario_id);
CREATE INDEX idx_solicitud_estado ON solicitudes_cambio_plan(estado);
