package com.secretpanda.codenames.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProfanityFilterServiceTest {

    private ProfanityFilterService profanityFilterService;

    @BeforeEach
    void setUp() {
        String[] blacklist = {"insulto1", "palabrota"};
        profanityFilterService = new ProfanityFilterService(blacklist);
    }

    @Test
    void testFilter_ConInsultos() {
        String texto = "Este es un insulto1 y una palabrota.";
        ProfanityFilterService.FilterResult result = profanityFilterService.filter(texto);

        assertTrue(result.wasCensored());
        assertEquals("Este es un ******** y una *********.", result.filteredText());
    }

    @Test
    void testFilter_SinInsultos() {
        String texto = "Este mensaje es limpio.";
        ProfanityFilterService.FilterResult result = profanityFilterService.filter(texto);

        assertFalse(result.wasCensored());
        assertEquals("Este mensaje es limpio.", result.filteredText());
    }

    @Test
    void testFilter_IgnoraMayusculas() {
        String texto = "INSULTO1 en mayúsculas.";
        ProfanityFilterService.FilterResult result = profanityFilterService.filter(texto);

        assertTrue(result.wasCensored());
        assertEquals("******** en mayúsculas.", result.filteredText());
    }

    @Test
    void testFilter_PalabrasCompletas() {
        String texto = "Esto no es un insulto1pero casi.";
        ProfanityFilterService.FilterResult result = profanityFilterService.filter(texto);

        assertFalse(result.wasCensored());
        assertEquals("Esto no es un insulto1pero casi.", result.filteredText());
    }
}
