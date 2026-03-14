package com.secretpanda.codenames.dto.jugador;

import java.util.List;

import com.secretpanda.codenames.dto.tienda.PersonalizacionDTO;
import com.secretpanda.codenames.dto.tienda.TemaDTO;

/**
 * DTO para devolver el inventario completo de un jugador.
 * Separa los cosméticos (cartas/tableros) de los temas de palabras.
 */
public class InventarioDTO {

    // Lo que el usuario tiene activo visualmente
    private Integer idCartaEquipada;
    private Integer idTableroEquipado;

    // Todo lo que el usuario posee
    private List<PersonalizacionDTO> personalizacionesAdquiridas;
    private List<TemaDTO> temasAdquiridos;

    public InventarioDTO() {
    }

    // Getters
    public Integer getIdCartaEquipada() { return idCartaEquipada; }
    public Integer getIdTableroEquipado() { return idTableroEquipado; }
    public List<PersonalizacionDTO> getPersonalizacionesAdquiridas() { return personalizacionesAdquiridas; }
    public List<TemaDTO> getTemasAdquiridos() { return temasAdquiridos; }

    // Setters
    public void setIdCartaEquipada(Integer idCartaEquipada) { this.idCartaEquipada = idCartaEquipada; }
    public void setIdTableroEquipado(Integer idTableroEquipado) { this.idTableroEquipado = idTableroEquipado; }
    public void setPersonalizacionesAdquiridas(List<PersonalizacionDTO> personalizacionesAdquiridas) { this.personalizacionesAdquiridas = personalizacionesAdquiridas; }
    public void setTemasAdquiridos(List<TemaDTO> temasAdquiridos) { this.temasAdquiridos = temasAdquiridos; }
}