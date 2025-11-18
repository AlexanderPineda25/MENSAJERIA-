CREATE DATABASE IF NOT EXISTS mensajeria_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE mensajeria_db;

-- =======================================
-- 1. usuarios
-- =======================================
CREATE TABLE usuarios (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          username VARCHAR(50) NOT NULL UNIQUE,
                          email VARCHAR(254) NOT NULL UNIQUE,
                          password VARCHAR(120) NOT NULL,
                          estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
                          conectado BOOLEAN NOT NULL DEFAULT FALSE,
                          fecha_ultima_conexion DATETIME,
                          fecha_registro DATETIME NOT NULL DEFAULT NOW(),
                          activo BOOLEAN NOT NULL DEFAULT TRUE,

                          INDEX idx_usuario_estado (estado),
                          INDEX idx_usuario_conectado (conectado),
                          INDEX idx_usuario_registro (fecha_registro)
) ENGINE=InnoDB;

-- =======================================
-- 2. conexiones
-- =======================================
CREATE TABLE conexiones (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            usuario_id BIGINT NOT NULL,
                            direccion_ip VARCHAR(45) NOT NULL,
                            hora_conexion DATETIME NOT NULL,
                            hora_desconexion DATETIME,
                            mensajes_enviados INT NOT NULL DEFAULT 0,
                            activa BOOLEAN NOT NULL DEFAULT TRUE,
                            sistema_operativo VARCHAR(120),
                            dispositivo VARCHAR(120),

                            FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,

                            INDEX idx_conexion_usuario (usuario_id),
                            INDEX idx_conexion_activa (activa),
                            INDEX idx_conexion_hora (hora_conexion)
) ENGINE=InnoDB;

-- =======================================
-- 3. archivos
-- =======================================
CREATE TABLE archivos (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          nombre VARCHAR(255) NOT NULL,
                          ruta VARCHAR(767) NOT NULL UNIQUE,
                          tamano BIGINT NOT NULL DEFAULT 0,
                          tipo VARCHAR(30) NOT NULL,
                          mime VARCHAR(255),
                          propietario_id BIGINT NOT NULL,
                          fecha_subida DATETIME NOT NULL DEFAULT NOW(),
                          duracion INT,
                          formato VARCHAR(50),

                          FOREIGN KEY (propietario_id) REFERENCES usuarios(id) ON DELETE CASCADE,

                          INDEX idx_archivo_propietario (propietario_id),
                          INDEX idx_archivo_tipo (tipo),
                          INDEX idx_archivo_fecha (fecha_subida)
) ENGINE=InnoDB;

-- =======================================
-- 4. mensajes
-- =======================================
CREATE TABLE mensajes (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          remitente_id BIGINT NOT NULL,
                          destinatario_id BIGINT NOT NULL,
                          contenido VARCHAR(4000),
                          tipo VARCHAR(20) NOT NULL,
                          fecha_envio DATETIME NOT NULL DEFAULT NOW(),
                          ip_remitente VARCHAR(45),
                          ip_destinatario VARCHAR(45),
                          leido BOOLEAN NOT NULL DEFAULT FALSE,
                          archivo_id BIGINT,
                          mensaje_padre_id BIGINT,

                          FOREIGN KEY (remitente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                          FOREIGN KEY (destinatario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                          FOREIGN KEY (archivo_id) REFERENCES archivos(id) ON DELETE SET NULL,
                          FOREIGN KEY (mensaje_padre_id) REFERENCES mensajes(id) ON DELETE SET NULL,

                          INDEX idx_mensaje_rem (remitente_id),
                          INDEX idx_mensaje_dest (destinatario_id),
                          INDEX idx_mensaje_fecha (fecha_envio),
                          INDEX idx_mensaje_leido (leido)
) ENGINE=InnoDB;

-- =======================================
-- 5. configuracion_limites
-- =======================================
CREATE TABLE configuracion_limites (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       max_conexiones_usuario INT NOT NULL DEFAULT 3,
                                       max_conexiones_totales INT NOT NULL DEFAULT 100,
                                       max_archivos_usuario INT NOT NULL DEFAULT 100,
                                       max_tamano_archivo BIGINT NOT NULL DEFAULT 52428800,
                                       max_archivos_dia INT NOT NULL DEFAULT 10,
                                       max_audio_bytes BIGINT DEFAULT 10485760,
                                       max_audio_segundos INT DEFAULT 300,
                                       max_mensajes_minuto INT DEFAULT 30,
                                       activa BOOLEAN NOT NULL DEFAULT TRUE,
                                       fecha_actualizacion DATETIME NOT NULL DEFAULT NOW()
) ENGINE=InnoDB;

-- =======================================
-- 6. registro_acciones
-- =======================================
CREATE TABLE registro_acciones (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   tipo VARCHAR(30) NOT NULL,
                                   usuario_id BIGINT,
                                   descripcion TEXT NOT NULL,
                                   fecha DATETIME NOT NULL DEFAULT NOW(),
                                   ip VARCHAR(45),
                                   detalles TEXT,

                                   FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,

                                   INDEX idx_accion_tipo (tipo),
                                   INDEX idx_accion_usuario (usuario_id),
                                   INDEX idx_accion_fecha (fecha)
) ENGINE=InnoDB;

-- =======================================
-- 7. sesiones_activas
-- =======================================
CREATE TABLE sesiones_activas (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  usuario_id BIGINT NOT NULL,
                                  token VARCHAR(255) NOT NULL UNIQUE,
                                  ip VARCHAR(45) NOT NULL,
                                  inicio DATETIME NOT NULL DEFAULT NOW(),
                                  ultima_actividad DATETIME,
                                  activa BOOLEAN NOT NULL DEFAULT TRUE,
                                  aplicacion VARCHAR(50),
                                  version VARCHAR(30),

                                  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,

                                  INDEX idx_sesion_usuario (usuario_id),
                                  INDEX idx_sesion_activa (activa),
                                  INDEX idx_sesion_ultima (ultima_actividad)
) ENGINE=InnoDB;

-- =======================================
-- Datos iniciales
-- =======================================
INSERT INTO configuracion_limites (fecha_actualizacion)
VALUES (NOW());
