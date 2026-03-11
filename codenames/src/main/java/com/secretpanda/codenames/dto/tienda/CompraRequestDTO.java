package com.secretpanda.codenames.dto.tienda;

/**
 * DTO para la solicitud de compra o equipamiento de una personalización.
 */
public class CompraRequestDTO {

    // Nota: El idJugador podría omitirse si se obtiene del token JWT de seguridad.
    private String idJugador;
    private Integer idPersonalizacion;

    public CompraRequestDTO() {
    }

    public String getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(String idJugador) {
        this.idJugador = idJugador;
    }

    public Integer getIdPersonalizacion() {
        return idPersonalizacion;
    }

    public void setIdPersonalizacion(Integer idPersonalizacion) {
        this.idPersonalizacion = idPersonalizacion;
    }
}