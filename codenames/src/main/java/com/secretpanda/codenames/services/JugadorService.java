package com.secretpanda.codenames.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.secretpanda.codenames.models.Jugador;
import com.secretpanda.codenames.repositories.JugadorRepository;

@Service // Indica que contiene lógica de negocio
public class JugadorService {

    @Autowired // Inyecta automáticamente el repositorio
    private JugadorRepository jugadorRepository;

    // Obtener todos
    public List<Jugador> obtenerTodos() {
        return jugadorRepository.findAll();
    }

    // Obtener por ID
    public Optional<Jugador> obtenerPorId(String idGoogle) {
        return jugadorRepository.findById(idGoogle);
    }

    // Crear un nuevo jugador
    public Jugador crearJugador(Jugador jugador) {
        if (jugadorRepository.existsByTag(jugador.getTag())) {
            throw new RuntimeException("El tag ya está en uso");
        }
        return jugadorRepository.save(jugador);
    }

    // Actualizar jugador
    public Jugador actualizarJugador(String idGoogle, Jugador detalles) {
        return jugadorRepository.findById(idGoogle).map(jugadorExistente -> {
            jugadorExistente.setTag(detalles.getTag());
            jugadorExistente.setFotoPerfil(detalles.getFotoPerfil());
            // No actualizamos estadísticas aquí, de eso se encargan tus triggers de BD
            return jugadorRepository.save(jugadorExistente);
        }).orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
    }

    // Eliminar jugador
    public void eliminarJugador(String idGoogle) {
        jugadorRepository.deleteById(idGoogle);
    }
}