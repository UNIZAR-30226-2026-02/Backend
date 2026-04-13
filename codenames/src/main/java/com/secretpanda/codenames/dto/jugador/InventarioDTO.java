package com.secretpanda.codenames.dto.jugador;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO contenedor del inventario completo de un jugador.
 * Agrupa sus cosméticos y sus packs de palabras adquiridos.
 * Se usa en GET /api/personalizaciones/{id_google} y en el UserContext.
 */
public class InventarioDTO {

    @JsonProperty("personalizaciones")
    private List<PersonalizacionInventarioDTO> personalizaciones;
    @JsonProperty("temas")
    private List<TemaInventarioDTO> temas;

    public InventarioDTO() {}

    public InventarioDTO(List<PersonalizacionInventarioDTO> personalizaciones,
                         List<TemaInventarioDTO> temas) {
        this.personalizaciones = personalizaciones;
        this.temas = temas;
    }

    // Getters
    public List<PersonalizacionInventarioDTO> getPersonalizaciones() { return personalizaciones; }
    public List<TemaInventarioDTO> getTemas() { return temas; }

    // Setters
    public void setPersonalizaciones(List<PersonalizacionInventarioDTO> personalizaciones) { this.personalizaciones = personalizaciones; }
    public void setTemas(List<TemaInventarioDTO> temas) { this.temas = temas; }
}