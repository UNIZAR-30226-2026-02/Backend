-- Comprueba el aforo 
-- y que nadie pueda añadirse si la partida ya está empezada
CREATE OR REPLACE FUNCTION fn_aforo_y_estado_sala() RETURNS TRIGGER AS $$
DECLARE
    v_max_jugadores INT;
    v_jugadores_actuales INT;
    v_estado_partida VARCHAR;
BEGIN
    SELECT max_jugadores, estado INTO v_max_jugadores, v_estado_partida 
    FROM PARTIDA WHERE id_partida = NEW.id_partida FOR UPDATE; -- Lo miramos cada vez que se actualice la fila (se añadan jugadores)

    -- Solo puede meterse si la partida está en espera (en principio será así, pero de todas formas lo reforzamos para proteger la base de datos)
    IF v_estado_partida != 'esperando' THEN 
        RAISE EXCEPTION 'Error: La partida no está en espera. Estado actual: %', v_estado_partida;
    END IF;

    -- Contamos el número de jugadores relacionados con esa partida
    SELECT COUNT(*) INTO v_jugadores_actuales 
    FROM JUGADOR_PARTIDA WHERE id_partida = NEW.id_partida;

    IF v_jugadores_actuales >= v_max_jugadores THEN
        RAISE EXCEPTION 'Error: La sala está llena. Se ha alcanzado el límite de % jugadores.', v_max_jugadores;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Cuando se añada un jugador a una partida
CREATE TRIGGER trg_aforo_y_estado_sala
BEFORE INSERT ON JUGADOR_PARTIDA
FOR EACH ROW EXECUTE FUNCTION fn_aforo_y_estado_sala();


-- Para control al agregar a un amigo
CREATE OR REPLACE FUNCTION fn_amistad_bidireccional() RETURNS TRIGGER AS $$
BEGIN
    -- En caso de que dos jugadores intenten agregarse a la vez (o sin aceptar primero la solicitud del otro)
    IF EXISTS (
        SELECT 1 FROM AMISTAD 
        WHERE id_solicitante = NEW.id_receptor AND id_receptor = NEW.id_solicitante
    ) THEN
        -- No vamos a permitirlo (deberá ir a solicitudes pendientes y aceptarlas)
        RAISE EXCEPTION 'Error: Ya existe una solicitud inversa entre estos jugadores.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- CAda vez que se quiera añadir una relación/petición de amistad dentro del juego 
CREATE TRIGGER trg_amistad_bidireccional
BEFORE INSERT ON AMISTAD
FOR EACH ROW EXECUTE FUNCTION fn_amistad_bidireccional();


-- Para controlar las votaciones
CREATE OR REPLACE FUNCTION fn_validacion_voto() RETURNS TRIGGER AS $$
DECLARE
    v_partida_turno INT;
    v_partida_carta INT;
    v_estado_carta VARCHAR;
    v_estado_partida VARCHAR;
    v_jugador_partida_real INT;
    v_rol_jugador VARCHAR;
BEGIN
    -- Cogemos la partida en cuestión y la carta con su estado
    SELECT id_partida INTO v_partida_turno FROM TURNO WHERE id_turno = NEW.id_turno;
    SELECT id_partida, estado INTO v_partida_carta, v_estado_carta FROM TABLERO_CARTA WHERE id_carta_tablero = NEW.id_carta_tablero;
    
    -- Nos aseguramos de que estamos modificando una carta de esa partida
    IF v_partida_turno != v_partida_carta THEN
        RAISE EXCEPTION 'Error: El turno y la carta pertenecen a partidas distintas.';
    END IF;

    -- No permitimos que se revele una carta que ya ha sido revelada en esa partida
    IF v_estado_carta != 'oculta' THEN
        RAISE EXCEPTION 'Error: Voto nulo. La carta seleccionada ya ha sido revelada (Estado: %).', v_estado_carta;
    END IF;

    -- No se puede revelar una carta de una partida que no está en curso (está en espera o ya ha terminado)
    SELECT estado INTO v_estado_partida FROM PARTIDA WHERE id_partida = v_partida_turno;
    IF v_estado_partida IN ('esperando', 'finalizada') THEN
        RAISE EXCEPTION 'Error: Voto nulo. La partida está %.', v_estado_partida;
    END IF;

    -- Cogemos a qué partida pertenece el jugador que vota y su rol
    SELECT id_partida, rol INTO v_jugador_partida_real, v_rol_jugador 
    FROM JUGADOR_PARTIDA 
    WHERE id_jugador_partida = NEW.id_jugador_partida;

    -- Nos aseguramos de que el jugador que emite el voto pertenece realmente a esta partida
    IF v_jugador_partida_real != v_partida_turno THEN
        RAISE EXCEPTION 'Error: El jugador que emite el voto no pertenece a esta partida.';
    END IF;

    -- Validamos que solo los agentes pueden votar
    IF v_rol_jugador != 'agente' THEN
        RAISE EXCEPTION 'Error: Voto nulo. Solo los jugadores con rol "agente" pueden destapar cartas.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Cada vez que se vota una carta (es muy importante proteger la base de datos con esto porque es la aprte más compleja)
CREATE TRIGGER trg_validacion_voto
BEFORE INSERT ON VOTO_CARTA
FOR EACH ROW EXECUTE FUNCTION fn_validacion_voto();


-- Cuando se cambia la personalización, la modificamos directamente para que aparezca solo una
CREATE OR REPLACE FUNCTION fn_equipamiento_exclusivo() RETURNS TRIGGER AS $$
DECLARE
    v_tipo_personalizacion VARCHAR;
BEGIN
        -- Obtenemos el tipo del elemento a equipar
        SELECT tipo INTO v_tipo_personalizacion FROM PERSONALIZACION WHERE id_personalizacion = NEW.id_personalizacion;
        
        -- Quitamos los demás del mismo tipo para que solo haya uno
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

-- Cada vez que cambiamos la personalización activa
CREATE TRIGGER trg_equipamiento_exclusivo
BEFORE UPDATE ON INVENTARIO_PERSONALIZACION
FOR EACH ROW WHEN (NEW.equipado = true AND OLD.equipado = false)
EXECUTE FUNCTION fn_equipamiento_exclusivo();


-- Nos aseguramos de que un jugador participe siempre en el chat que le corresponde (que no se nos cruce)
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

-- Lo revisamos cada vez que alguien escribe en el chat
CREATE TRIGGER trg_control_chat_equivocado
BEFORE INSERT OR UPDATE ON CHAT
FOR EACH ROW EXECUTE FUNCTION fn_control_chat_equivocado();


-- Automatizamos las estadísticas de partida básicas (partidas jugadas y victorias) y balas conseguidas
CREATE OR REPLACE FUNCTION fn_estadisticas_globales() RETURNS TRIGGER AS $$
BEGIN   
    UPDATE jugador j
    SET partidas_jugadas = j.partidas_jugadas + 1,
        
        -- Qué hacemos con los aciertos y los fallos?
        num_aciertos = j.num_aciertos + jp.num_aciertos,
        num_fallos = j.num_fallos + jp.num_fallos,
        
        victorias = j.victorias + CASE 
            WHEN (NEW.rojo_gana = true AND jp.equipo = 'rojo') 
              OR (NEW.rojo_gana = false AND jp.equipo = 'azul') THEN 1 
            ELSE 0 
        END,
        
        balas = j.balas + CASE 
            WHEN (NEW.rojo_gana = true AND jp.equipo = 'rojo') 
              OR (NEW.rojo_gana = false AND jp.equipo = 'azul') THEN 20 
            ELSE 10 
        END
        
    FROM jugador_partida jp
    WHERE j.id_google = jp.id_jugador 
      AND jp.id_partida = NEW.id_partida
      AND jp.abandono = FALSE; -- ELIMINAMOS RECOMPENSAS PARA LOS QUE ABANDONAN

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Cada vez que modificamos la tabla partida revisamos si ha sido finalizada y actualizamos (si aún no se ha terminado, no hace falta) hacer la llamada.
CREATE TRIGGER trg_estadisticas_globales
AFTER UPDATE ON PARTIDA
FOR EACH ROW WHEN (NEW.estado = 'finalizada' AND OLD.estado != 'finalizada')
EXECUTE FUNCTION fn_estadisticas_globales();


-- Validación para la integridad del turno
CREATE OR REPLACE FUNCTION fn_control_turno() RETURNS TRIGGER AS $$
DECLARE
    v_rol VARCHAR;
    v_estado_partida VARCHAR;
    v_id_partida_real INT;
BEGIN
    -- Obtenemos el rol del jugador, el estado de la partida y a qué partida pertenece en una sola consulta para optimizar
    SELECT jp.rol, p.estado, jp.id_partida INTO v_rol, v_estado_partida, v_id_partida_real 
    FROM JUGADOR_PARTIDA jp
    JOIN PARTIDA p ON p.id_partida = jp.id_partida
    WHERE jp.id_jugador_partida = NEW.id_jugador_partida;

    -- Comprobamos que esté en alguna partida (que se le haya asignado un rol)
    IF v_rol IS NULL THEN
        RAISE EXCEPTION 'Error: El jugador no forma parte de ninguna partida.';
    END IF;

    -- Comprobamos que el jugador pertenece a la partida correspondiente (y no a otra)
    IF v_id_partida_real != NEW.id_partida THEN
        RAISE EXCEPTION 'Error: Inconsistencia en turno. El jugador no forma parte de la partida %.', NEW.id_partida;
    END IF;

    -- Comprobamos que solo el líder inicie el turno y dé pistas
    IF v_rol != 'lider' THEN
        RAISE EXCEPTION 'Error: Solo el jugador con rol "lider" puede iniciar un turno y dar pistas.';
    END IF;

    -- Comprobamos que la partida está activa
    IF v_estado_partida != 'en_curso' THEN
        RAISE EXCEPTION 'Error: No se pueden crear turnos porque la partida está %.', v_estado_partida;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_control_turno
BEFORE INSERT ON TURNO
FOR EACH ROW EXECUTE FUNCTION fn_control_turno();


-- Cuando se definan los logros que van a haber, podríamos hacer un trigger para mantener las estadísticas cosistentes y sincronizadas con los logros.