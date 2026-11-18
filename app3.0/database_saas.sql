-- =========================================================================
-- SCRIPT DE BASE DE DATOS - LOGISTOCK (VERSIÓN SaaS)
-- Añade: planes de suscripción (BASICO/PREMIUM) + aislamiento de datos
-- por usuario (multitenancy básico) en productos, categorías y auditoría.
-- Sigue usando BCryptPasswordEncoder (Spring Security).
-- =========================================================================

DROP DATABASE IF EXISTS logistock_db;
CREATE DATABASE logistock_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE logistock_db;

-- =========================================================================
-- 1. ROLES
-- =========================================================================
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
);

-- =========================================================================
-- 2. USUARIOS (+ columna "plan" para el modelo SaaS)
-- =========================================================================
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    -- Plan de suscripción del usuario. Por defecto todos los usuarios nuevos
    -- entran como BASICO. El Enum en la entidad Java se mapea con @Enumerated(STRING).
    plan VARCHAR(20) NOT NULL DEFAULT 'BASICO',
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_plan CHECK (plan IN ('BASICO', 'PREMIUM'))
);

-- Tabla Pivote (Usuarios - Roles)
CREATE TABLE usuarios_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_ur_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- =========================================================================
-- 3. CATEGORÍAS (aisladas por usuario)
-- =========================================================================
-- usuario_id es NULLABLE a propósito: permite mantener un catálogo de
-- categorías "globales" sembradas por el sistema (usuario_id = NULL) que
-- todos los usuarios pueden usar, además de las categorías privadas que
-- cada usuario cree para sí mismo. El nombre solo debe ser único DENTRO
-- del alcance de cada usuario (o del catálogo global).
CREATE TABLE categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    usuario_id BIGINT NULL,
    CONSTRAINT fk_categoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT uk_categoria_nombre_usuario UNIQUE (usuario_id, nombre)
);

-- =========================================================================
-- 4. PRODUCTOS (aislados por usuario -> multitenancy)
-- =========================================================================
CREATE TABLE productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    cantidad INT NOT NULL DEFAULT 0,
    precio_compra DECIMAL(10,2) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    categoria_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE RESTRICT,
    CONSTRAINT fk_producto_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- =========================================================================
-- 5. AUDITORÍA (relacionada opcionalmente con el usuario que ejecuta la acción)
-- =========================================================================
-- usuario_id es NULLABLE porque algunas acciones de auditoría son generadas
-- por "Sistema" (procesos sin usuario autenticado, ver AuditoriaService).
CREATE TABLE auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    usuario_email VARCHAR(150) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    detalle TEXT NOT NULL,
    usuario_id BIGINT NULL,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- =========================================================================
-- 5.1. SOLICITUDES DE CAMBIO DE PLAN
-- =========================================================================
-- El usuario ya NO cambia su plan directamente: crea una solicitud aquí,
-- que queda en estado PENDIENTE hasta que ROLE_ADMIN la aprueba o rechaza
-- desde /admin/solicitudes (ver SolicitudPlanService).
CREATE TABLE solicitudes_cambio_plan (
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

-- =========================================================================
-- 6. ÍNDICES DE OPTIMIZACIÓN
-- =========================================================================
CREATE INDEX idx_usuario_email ON usuarios(email);
CREATE INDEX idx_producto_categoria ON productos(categoria_id);
CREATE INDEX idx_producto_usuario ON productos(usuario_id);
CREATE INDEX idx_categoria_usuario ON categorias(usuario_id);
CREATE INDEX idx_auditoria_usuario ON auditoria(usuario_id);
CREATE INDEX idx_solicitud_usuario ON solicitudes_cambio_plan(usuario_id);
CREATE INDEX idx_solicitud_estado ON solicitudes_cambio_plan(estado);

-- =========================================================================
-- 7. ROLES BASE
-- =========================================================================
-- IMPORTANTE: El nombre del rol DEBE llevar el prefijo ROLE_
INSERT INTO roles (nombre) VALUES ('ROLE_ADMIN'), ('ROLE_USER');

-- =========================================================================
-- 8. USUARIO ADMINISTRADOR (superadmin del SaaS, sin almacén propio)
-- =========================================================================
-- Hash BCrypt (costo 10, prefijo $2b$) generado y verificado para: admin123
INSERT INTO usuarios (nombre_completo, email, password, activo, plan)
VALUES (
    'Administrador del Sistema',
    'admin@logistock.com',
    '$2b$10$el/J0pUphl.TQgvFxecqjORqrdNoup4V3fMtX9nyjrty8se4wFWSa',
    TRUE,
    'PREMIUM'
);
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (1, 1);

-- =========================================================================
-- 9. USUARIO CLIENTE DE PRUEBA (dueño de su propio almacén, plan BASICO)
-- =========================================================================
INSERT INTO usuarios (nombre_completo, email, password, activo, plan)
VALUES ('Usuario Invitado', 'usuario@logistock.com',
        '$2b$10$el/J0pUphl.TQgvFxecqjORqrdNoup4V3fMtX9nyjrty8se4wFWSa',
        TRUE, 'BASICO');
INSERT INTO usuarios_roles (usuario_id, rol_id) VALUES (2, 2);

-- =========================================================================
-- 10. DATOS DE PRUEBA DEL INVENTARIO (pertenecientes al usuario cliente)
-- =========================================================================
-- Categorías globales del catálogo (usuario_id NULL -> visibles para todos)
INSERT INTO categorias (nombre, usuario_id) VALUES
    ('Componentes', NULL), ('Periféricos', NULL), ('Hardware', NULL), ('Otros', NULL);

INSERT INTO productos (nombre, cantidad, precio_compra, precio_venta, categoria_id, usuario_id)
VALUES ('Disco Duro SSD 1TB', 10, 150.00, 220.00, 1, 2);

SELECT id, email, plan FROM usuarios;
