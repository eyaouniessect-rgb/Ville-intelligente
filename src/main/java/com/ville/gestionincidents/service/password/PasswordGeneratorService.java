package com.ville.gestionincidents.service.password;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Service de génération de mots de passe sécurisés
 */
@Service
public class PasswordGeneratorService {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "@$!%*?&";
    private static final String ALL_CHARS = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARS;

    private static final int DEFAULT_PASSWORD_LENGTH = 16;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Génère un mot de passe aléatoire sécurisé
     *
     * Format : 16 caractères avec au moins :
     * - 1 majuscule
     * - 1 minuscule
     * - 1 chiffre
     * - 1 caractère spécial
     *
     * @return Mot de passe généré
     */
    public String generatePassword() {
        return generatePassword(DEFAULT_PASSWORD_LENGTH);
    }

    /**
     * Génère un mot de passe aléatoire sécurisé avec une longueur personnalisée
     *
     * @param length Longueur souhaitée (minimum 12)
     * @return Mot de passe généré
     */
    public String generatePassword(int length) {
        if (length < 12) {
            throw new IllegalArgumentException("La longueur minimale du mot de passe est 12 caractères");
        }

        StringBuilder password = new StringBuilder(length);

        // 1. Garantir au moins un caractère de chaque type requis
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // 2. Remplir le reste avec des caractères aléatoires
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // 3. Mélanger les caractères pour éviter un pattern prévisible
        return shuffleString(password.toString());
    }

    /**
     * Mélange les caractères d'une chaîne
     */
    private String shuffleString(String string) {
        char[] characters = string.toCharArray();

        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }

        return new String(characters);
    }

    /**
     * Génère un mot de passe facilement mémorisable (mais toujours sécurisé)
     * Format : Mot-Mot-Nombre-Symbole (ex: Soleil-Lune-2024-!)
     *
     * @return Mot de passe mémorisable
     */
    public String generateMemorablePassword() {
        String[] words = {
                "Soleil", "Lune", "Etoile", "Montagne", "Ocean", "Foret", "Riviere", "Nuage",
                "Jardin", "Fleur", "Arbre", "Oiseau", "Papillon", "Dragon", "Lion", "Aigle"
        };

        String word1 = words[random.nextInt(words.length)];
        String word2 = words[random.nextInt(words.length)];
        int number = 1000 + random.nextInt(9000); // Nombre entre 1000 et 9999
        char special = SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length()));

        return word1 + "-" + word2 + "-" + number + special;
    }
}