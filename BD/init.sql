-- ============================================================
--  BASE DE DATOS - Esquema completo PostgreSQL
-- ============================================================

-- ----------------------------------------------------------------
-- JUGADOR
-- ----------------------------------------------------------------
CREATE TABLE jugador (
    id_google            VARCHAR(255) PRIMARY KEY,
    tag                  VARCHAR(100)  NOT NULL UNIQUE,
    foto_perfil          TEXT,
    balas                INT           NOT NULL DEFAULT 0,
    fecha_registro       TIMESTAMP     NOT NULL DEFAULT NOW(),
    activo               BOOLEAN       NOT NULL DEFAULT TRUE,
    -- Estadísticas globales 
    partidas_jugadas     INT           NOT NULL DEFAULT 0,
    victorias            INT           NOT NULL DEFAULT 0,
    num_aciertos         INT           NOT NULL DEFAULT 0,
    num_fallos           INT           NOT NULL DEFAULT 0
);

-- ----------------------------------------------------------------
-- LOGRO
-- ----------------------------------------------------------------
CREATE TABLE logro (
    id_logro             SERIAL        PRIMARY KEY,
    nombre               VARCHAR(128)  NOT NULL UNIQUE,
    descripcion          TEXT,
    tipo                 VARCHAR(64)   NOT NULL CHECK( tipo IN( 'medalla', 'logro' )),          
    estadistica_clave    VARCHAR(64)   NOT NULL,     
    valor_objetivo       INT           NOT NULL,           
    balas_recompensa     INT           NOT NULL,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE
);

-- ----------------------------------------------------------------
-- AMISTAD
-- ----------------------------------------------------------------
CREATE TABLE amistad (
    id_solicitante       VARCHAR(255)  NOT NULL REFERENCES jugador(id_google) ON DELETE CASCADE,
    id_receptor          VARCHAR(255)  NOT NULL REFERENCES jugador(id_google) ON DELETE CASCADE,
    estado               VARCHAR(16)    NOT NULL DEFAULT 'pendiente'
                                        CHECK (estado IN ('pendiente', 'aceptada')),
    fecha_solicitud      TIMESTAMP      NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id_solicitante, id_receptor),

    CHECK ( NOT id_solicitante = id_receptor)
);

-- ----------------------------------------------------------------
-- JUGADOR_LOGRO
-- ----------------------------------------------------------------
CREATE TABLE jugador_logro (
    id_jugador           VARCHAR(255)  NOT NULL REFERENCES jugador(id_google) ON DELETE CASCADE,
    id_logro             INT            NOT NULL REFERENCES logro(id_logro)    ON DELETE CASCADE,
    progreso_actual      INT            NOT NULL,
    completado           BOOLEAN        NOT NULL DEFAULT FALSE,
    fecha_desbloqueo     TIMESTAMP,
    PRIMARY KEY (id_jugador, id_logro)
);

-- ----------------------------------------------------------------
-- PERSONALIZACION
-- ----------------------------------------------------------------
CREATE TABLE personalizacion (
    id_personalizacion   SERIAL        PRIMARY KEY,
    nombre               VARCHAR(128)  NOT NULL UNIQUE,
    descripcion          TEXT,
    precio_bala          INT            NOT NULL,
    tipo                 VARCHAR(64)    NOT NULL CHECK(tipo IN('carta', 'tablero')),          
    valor_visual         TEXT,                  -- URL
    activo               BOOLEAN        NOT NULL DEFAULT TRUE
);

-- ----------------------------------------------------------------
-- INVENTARIO_PERSONALIZACION
-- ----------------------------------------------------------------
CREATE TABLE inventario_personalizacion (
    id_jugador           VARCHAR(255)  NOT NULL REFERENCES jugador(id_google)  ON DELETE CASCADE,
    id_personalizacion   INT            NOT NULL REFERENCES personalizacion(id_personalizacion) ON DELETE CASCADE,
    equipado             BOOLEAN        NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id_jugador, id_personalizacion)
);

-- ----------------------------------------------------------------
-- TEMA
-- ----------------------------------------------------------------
CREATE TABLE tema (
    id_tema              SERIAL        PRIMARY KEY,
    nombre               VARCHAR(128)  NOT NULL UNIQUE,
    descripcion          TEXT,
    precio_balas         INT           NOT NULL ,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE
);

-- ----------------------------------------------------------------
-- INVENTARIO_TEMA
-- ----------------------------------------------------------------
CREATE TABLE inventario_tema (
    id_jugador           VARCHAR(255)  NOT NULL REFERENCES jugador(id_google)  ON DELETE CASCADE,
    id_tema              INT           NOT NULL REFERENCES tema(id_tema)        ON DELETE CASCADE,
    PRIMARY KEY (id_jugador, id_tema)
);

-- ----------------------------------------------------------------
-- PALABRA_TEMA
-- ----------------------------------------------------------------
CREATE TABLE palabra_tema (
    id_palabra           SERIAL        PRIMARY KEY,
    id_tema              INT           NOT NULL REFERENCES tema(id_tema) ON DELETE CASCADE,
    valor                TEXT          NOT NULL,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE
);

-- ----------------------------------------------------------------
-- PARTIDA
-- ----------------------------------------------------------------
CREATE TABLE partida (
    id_partida           SERIAL        PRIMARY KEY,
    codigo_partida       VARCHAR(32)   NOT NULL UNIQUE,  -- Código de invitación
    id_tema              INT           NOT NULL REFERENCES tema(id_tema),
    id_creador           VARCHAR(255) NOT NULL REFERENCES jugador(id_google),
    tiempo_espera        INT           NOT NULL DEFAULT 60 CHECK(tiempo_espera = 30 OR 
                                        tiempo_espera = 60 OR tiempo_espera = 90 OR 
                                        tiempo_espera = 120),   -- segundos
    max_jugadores        INT           NOT NULL DEFAULT 8 CHECK(max_jugadores >= 4 AND max_jugadores <= 16),
    es_publica           BOOLEAN       NOT NULL,
    fecha_creacion       TIMESTAMP     NOT NULL DEFAULT NOW(),
    fecha_fin            TIMESTAMP,
    estado               VARCHAR(32)   NOT NULL DEFAULT 'esperando'
                                       CHECK (estado IN ('esperando', 'en_curso', 'finalizada')),
    rojo_gana            BOOLEAN

);

-- ----------------------------------------------------------------
-- JUGADOR_PARTIDA
-- ----------------------------------------------------------------
CREATE TABLE jugador_partida (
    id_jugador_partida   SERIAL        PRIMARY KEY,
    id_jugador           VARCHAR(255) NOT NULL REFERENCES jugador(id_google) ON DELETE CASCADE,
    id_partida           INT           NOT NULL REFERENCES partida(id_partida) ON DELETE CASCADE,
    equipo               VARCHAR(16)   NOT NULL CHECK (equipo IN ('rojo', 'azul')),
    rol                  VARCHAR(32)   NOT NULL CHECK (rol IN ('lider', 'agente')),
    num_fallos           INT           NOT NULL DEFAULT 0,
    num_aciertos         INT           NOT NULL DEFAULT 0,
    abandono             BOOLEAN       NOT NULL DEFAULT FALSE,
    UNIQUE (id_jugador, id_partida)
);

-- ----------------------------------------------------------------
-- TABLERO_CARTA
-- ----------------------------------------------------------------
CREATE TABLE tablero_carta (
    id_carta_tablero     SERIAL        PRIMARY KEY,
    id_partida           INT           NOT NULL REFERENCES partida(id_partida)  ON DELETE CASCADE,
    id_palabra           INT           NOT NULL REFERENCES palabra_tema(id_palabra),
    fila                 INT           NOT NULL CHECK( fila <= 3 AND fila >= 0 ),
    columna              INT           NOT NULL CHECK( columna <= 4 AND columna >= 0 ),
    estado               VARCHAR(32)   NOT NULL DEFAULT 'oculta'
                                       CHECK (estado IN ('oculta', 'revelada')),
    tipo                 VARCHAR(20)   NOT NULL CHECK( tipo IN('rojo', 'azul', 'civil', 'asesino')),
    UNIQUE (id_partida, fila, columna)
);

-- ----------------------------------------------------------------
-- TURNO
-- ----------------------------------------------------------------
CREATE TABLE turno (
    id_turno             SERIAL        PRIMARY KEY,
    id_partida           INT           NOT NULL REFERENCES partida(id_partida)     ON DELETE CASCADE, -- Desnormalizado para rendimiento
    id_jugador_partida   INT           NOT NULL REFERENCES jugador_partida(id_jugador_partida),
    num_turno            INT           NOT NULL,
    palabra_pista        VARCHAR(256),
    pista_numero         INT,
    aciertos_turno       INT           NOT NULL DEFAULT 0,
    UNIQUE (id_partida, num_turno)
);

-- ----------------------------------------------------------------
-- VOTO_CARTA
-- ----------------------------------------------------------------
CREATE TABLE voto_carta (
    id_voto              SERIAL        PRIMARY KEY,
    id_turno             INT           NOT NULL REFERENCES turno(id_turno)                     ON DELETE CASCADE,
    id_jugador_partida   INT           NOT NULL REFERENCES jugador_partida(id_jugador_partida) ON DELETE CASCADE,
    id_carta_tablero     INT           NOT NULL REFERENCES tablero_carta(id_carta_tablero)     ON DELETE CASCADE,
    id_carta_revelada    INT           REFERENCES tablero_carta(id_carta_tablero)              ON DELETE SET NULL,
    UNIQUE (id_turno, id_jugador_partida, id_carta_tablero)
);

-- ----------------------------------------------------------------
-- CHAT
-- ----------------------------------------------------------------
CREATE TABLE chat (
    id_mensaje           SERIAL        PRIMARY KEY,
    id_partida           INT           NOT NULL REFERENCES partida(id_partida)     ON DELETE CASCADE, 
    id_jugador_partida   INT           NOT NULL REFERENCES jugador_partida(id_jugador_partida) ON DELETE CASCADE,
    mensaje              TEXT          NOT NULL,
    fecha                TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ============================================================
-- DATOS INICIALES POR DEFECTO
-- ============================================================

-- Inserción de temas (RF-11)
INSERT INTO tema (nombre, descripcion, precio_balas, activo) VALUES 
('Basico', 'Tema clásico con palabras comunes', 0, true),
('Magia', 'Un tema místico lleno de magia y fantasía', 100, true),
('Histórico', 'Conceptos épicos de la historia de la humanidad', 100, true),
('Vida submarina', 'Misterios y animales del océano profundo', 100, true),
('Cyberpunk', 'Un futuro oscuro, tecnológico y rebelde', 100, true),
('Naturaleza', 'Fauna, flora natural y paisajes boscosos', 100, true);


-- INSERCIÓN DE LAS IMÁGENES DESDE LA NUBE
DO $$ 
DECLARE 
    t_id INT;
BEGIN
    -- Paquete Básico (carta1.png - carta20.png)
    SELECT id_tema INTO t_id FROM tema WHERE nombre = 'Basico';
    FOR i IN 1..20 LOOP
        INSERT INTO palabra_tema (id_tema, valor) 
        VALUES (t_id, 'https://imagescodenames.blob.core.windows.net/imagenes/cartas/basico/carta' || i || '.png');
    END LOOP;

    -- Paquete Magia (carta_magia1.png - carta_magia20.png)
    SELECT id_tema INTO t_id FROM tema WHERE nombre = 'Magia';
    FOR i IN 1..20 LOOP
        INSERT INTO palabra_tema (id_tema, valor) 
        VALUES (t_id, 'https://imagescodenames.blob.core.windows.net/imagenes/cartas/magia/carta_magia' || i || '.png');
    END LOOP;

    -- Paquete Cyberpunk (carta_cyberpunk1.png - carta_cyberpunk20.png)
    SELECT id_tema INTO t_id FROM tema WHERE nombre = 'Cyberpunk';
    FOR i IN 1..20 LOOP
        INSERT INTO palabra_tema (id_tema, valor) 
        VALUES (t_id, 'https://imagescodenames.blob.core.windows.net/imagenes/cartas/cyber_punk/carta_cyberpunk' || i || '.png');
    END LOOP;

    -- Paquete Histórico (carta_historico1.png - carta_historico20.png)
    SELECT id_tema INTO t_id FROM tema WHERE nombre = 'Histórico';
    FOR i IN 1..20 LOOP
        INSERT INTO palabra_tema (id_tema, valor) 
        VALUES (t_id, 'https://imagescodenames.blob.core.windows.net/imagenes/cartas/hist%C3%B3rico/carta_historico' || i || '.png');
    END LOOP;

    -- Paquete Vida submarina (carta_submarina1.png - carta_submarina20.png)
    SELECT id_tema INTO t_id FROM tema WHERE nombre = 'Vida submarina';
    FOR i IN 1..20 LOOP
        INSERT INTO palabra_tema (id_tema, valor) 
        VALUES (t_id, 'https://imagescodenames.blob.core.windows.net/imagenes/cartas/vida_submarina/carta_submarina' || i || '.png');
    END LOOP;

    -- Paquete Naturaleza (carta_naturaleza1.png - carta_naturaleza20.png)
    SELECT id_tema INTO t_id FROM tema WHERE nombre = 'Naturaleza';
    FOR i IN 1..20 LOOP
        INSERT INTO palabra_tema (id_tema, valor) 
        VALUES (t_id, 'https://imagescodenames.blob.core.windows.net/imagenes/cartas/naturaleza/carta_naturaleza' || i || '.png');
    END LOOP;
END $$;


-- Inserción de Logros y Medallas
INSERT INTO logro (nombre, descripcion, tipo, estadistica_clave, valor_objetivo, balas_recompensa, activo) VALUES 
('Agente principiante', 'Primera partida completada.', 'logro', 'partidas_jugadas', 1, 50, true),
('Agente de entrenamiento', '20 partidas jugadas.', 'logro', 'partidas_jugadas', 20, 50, true),
('Agente oficial', '50 partidas jugadas.', 'logro', 'partidas_jugadas', 50, 50, true),
('Agente inspector', '100 partidas jugadas.', 'logro', 'partidas_jugadas', 100, 50, true),
('Sociable', '5 amigos añadidos.', 'logro', 'amigos_añadidos', 5, 50, true),
('Puntería extrema', 'Acabar una partida sin fallos.', 'logro', 'partidas_sin_fallos', 1, 50, true),
('Fiebre de balas', 'Adquirir todos los paquetes de cartas y temas visuales.', 'logro', 'compras_tienda', 15, 50, true),
('Agente de bronce', '50 partidas ganadas. Insignia de color bronce.', 'medalla', 'victorias', 50, 0, true),
('Agente de plata', '100 partidas ganadas. Insignia de color plateado.', 'medalla', 'victorias', 100, 0, true),
('Agente de oro', '200 partidas ganadas. Insignia de color dorado.', 'medalla', 'victorias', 200, 0, true);

-- Inserción de temas visuales de Carta y Tablero (según fichero ids_y_mas.md de Gestión)
INSERT INTO personalizacion (nombre, descripcion, precio_bala, tipo, valor_visual, activo) VALUES
('gold', 'Fondo de tablero amarillo (oro envejecido).', 50, 'tablero', 'd4af37', true),
('sage', 'Fondo de tablero verde (verde salvia).', 50, 'tablero', '8a9a5b', true),
('terracotta', 'Fondo de tablero naranja (terracota cálida).', 50, 'tablero', 'c65d3b', true),
('purple', 'Fondo de tablero morado (púrpura real).', 50, 'tablero', '8b5a8b', true),
('rose', 'Fondo de tablero rosa (cuarzo rosa).', 50, 'tablero', 'c67b8a', true),
('gold', 'Marco de cartas amarillo (oro envejecido).', 50, 'carta', 'd4af37', true),
('sage', 'Marco de cartas verde (verde salvia).', 50, 'carta', '8a9a5b', true),
('terracotta', 'Marco de cartas naranja (terracota cálida).', 50, 'carta', 'c65d3b', true),
('purple', 'Marco de cartas morado (púrpura real).', 50, 'carta', '8b5a8b', true),
('rose', 'Marco de cartas rosa (cuarzo rosa).', 50, 'carta', 'c67b8a', true);
