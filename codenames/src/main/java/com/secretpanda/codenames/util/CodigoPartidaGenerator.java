package com.secretpanda.codenames.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class CodigoPartidaGenerator {

    /**
     * Genera un código alfanumérico aleatorio para identificar o unirse a una partida.
     * El código consta de 6 caracteres en total (3 letras y 3 números mezclados), 
     * excluyendo deliberadamente caracteres ambiguos (como 'O', '0', 'I', '1') 
     * para evitar confusiones al momento de que los jugadores lo lean o escriban.
     *
     * @return Una cadena de texto (String) de 6 caracteres que representa el código de la partida.
     */

    private static final String LETRAS_PERMITIDAS = "ABCDEFGHJKLMNPRSTUVWXYZ";
    private static final String NUMEROS_PERMITIDOS = "23456789"; 
    private final SecureRandom random = new SecureRandom();

    public String generarCodigo() {
        List<Character> caracteres = new ArrayList<>();
        
        // Extraemos 3 letras aleatorias
        for (int i = 0; i < 3; i++) {
            caracteres.add(LETRAS_PERMITIDAS.charAt(random.nextInt(LETRAS_PERMITIDAS.length())));
        }
        
        // Extraemos 3 números aleatorios
        for (int i = 0; i < 3; i++) {
            caracteres.add(NUMEROS_PERMITIDOS.charAt(random.nextInt(NUMEROS_PERMITIDOS.length())));
        }
        
        // Mezclamos los caracteres para evitar patrones predecibles
        Collections.shuffle(caracteres, random);
        
        StringBuilder codigoFinal = new StringBuilder();
        for (char c : caracteres) {
            codigoFinal.append(c);
        }
        
        return codigoFinal.toString();
    }
}