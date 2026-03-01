-- Muchos de estos triggers protegen la base de datos de situaciones que, en principio y según como se va a desarrollar la lógica del juego, 
-- no van a suceder. No obstante los añadimos para asegurarnos de que la base de datos no se modifique incorrectamente.
-- Además, en caso de llevarlas a cabo por error, de esta forma las detectaremos mucho antes.


-- Comprueba el aforo (check en la tabla sería mejor maybe? lo pongo por si acaso) 
-- y que nadie pueda añadirse si la partida ya está empezada
CREATE OR REPLACE FUNCTION fn_aforo_y_estado_sala() RETURNS TRIGGER AS $$
DECLARE
    v_max_jugadores INT;
    v_jugadores_actuales INT;
    v_estado_partida VARCHAR;
BEGIN
    SELECT maxJugadores, estado INTO v_max_jugadores, v_estado_partida 
    FROM PARTIDA WHERE id_partida = NEW.id_partida FOR UPDATE; -- Lo miramos cada vez que se actualice la fila (se añadan jugadores)

    -- Solo puede meterse si la partida está en espera (en principio será así, pero de todas formas lo reforzamos para proteger la base de datos)
    IF v_estado_partida != 'espera' THEN 
        RAISE EXCEPTION 'Error: La partida no está en espera. Estado actual: %', v_estado_partida;
    END IF;

    -- Esto bastaría con un check?? REVISAR
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
    -- No puedes agregarte a ti mismo (en principio no habrá opción posible, pero por si acaso)
    IF NEW.id_solicitante = NEW.id_receptor THEN
        RAISE EXCEPTION 'Error: No puedes añadirte a ti mismo como amigo.';
    END IF;

    -- En caso de que dos jugadores intenten agregarse a la vez (o sin aceptar primero la solicitud del otro)
    IF EXISTS (
        SELECT 1 FROM AMISTAD 
        WHERE id_solicitante = NEW.id_receptor AND id_receptor = NEW.id_solicitante
    ) THEN
        -- De momento no vamos a permitirlo (deberá ir a solicitudes pendientes y aceptarlas)
        -- Pero podemos hacer que se acepten directamente (no suele ser habitual creo) REVISAR
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
CREATE OR REPLACE FUNCTION fn_validacion_integral_voto() RETURNS TRIGGER AS $$
DECLARE
    v_partida_turno INT;
    v_partida_carta INT;
    v_estado_carta VARCHAR;
    v_estado_partida VARCHAR;
BEGIN
    -- Cogemos la partida en cuestión y la carta con su estado
    SELECT id_partida INTO v_partida_turno FROM TURNO WHERE id_turno = NEW.id_turno;
    SELECT id_partida, estado INTO v_partida_carta, v_estado_carta FROM TABLERO_CARTA WHERE id_carta_tablero = NEW.id_carta_tablero;
    
    -- Nos aseguramos de que estamos modificando una carta de esa partida
    IF v_partida_turno != v_partida_carta THEN
        RAISE EXCEPTION 'Error: El turno y la carta pertenecen a partidas distintas.';
    END IF;

    -- No pwermitimos que se revele una carta que ya ha sido revelada en esa partida
    IF v_estado_carta != 'oculta' THEN
        RAISE EXCEPTION 'Error: Voto nulo. La carta seleccionada ya ha sido revelada (Estado: %).', v_estado_carta;
    END IF;

    -- No se puede revelar una carta de una partida que no está en curso (está en espera o ya ha terminado)
    SELECT estado INTO v_estado_partida FROM PARTIDA WHERE id_partida = v_partida_turno;
    IF v_estado_partida IN ('espera', 'finalizada') THEN
        RAISE EXCEPTION 'Error: Voto nulo. La partida está %.', v_estado_partida;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Cada vez que se vota una carta (es muy importante proteger la base de datos con esto porque es la aprte más compleja)
CREATE TRIGGER trg_validacion_integral_voto
BEFORE INSERT ON VOTO_CARTA
FOR EACH ROW EXECUTE FUNCTION fn_validacion_integral_voto();


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


-- Automatización del revelado de cartas
CREATE OR REPLACE FUNCTION fn_actions_votos() RETURNS TRIGGER AS $$
DECLARE
    v_pista_numero INT;
    v_votos_emitidos INT;
BEGIN
    --Asumimos que inicialmente se va a revelar una carta que estaba oculta (tenemos un trigger para eso así que en principio ningún problema)
    -- Y la cambiamos directamente a carta revelada. 

    -- Haría falta más lógica para saber el color con el que se revela o es irrelevante?? Porque el color no va a cambiar
    -- Algo que se me acaba de ocurrir, lo apunto aquí por si acaso, habría que poner que para cada partida, el color de la carta fuese único (UNIQUE en la tabla) 
    -- El apdate de la fila se haría solo si la carta tiene mayoría de votos (eso se debería comprobar fuera) REVISAR
    UPDATE TABLERO_CARTA 
    SET estado = REPLACE(estado, 'oculta', 'revelada')
    WHERE id_carta_tablero = NEW.id_carta_tablero;

    -- No sé si me estoy liando con los turnos y los votos en las tablas.
    -- En principio no debería permitir más turnos de los que dice la pista. 
    -- Se puede hacer con un trigger o hace falta más lógica (algún contador) y mejor dejarlo fuera? REVISAR
    SELECT pistaNumero INTO v_pista_numero FROM TURNO WHERE id_turno = NEW.id_turno;
    SELECT COUNT(*) INTO v_votos_emitidos FROM VOTO_CARTA WHERE id_turno = NEW.id_turno;
    -- O deberíamos controlar que el número de votos recibidos por una carta no sea nunca superior al número de integrantes de un equipo.
    -- ESto mejor hacerlo BEFORE insert o update que after, porque no debería permitirlo de primeras.
    
    IF v_votos_emitidos > (v_pista_numero + 1) THEN
        RAISE EXCEPTION 'Error: ... TRIGGER SIN TERMINAR (fn_actions_votos).', v_pista_numero, v_pista_numero + 1;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Cada vez que se vota una carta... Muchas dudas aquí
CREATE TRIGGER trg_actions_votos
AFTER INSERT ON VOTO_CARTA
FOR EACH ROW EXECUTE FUNCTION fn_actions_votos();


-- Comprobamos la condición de vistoria automáticamente
CREATE OR REPLACE FUNCTION fn_check_condicion_victoria() RETURNS TRIGGER AS $$
DECLARE
    v_cartas_rojas_ocultas INT;
    v_cartas_azules_ocultas INT;
BEGIN
    -- Si la carta revelada es el asesino
    IF NEW.estado = 'revelada_asesino' THEN
        -- De momento solo marcamos automáticamente la partida como finalizada. 
        -- Podríamos poner automáticamente quién ha ganado mirando quién ha revelado al asesino? REVISAR
        UPDATE PARTIDA SET estado = 'finalizada' WHERE id_partida = NEW.id_partida;
        RETURN NEW;
    END IF;

    -- Contamos las cartas restantes de cada equipo
    SELECT COUNT(*) INTO v_cartas_rojas_ocultas FROM TABLERO_CARTA WHERE id_partida = NEW.id_partida AND estado = 'oculta_rojo';
    SELECT COUNT(*) INTO v_cartas_azules_ocultas FROM TABLERO_CARTA WHERE id_partida = NEW.id_partida AND estado = 'oculta_azul';

    -- En el momento en el que algún equipo las haya revelado todas, se termina la partida y establece como ganador
    IF v_cartas_rojas_ocultas = 0 THEN
        UPDATE PARTIDA SET estado = 'finalizada', rojoGana = true WHERE id_partida = NEW.id_partida;
    ELSIF v_cartas_azules_ocultas = 0 THEN
        UPDATE PARTIDA SET estado = 'finalizada', rojoGana = false WHERE id_partida = NEW.id_partida;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Cada vez que modificamos el estado de una carta (oculta o revelada) miramos si podemos terminar la partida
CREATE TRIGGER trg_check_condicion_victoria
AFTER UPDATE ON TABLERO_CARTA
FOR EACH ROW WHEN (NEW.estado != OLD.estado)
EXECUTE FUNCTION fn_check_condicion_victoria();


-- Automatizamos las estadísticas de partida básicas (partidas jugadas y victorias) y balas conseguidas
CREATE OR REPLACE FUNCTION fn_estadisticas_globales() RETURNS TRIGGER AS $$
BEGIN   
        -- Actualizamos las partidas jugadas de todos los participantes (+1)
        -- Si un jugador se va de la partida, se quita del registro de la partida??? Porque si no, esto le da igualmente un +1
        -- Eso podríamos hacerlo con otro trigger o no guardamos registro de los jugadores desconectados (simplemente les penalizamos pero no lo registramos)? REVISAR
        UPDATE JUGADOR j
        SET partidas_jugadas = j.partidas_jugadas + 1, 
            balas = j.balas + 10 -- Añadimos las 10 balas que ganan si pierden ya, y luego a los que ganen les daremos 10 más
        FROM JUGADOR_PARTIDA jp
        WHERE j.id_google = jp.id_jugador AND jp.id_partida = NEW.id_partida;

        -- Actualización solo para los que ganan (+1 victoria y balas)
        -- En principio son 20 balas por victoria (se puede ajustar según los precios que pongamos en la tienda). Ya hemos sumado 10 antes, solo falta el resto
        UPDATE JUGADOR j
        SET victorias = j.victorias + 1,
            balas = j.balas + 10
        FROM JUGADOR_PARTIDA jp
        WHERE j.id_google = jp.id_jugador 
          AND jp.id_partida = NEW.id_partida
          AND ((NEW.rojoGana = true AND jp.equipo = 'rojo') OR (NEW.rojoGana = false AND jp.equipo = 'azul'));

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Cada vez que modificamos la tabla partida revisamos si ha sido finalizada y actualizamos (si aún no se ha terminado, no hace falta) hacer la llamada.
-- Haría falta mirar el OLD.estado = 'finalizada' (creo que no, en cuanto se cambie basta con actualizarlo y ya está) REVISAR
CREATE TRIGGER trg_estadisticas_globales
AFTER UPDATE ON PARTIDA
FOR EACH ROW WHEN IF NEW.estado = 'finalizada'
EXECUTE FUNCTION fn_estadisticas_globales();


-- Control de los logros de los jugadores
CREATE OR REPLACE FUNCTION fn_actualizar_progreso_logros() RETURNS TRIGGER AS $$
BEGIN
    -- Comprobamos los logros de victorias
    IF NEW.victorias > OLD.victorias THEN
        INSERT INTO JUGADOR_LOGRO (id_jugador, id_logro, progreso_actual, completado, fecha_desbloqueo)
        SELECT NEW.id_google, l.id_logro, NEW.victorias, (NEW.victorias >= l.valor_objetivo), CURRENT_TIMESTAMP
        FROM LOGRO l
        WHERE l.estadistica_clave = 'victorias' 
          AND NEW.victorias >= l.valor_objetivo
        ON CONFLICT (id_jugador, id_logro) 
        DO UPDATE SET progreso_actual = EXCLUDED.progreso_actual, 
                      completado = EXCLUDED.completado, 
                      fecha_desbloqueo = CASE WHEN EXCLUDED.completado THEN CURRENT_TIMESTAMP ELSE NULL END
        WHERE JUGADOR_LOGRO.completado = false; 
        -- Solo actualiza si no estaba completado ya. 
        -- Añadimos aquí también que añada balas al jugador cuando consigue el logro? Habría que mirar cuántas balas da cada logro y sumarlas. REVISAR
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Cada vez que aumentamos el número de vistorias 
-- Separar por número de partidas en general? (hay logro por partidas jugadas?) REVISAR
CREATE TRIGGER trg_actualizar_progreso_logros
AFTER UPDATE ON JUGADOR
FOR EACH ROW WHEN (NEW.victorias != OLD.victorias) -- OR NEW.partidas_jugadas != OLD.partidas_jugadas)
EXECUTE FUNCTION fn_actualizar_progreso_logros();