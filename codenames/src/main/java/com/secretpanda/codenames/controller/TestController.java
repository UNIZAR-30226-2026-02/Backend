package com.secretpanda.codenames.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/hello")
    public String hello() {
        return "¡Hola! El backend de Codenames está funcionando correctamente en Azure. PRUEBA 1 DESPLIEGUE ACTION";
    }
}