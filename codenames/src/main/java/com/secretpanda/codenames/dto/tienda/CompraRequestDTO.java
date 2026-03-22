package com.secretpanda.codenames.dto.tienda;

/**
 * DTO para la solicitud de compra de un ítem de la tienda.
 * Solo uno de los dos campos debe venir informado por petición:
 *   - idTema           → inserta en INVENTARIO_TEMA
 *   - idPersonalizacion → inserta en INVENTARIO_PERSONALIZACION
 * NOTA: El id del jugador comprador se extrae de forma segura del token JWT en el controlador.
 */
public class CompraRequestDTO {

    private Integer idTema;
    private Integer idPersonalizacion;

    public CompraRequestDTO() {}

    public Integer getIdTema() {
        return idTema;
    }

    public void setIdTema(Integer idTema) {
        this.idTema = idTema;
    }

    public Integer getIdPersonalizacion() {
        return idPersonalizacion;
    }

    public void setIdPersonalizacion(Integer idPersonalizacion) {
        this.idPersonalizacion = idPersonalizacion;
    }
}