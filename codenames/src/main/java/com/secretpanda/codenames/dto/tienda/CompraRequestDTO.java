package com.secretpanda.codenames.dto.tienda;

/**
 * DTO para la solicitud de compra o equipamiento de una personalización.
 * NOTA: El id del jugador comprador se extrae de forma segura del token JWT en el controlador.
 */
public class CompraRequestDTO {

    private Integer idPersonalizacion; 

    public CompraRequestDTO() {
    }

    public Integer getIdPersonalizacion() { 
        return idPersonalizacion; 
    }

    public void setIdPersonalizacion(Integer idPersonalizacion) { 
        this.idPersonalizacion = idPersonalizacion; 
    }
}