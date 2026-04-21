package com.secretpanda.codenames.dto.tienda;

import lombok.Data;

@Data
public class ComprarItemRequest {
    private Integer idTema;
    private Integer idPersonalizacion;
}