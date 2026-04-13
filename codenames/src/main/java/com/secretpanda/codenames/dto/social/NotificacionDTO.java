package com.secretpanda.codenames.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NotificacionDTO(
    @JsonProperty("tipo") String tipo, 
    @JsonProperty("payload") Object payload
) {}