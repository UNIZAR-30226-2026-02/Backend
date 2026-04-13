package com.secretpanda.codenames.service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProfanityFilterService {

    public record FilterResult(String filteredText, boolean wasCensored) {}

    private final List<String> blacklist;

    public ProfanityFilterService(@Value("${game.profanity-blacklist}") String[] blacklist) {
        this.blacklist = Arrays.asList(blacklist);
    }

    /**
     * Filtra el mensaje sustituyendo palabras prohibidas por asteriscos.
     * Utiliza Regex para detectar variaciones básicas e ignorar mayúsculas.
     */
    public FilterResult filter(String text) {
        if (text == null || text.isBlank()) {
            return new FilterResult(text, false);
        }

        String filteredText = text;
        boolean censored = false;
        for (String word : blacklist) {
            // Regex que busca la palabra completa (\b) ignorando mayúsculas ((?i))
            String regex = "(?i)\\b" + Pattern.quote(word) + "\\b";
            String newText = filteredText.replaceAll(regex, "*".repeat(word.length()));
            
            if (!newText.equals(filteredText)) {
                censored = true;
            }
            filteredText = newText;
        }
        return new FilterResult(filteredText, censored);
    }
}
