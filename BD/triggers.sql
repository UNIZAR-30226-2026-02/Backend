--  TRIGGERS - Base de datos

-- Comprueba el aforo 
-- y que nadie pueda añadirse si la partida ya está empezada
CREATE OR REPLACE FUNCTION fn_aforo_y_estado_sala() RETURNS TRIGGER AS $$
DECLARE
    v_max_jugadores INT;
    v_jugadores_actuales INT;
    v_estado_partida VARCHAR;
BEGIN
    SELECT max_jugadores, estado INTO v_max_jugadores, v_estado_partida 
    FROM PARTIDA WHERE id_partida = NEW.id_partida FOR UPDATE;

    IF v_estado_partida != 'esperando' THEN 
        RAISE EXCEPTION 'Error: La partida no está en espera. Estado actual: %', v_estado_partida;
    END IF;

    SELECT COUNT(*) INTO v_jugadores_actuales 
    FROM JUGADOR_PARTIDA WHERE id_partida = NEW.id_partida;

    IF v_jugadores_actuales >= v_max_jugadores THEN
        RAISE EXCEPTION 'Error: La sala está llena. Se ha alcanzado el límite de % jugadores.', v_max_jugadores;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_aforo_y_estado_sala
BEFORE INSERT ON JUGADOR_PARTIDA
FOR EACH ROW EXECUTE FUNCTION fn_aforo_y_estado_sala();


-- Para control al agregar a un amigo
CREATE OR REPLACE FUNCTION fn_amistad_bidireccional() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM AMISTAD 
        WHERE id_solicitante = NEW.id_receptor AND id_receptor = NEW.id_solicitante
    ) THEN
        RAISE EXCEPTION 'Error: Ya existe una solicitud inversa entre estos jugadores.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_amistad_bidireccional
BEFORE INSERT ON AMISTAD
FOR EACH ROW EXECUTE FUNCTION fn_amistad_bidireccional();





-- Cuando se cambia la personalización, la modificamos directamente para que aparezca solo una
CREATE OR REPLACE FUNCTION fn_equipamiento_exclusivo() RETURNS TRIGGER AS $$
DECLARE
    v_tipo_personalizacion VARCHAR;
BEGIN
        SELECT tipo INTO v_tipo_personalizacion FROM PERSONALIZACION WHERE id_personalizacion = NEW.id_personalizacion;
        
        UPDATE INVENTARIO_PERSONALIZACION ip
        SET equipado = false
        FROM PERSONALIZACION p
        WHERE ip.id_personalizacion = p.id_personalizacion 
          AND p.tipo = v_tipo_personalizacion 
          AND ip.id_jugador = NEW.id_jugador 
          AND ip.id_personalizacion != NEW.id_personalizacion;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_equipamiento_exclusivo
BEFORE UPDATE ON INVENTARIO_PERSONALIZACION
FOR EACH ROW WHEN (NEW.equipado = true AND OLD.equipado = false)
EXECUTE FUNCTION fn_equipamiento_exclusivo();


-- Nos aseguramos de que un jugador participe siempre en el chat que le corresponde
CREATE OR REPLACE FUNCTION fn_control_chat_equivocado() RETURNS TRIGGER AS $$
DECLARE
    v_partida_real INT;
BEGIN
    SELECT id_partida INTO v_partida_real FROM JUGADOR_PARTIDA WHERE id_jugador_partida = NEW.id_jugador_partida;
    IF v_partida_real != NEW.id_partida THEN
        RAISE EXCEPTION 'Error: El jugador no pertenece a la partida del chat.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_control_chat_equivocado
BEFORE INSERT OR UPDATE ON CHAT
FOR EACH ROW EXECUTE FUNCTION fn_control_chat_equivocado();


