CREATE DATABASE IF NOT EXISTS clinica_san_felipe
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE clinica_san_felipe;

CREATE TABLE IF NOT EXISTS usuarios (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(100) NOT NULL,
  correo         VARCHAR(150) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  telefono       VARCHAR(20),
  rol            VARCHAR(20)  NOT NULL DEFAULT 'PACIENTE',
  creado_en      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS servicios (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre            VARCHAR(100)  NOT NULL UNIQUE,
  descripcion       VARCHAR(255),
  duracion_minutos  INT           NOT NULL,
  precio            DECIMAL(8,2)  NOT NULL,
  activo            BOOLEAN       NOT NULL DEFAULT TRUE
);


CREATE TABLE IF NOT EXISTS citas (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id   BIGINT       NOT NULL,
  servicio_id  BIGINT       NOT NULL,
  fecha_hora   DATETIME     NOT NULL,
  estado       VARCHAR(20)  NOT NULL DEFAULT 'PROGRAMADA',
  motivo       VARCHAR(255),
  creado_en    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_citas_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios (id),
  CONSTRAINT fk_citas_servicio FOREIGN KEY (servicio_id) REFERENCES servicios (id)
);
