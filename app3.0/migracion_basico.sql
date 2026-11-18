-- =========================================================================
-- MIGRACIÓN: corrige el desfase entre la base de datos y el código actual
-- =========================================================================
-- DIAGNÓSTICO: tu base de datos todavía tiene filas con plan = 'FREE'
-- (y el CHECK constraint todavía permite 'FREE'), pero el enum Java
-- Plan.java SOLO reconoce BASICO y PREMIUM. Como Usuario.plan usa
-- @Enumerated(EnumType.STRING), Hibernate intenta convertir el texto de
-- la columna al enum en CADA lectura de esa fila; con 'FREE' eso lanza
-- IllegalArgumentException: No enum constant Plan.FREE, y la petición
-- que haya disparado esa lectura termina en una página de error genérica
-- (ni siquiera pasa por tus @ExceptionHandler, porque GlobalExceptionHandler
-- solo cubre el paquete com.logistock.app.controller, no WebController).
-- Esto NO borra tu base de datos: solo corrige los datos y el constraint.
-- Es seguro ejecutarlo aunque ya hayas insertado admin2@logistock.com.
-- =========================================================================

USE logistock_db;

-- 1) Actualiza cualquier usuario que haya quedado con el valor obsoleto.
UPDATE usuarios SET plan = 'BASICO' WHERE plan = 'FREE';

-- 2) Sincroniza el CHECK constraint con el enum real (MySQL 8+).
--    Si tu constraint tiene otro nombre, ajústalo con:
--    SHOW CREATE TABLE usuarios;
ALTER TABLE usuarios DROP CHECK ck_usuario_plan;
ALTER TABLE usuarios ADD CONSTRAINT ck_usuario_plan CHECK (plan IN ('BASICO', 'PREMIUM'));

-- 3) Verificación: no debe quedar ninguna fila fuera de (BASICO, PREMIUM).
SELECT id, email, plan FROM usuarios WHERE plan NOT IN ('BASICO', 'PREMIUM');
-- Si esta consulta devuelve filas, corrígelas manualmente antes de continuar.

-- 4) Confirmación final del estado de todas las cuentas.
SELECT u.id, u.email, u.plan, u.activo, GROUP_CONCAT(r.nombre) AS roles
FROM usuarios u
LEFT JOIN usuarios_roles ur ON ur.usuario_id = u.id
LEFT JOIN roles r ON r.id = ur.rol_id
GROUP BY u.id, u.email, u.plan, u.activo;
