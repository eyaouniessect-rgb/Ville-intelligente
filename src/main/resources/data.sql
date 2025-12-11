-- Script SQL pour créer un compte SUPERADMIN manuellement
-- Utilisez ce script si vous préférez créer le compte directement en base de données

-- Hash BCrypt pour le mot de passe "SuperAdmin123!@#"
-- ⚠️ ATTENTION : Ce hash est généré avec BCrypt, vous devez le générer vous-même
-- ou utiliser la classe DataInitializer qui le fait automatiquement

-- Exemple d'insertion (à adapter selon votre structure de table)
-- Le hash BCrypt change à chaque génération, vous devez le générer

/*
INSERT INTO utilisateur (
    nom, 
    prenom, 
    email, 
    mot_de_passe, 
    role, 
    email_verifie, 
    verification_token, 
    verification_token_expiration
) VALUES (
    'Admin',
    'Super',
    'superadmin@ville.intelligente',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- Hash pour "SuperAdmin123!@#"
    'SUPERADMIN',
    true,
    NULL,
    NULL
);
*/

-- Pour générer un hash BCrypt correct, utilisez la classe DataInitializer
-- ou un outil en ligne comme : https://bcrypt-generator.com/

