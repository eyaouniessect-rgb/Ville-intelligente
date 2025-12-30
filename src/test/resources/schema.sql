-- Suppression des tables dans l'ordre inverse des dépendances
DROP TABLE IF EXISTS log_entry;
DROP TABLE IF EXISTS rapport;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS photo;
DROP TABLE IF EXISTS incident;
DROP TABLE IF EXISTS preference_notification;
DROP TABLE IF EXISTS utilisateur;
DROP TABLE IF EXISTS service_municipal;
DROP TABLE IF EXISTS quartier;
DROP TABLE IF EXISTS departements;

-- ==================== TABLES PRINCIPALES ====================

CREATE TABLE departements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    email VARCHAR(100),
    telephone VARCHAR(20)
);

CREATE TABLE quartier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    code_postal VARCHAR(4) NOT NULL,
    departement_id BIGINT,
    FOREIGN KEY (departement_id) REFERENCES departements(id)
);

CREATE TABLE service_municipal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    departement_id BIGINT NOT NULL,
    FOREIGN KEY (departement_id) REFERENCES departements(id)
);

CREATE TABLE utilisateur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prenom VARCHAR(255),
    nom VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    mot_de_passe VARCHAR(255),
    role VARCHAR(50),
    email_verifie BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    verification_token_expiration TIMESTAMP,
    auth_provider VARCHAR(50) DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    image_url VARCHAR(500),
    departement_id BIGINT,
    service_municipal_id BIGINT,
    FOREIGN KEY (departement_id) REFERENCES departements(id),
    FOREIGN KEY (service_municipal_id) REFERENCES service_municipal(id)
);

CREATE TABLE preference_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email_actif BOOLEAN DEFAULT TRUE,
    email_changement_statut BOOLEAN DEFAULT TRUE,
    push_actif BOOLEAN DEFAULT TRUE,
    utilisateur_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

CREATE TABLE incident (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description TEXT,
    statut VARCHAR(50),
    priorite VARCHAR(50),
    latitude DOUBLE,
    longitude DOUBLE,
    date_declaration TIMESTAMP,
    date_derniere_mise_a_jour TIMESTAMP,  -- ✅ ANCIEN
    date_derniere_miseajour TIMESTAMP,     -- ✅ NOUVEAU - sans underscore
    date_resolution_estimee TIMESTAMP,
    date_resolution TIMESTAMP,
    citoyen_id BIGINT,
    agent_id BIGINT,
    departement_id BIGINT NOT NULL,
    quartier_id BIGINT NOT NULL,
    service_id BIGINT,
    FOREIGN KEY (citoyen_id) REFERENCES utilisateur(id),
    FOREIGN KEY (agent_id) REFERENCES utilisateur(id),
    FOREIGN KEY (departement_id) REFERENCES departements(id),
    FOREIGN KEY (quartier_id) REFERENCES quartier(id),
    FOREIGN KEY (service_id) REFERENCES service_municipal(id)
);
CREATE TABLE photo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_fichier VARCHAR(255),
    type_contenu VARCHAR(100),
    chemin_stockage VARCHAR(500),
    date_upload TIMESTAMP,
    principale BOOLEAN DEFAULT FALSE,
    incident_id BIGINT,
    FOREIGN KEY (incident_id) REFERENCES incident(id)
);

CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50),
    message TEXT,
    date_envoi TIMESTAMP,
    lu BOOLEAN DEFAULT FALSE,
    utilisateur_id BIGINT,
    incident_id BIGINT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
    FOREIGN KEY (incident_id) REFERENCES incident(id)
);

CREATE TABLE rapport (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    date_generation TIMESTAMP NOT NULL,
    format VARCHAR(50) NOT NULL,
    nom_fichier VARCHAR(255) NOT NULL,
    chemin_fichier VARCHAR(500) NOT NULL,
    taille_fichier BIGINT,
    departement_id BIGINT,
    service_id BIGINT,
    quartier_id BIGINT,
    genere_par BIGINT,
    total_incidents BIGINT,
    incidents_resolus BIGINT,
    incidents_en_cours BIGINT,
    delai_moyen DOUBLE,
    FOREIGN KEY (departement_id) REFERENCES departements(id),
    FOREIGN KEY (service_id) REFERENCES service_municipal(id),
    FOREIGN KEY (quartier_id) REFERENCES quartier(id),
    FOREIGN KEY (genere_par) REFERENCES utilisateur(id)
);

CREATE TABLE log_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level VARCHAR(50),
    message TEXT,
    username VARCHAR(255),
    action VARCHAR(100),
    timestamp TIMESTAMP,
    ip_address VARCHAR(50)
);