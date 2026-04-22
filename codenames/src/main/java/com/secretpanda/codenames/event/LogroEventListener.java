package com.secretpanda.codenames.event;

import com.secretpanda.codenames.service.JugadorService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LogroEventListener {

    private final JugadorService jugadorService;

    public LogroEventListener(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLogroEvent(LogroEvent event) {
        jugadorService.actualizarProgresoLogros(event.getIdJugador());
    }
}
