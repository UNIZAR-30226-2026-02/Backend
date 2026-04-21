package com.secretpanda.codenames.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secretpanda.codenames.dto.tienda.PersonalizacionDTO;
import com.secretpanda.codenames.dto.tienda.TemaDTO;
import com.secretpanda.codenames.exception.BadRequestException;
import com.secretpanda.codenames.exception.NotFoundException;
import com.secretpanda.codenames.mapper.tienda.PersonalizacionMapper;
import com.secretpanda.codenames.mapper.tienda.TemaMapper;
import com.secretpanda.codenames.model.InventarioPersonalizacion;
import com.secretpanda.codenames.model.InventarioPersonalizacionId;
import com.secretpanda.codenames.model.InventarioTema;
import com.secretpanda.codenames.model.InventarioTemaId;
import com.secretpanda.codenames.model.Jugador;
import com.secretpanda.codenames.model.Personalizacion;
import com.secretpanda.codenames.model.Tema;
import com.secretpanda.codenames.repository.InventarioPersonalizacionRepository;
import com.secretpanda.codenames.repository.InventarioTemaRepository;
import com.secretpanda.codenames.repository.JugadorRepository;
import com.secretpanda.codenames.repository.PersonalizacionRepository;
import com.secretpanda.codenames.repository.TemaRepository;

@Service
public class TiendaService {

    private final TemaRepository temaRepository;
    private final PersonalizacionRepository personalizacionRepository;
    private final InventarioTemaRepository inventarioTemaRepository;
    private final InventarioPersonalizacionRepository inventarioPersoRepository;
    private final JugadorRepository jugadorRepository;
    private final JugadorService jugadorService;

    public TiendaService(TemaRepository temaRepository, 
                         PersonalizacionRepository personalizacionRepository, 
                         InventarioTemaRepository inventarioTemaRepository, 
                         InventarioPersonalizacionRepository inventarioPersoRepository, 
                         JugadorRepository jugadorRepository,
                         JugadorService jugadorService) {
        this.temaRepository = temaRepository;
        this.personalizacionRepository = personalizacionRepository;
        this.inventarioTemaRepository = inventarioTemaRepository;
        this.inventarioPersoRepository = inventarioPersoRepository;
        this.jugadorRepository = jugadorRepository;
        this.jugadorService = jugadorService;
    }

    @Transactional(readOnly = true)
    public List<TemaDTO> getTemasTienda(String idGoogle) {
        List<Tema> todos = temaRepository.findByActivoTrue();

        final List<Integer> misTemasIds = (idGoogle != null) 
                ? inventarioTemaRepository.findById_IdJugador(idGoogle)
                        .stream()
                        .map(it -> it.getTema().getIdTema())
                        .collect(Collectors.toList())
                : List.of();

        return todos.stream()
                .map(t -> TemaMapper.toDTO(t, misTemasIds.contains(t.getIdTema())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PersonalizacionDTO> getPersonalizacionesTienda(String idGoogle) {
        List<Personalizacion> todas = personalizacionRepository.findByActivoTrue();

        final List<Integer> misPersosIds = (idGoogle != null)
                ? inventarioPersoRepository.findById_IdJugador(idGoogle)
                        .stream()
                        .map(ip -> ip.getPersonalizacion().getIdPersonalizacion())
                        .collect(Collectors.toList())
                : List.of();

        return todas.stream()
                .map(p -> PersonalizacionMapper.toDTO(p, misPersosIds.contains(p.getIdPersonalizacion())))
                .collect(Collectors.toList());
    }

    @Transactional
    public int comprarTema(String idGoogle, Integer idTema) {
        Jugador j = jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado"));

        Tema t = temaRepository.findById(idTema)
                .orElseThrow(() -> new NotFoundException("El tema no existe"));

        if (inventarioTemaRepository.existsById_IdJugadorAndId_IdTema(idGoogle, idTema)) {
            throw new BadRequestException("Ya posees este tema de palabras.");
        }

        if (j.getBalas() < t.getPrecioBalas()) {
            throw new BadRequestException("Saldo insuficiente para comprar este tema.");
        }

        jugadorService.modificarBalas(idGoogle, -t.getPrecioBalas());

        InventarioTema it = new InventarioTema();
        InventarioTemaId itid = new InventarioTemaId();
        itid.setIdJugador(idGoogle);
        itid.setIdTema(idTema);
        it.setId(itid);
        it.setJugador(j);
        it.setTema(t);
        inventarioTemaRepository.save(it);

        return j.getBalas();
    }

    @Transactional
    public int comprarPersonalizacion(String idGoogle, Integer idPerso) {
        Jugador j = jugadorRepository.findById(idGoogle)
                .orElseThrow(() -> new NotFoundException("Jugador no encontrado"));

        Personalizacion p = personalizacionRepository.findById(idPerso)
                .orElseThrow(() -> new NotFoundException("El artículo no existe"));

        if (inventarioPersoRepository.findById_IdJugadorAndId_IdPersonalizacion(idGoogle, idPerso).isPresent()) {
            throw new BadRequestException("Ya posees este artículo estético.");
        }

        if (j.getBalas() < p.getPrecioBala()) {
            throw new BadRequestException("Saldo insuficiente.");
        }

        jugadorService.modificarBalas(idGoogle, -p.getPrecioBala());

        InventarioPersonalizacion ip = new InventarioPersonalizacion();
        InventarioPersonalizacionId ipid = new InventarioPersonalizacionId();
        ipid.setIdJugador(idGoogle);
        ipid.setIdPersonalizacion(idPerso);
        ip.setId(ipid);
        ip.setJugador(j);
        ip.setPersonalizacion(p);
        ip.setEquipado(false);
        inventarioPersoRepository.save(ip);

        return j.getBalas();
    }
}