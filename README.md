# 🏙️ Plateforme de Gestion d'Incidents dans une Ville Intelligente

## 📋 Description Générale

Cette application web est une **plateforme complète de gestion d'incidents urbains** développée avec Spring Boot. Elle permet aux citoyens de signaler des incidents (problèmes d'infrastructure, propreté, sécurité, etc.) et aux administrations municipales de les traiter efficacement.

Le système facilite la communication entre les citoyens et les services municipaux, améliorant ainsi la réactivité et la transparence dans la gestion des problèmes urbains.

## 🎯 Objectif du Projet

Créer une solution numérique moderne et efficace pour :

- **Centraliser** la déclaration et le suivi des incidents urbains
- **Automatiser** l'assignation des incidents aux services compétents
- **Améliorer** la communication entre citoyens et administration
- **Fournir** des statistiques et rapports pour l'aide à la décision
- **Garantir** la traçabilité et la transparence des interventions

## 🔍 Problématique Traitée

Dans les villes modernes, la gestion des incidents urbains (nids-de-poule, éclairage défaillant, déchets, etc.) est souvent :

- **Dispersée** : plusieurs canaux de signalement non coordonnés
- **Manuelle** : traitement papier ou emails non structurés
- **Peu transparente** : les citoyens ne suivent pas l'avancement de leurs signalements
- **Inefficace** : délais de traitement longs, perte d'informations

Cette plateforme résout ces problèmes en offrant un système unifié, automatisé et transparent.

## 🌍 Contexte

Application développée dans le cadre d'un projet de **ville intelligente (Smart City)**, permettant :

- La **déclaration d'incidents** par les citoyens via une interface web intuitive
- La **gestion administrative** par les départements et services municipaux
- Le **suivi en temps réel** du statut des incidents
- La **génération de rapports** statistiques pour l'analyse et la planification

---

## ✨ Fonctionnalités Principales

### 👥 Gestion des Utilisateurs et Rôles

Le système distingue **4 types d'utilisateurs** avec des droits spécifiques :

#### 🧑‍💼 **CITOYEN**

- Inscription et authentification (email/mot de passe ou OAuth2 Google)
- Déclaration d'incidents avec photos et géolocalisation
- Consultation de ses incidents déclarés
- Suivi du statut de ses incidents
- Clôture d'un incident résolu
- Gestion du profil personnel
- Modification du mot de passe
- Préférences de notifications personnalisables

#### 👨‍💻 **AGENT**

- Visualisation des incidents assignés à son service
- Mise à jour du statut des incidents (PRIS_EN_CHARGE, EN_RESOLUTION, RESOLU)
- Consultation des détails complets d'un incident
- Statistiques personnelles (nombre d'incidents traités)

#### 👨‍💼 **ADMIN**

- Gestion des incidents de son département
- Assignation d'incidents aux services et agents
- Filtrage avancé (par service, statut, dates)
- Dashboard avec statistiques détaillées :
  - Nombre d'incidents par statut
  - Incidents par quartier
  - Incidents par service municipal
  - Délai moyen de résolution
- Génération de rapports (PDF, Excel)
- Gestion des services municipaux de son département

#### 🔐 **SUPERADMIN**

- Gestion complète de la plateforme
- Création et gestion des départements
- Création et gestion des quartiers
- Gestion des utilisateurs (création, modification, désactivation)
- Visualisation des logs

### 📝 Gestion des Incidents

#### Création d'Incident

- Formulaire intuitif avec validation en temps réel
- Description détaillée (10-1000 caractères)
- Sélection du département (catégorie)
- Sélection du quartier
- Géolocalisation interactive via carte Leaflet
- Upload de photos (jusqu'à 3 photos, max 5MB chacune)

#### Statuts des Incidents

1. **SIGNALE** : Incident déclaré par un citoyen
2. **PRIS_EN_CHARGE** : Un agent a pris en charge l'incident
3. **EN_RESOLUTION** : L'incident est en cours de traitement
4. **RESOLU** : L'incident a été résolu
5. **CLOTURE** : L'incident est clôturé par le citoyen

#### Priorités

- **BASSE** : Impact limité
- **MOYENNE** : Impact modéré
- **HAUTE** : Impact important
- **URGENTE** : Intervention immédiate requise

#### Fonctionnalités Avancées

- Filtrage multi-critères (statut, service, dates, quartier)
- Recherche
- Géolocalisation précise

### 🔔 Système de Notifications

#### Types de Notifications

- **CREATION_INCIDENT** : Confirmation de création d'un incident
- **CHANGEMENT_STATUT** : Mise à jour du statut d'un incident
- **ASSIGNATION** : Assignation d'un incident à un agent/service

#### Canaux de Notification

1. **Notifications Internes** : Toujours enregistrées en base de données
2. **Email** : Envoi d'emails selon les préférences utilisateur
3. **WebSocket** : Notifications push en temps réel dans l'interface

#### Préférences Utilisateur

Chaque utilisateur peut personnaliser ses notifications :

- Activer/désactiver les emails
- Activer/désactiver les notifications push
- Activer/désactiver les emails de changement de statut
- Gestion via une interface dédiée

#### Historique

- Consultation de l'historique complet des notifications
- Marquage comme "lu/non lu"
- Compteur de notifications non lues

### 📸 Upload et Gestion des Images

- **Upload multiple** : Jusqu'à 3 photos par incident
- **Validation** : Type (images uniquement), taille (max 5MB)
- **Stockage** : Fichiers stockés dans le dossier `uploads/`
- **Photo principale** : Première photo marquée comme principale
- **Affichage** : Prévisualisation avant upload, galerie dans les détails

---

## 🏗️ Architecture du Projet

### Architecture en Couches

Le projet suit une **architecture en couches** (Layered Architecture) pour une séparation claire des responsabilités :

```
┌─────────────────────────────────────┐
│         CONTROLLER LAYER             │  ← Gestion des requêtes HTTP
├─────────────────────────────────────┤
│         SERVICE LAYER                │  ← Logique métier
├─────────────────────────────────────┤
│         REPOSITORY LAYER             │  ← Accès aux données
├─────────────────────────────────────┤
│         ENTITY LAYER                 │  ← Modèle de données JPA
└─────────────────────────────────────┘
```

#### 🎮 **Controller Layer** (`controller/`)

**Rôle** : Gérer les requêtes HTTP, valider les entrées, appeler les services, préparer les réponses.

- **`controller/citoyen/`** : Endpoints pour les citoyens
- **`controller/agent/`** : Endpoints pour les agents
- **`controller/admin/`** : Endpoints pour les administrateurs
- **`controller/superadmin/`** : Endpoints pour les super-admins
- **`controller/incident/`** : Gestion des incidents
- **`controller/notification/`** : Gestion des notifications
- **`controller/AuthController`** : Authentification et inscription

**Responsabilités** :

- Validation des DTOs avec `@Valid`
- Gestion des erreurs de validation (`BindingResult`)
- Injection des données dans le modèle Thymeleaf
- Redirection et messages flash

#### ⚙️ **Service Layer** (`service/`)

**Rôle** : Contenir toute la logique métier de l'application.

- **`service/incident/`** : Création, modification, assignation d'incidents
- **`service/notification/`** : Création et envoi de notifications
- **`service/utilisateur/`** : Gestion des utilisateurs
- **`service/dashboardAdmin/`** : Calculs statistiques
- **`service/email/`** : Envoi d'emails
- **`service/rapport/`** : Génération de rapports

**Responsabilités** :

- Logique métier complexe
- Transactions (`@Transactional`)
- Appels aux repositories
- Appels aux autres services
- Gestion des exceptions métier

#### 💾 **Repository Layer** (`repository/`)

**Rôle** : Interface avec la base de données via Spring Data JPA.

- **`IncidentRepository`** : Requêtes sur les incidents
- **`UtilisateurRepository`** : Requêtes sur les utilisateurs
- **`NotificationRepository`** : Requêtes sur les notifications
- **`DepartementRepository`** : Requêtes sur les départements
- **`QuartierRepository`** : Requêtes sur les quartiers
- **`ServiceMunicipalRepository`** : Requêtes sur les services

**Responsabilités** :

- Requêtes CRUD de base (héritées de `JpaRepository`)
- Requêtes personnalisées avec `@Query`
- Méthodes de recherche complexes
- Projections pour optimiser les requêtes

#### 📦 **Entity Layer** (`entity/`)

**Rôle** : Modéliser les tables de la base de données avec JPA/Hibernate.

- **`Incident`** : Table des incidents
- **`Utilisateur`** : Table des utilisateurs
- **`Notification`** : Table des notifications
- **`Photo`** : Table des photos
- **`Departement`** : Table des départements
- **`Quartier`** : Table des quartiers
- **`ServiceMunicipal`** : Table des services municipaux
- **`Rapport`** : Table des rapports générés
- **`PreferenceNotification`** : Préférences de notification

**Responsabilités** :

- Mapping objet-relationnel (ORM)
- Définition des relations (OneToMany, ManyToOne)
- Validation avec annotations JPA
- Contraintes d'intégrité

#### 📋 **DTO Layer** (`dto/`)

**Rôle** : Objets de transfert de données pour isoler les entités de la couche présentation.

- **`dto/incident/`** : DTOs pour les incidents
- **`dto/utilisateur/`** : DTOs pour les utilisateurs
- **`dto/notification/`** : DTOs pour les notifications
- **`dto/auth/`** : DTOs pour l'authentification

**Avantages** :

- Sécurité : ne pas exposer les entités directement
- Performance : ne transférer que les données nécessaires
- Validation : annotations Bean Validation
- Flexibilité : structure différente de l'entité si besoin

#### 🔄 **Mapper Layer** (`mapper/`)

**Rôle** : Conversion entre entités et DTOs.

- **`IncidentMapper`** : Conversion Incident ↔ IncidentDTO
- **`UtilisateurMapper`** : Conversion Utilisateur ↔ UtilisateurDTO
- **`PreferenceNotificationMapper`** : Conversion préférences

**Avantages** :

- Code réutilisable
- Séparation claire des responsabilités
- Facilite les tests

---

## 🛠️ Technologies Utilisées

### Backend

- **Spring Boot 2.7.18** : Framework principal
- **Spring Data JPA** : Abstraction pour l'accès aux données
- **Hibernate** : ORM (Object-Relational Mapping)
- **Spring Security** : Authentification et autorisation
- **Spring Mail** : Envoi d'emails
- **Spring WebSocket** : Notifications en temps réel
- **OAuth2 Client** : Authentification via Google
- **Bean Validation** : Validation des données
- **Thymeleaf** : Moteur de templates côté serveur
- **Lombok** : Réduction du code boilerplate

### Base de Données

- **MySQL 8.0** : Base de données relationnelle
- **JPA/Hibernate** : Mapping objet-relationnel
- **Hibernate DDL Auto** : Génération automatique du schéma

### Outils et Bibliothèques

- **Maven** : Gestion des dépendances et build
- **Lombok** : Génération automatique de getters/setters/constructeurs
- **Leaflet** : Cartographie interactive
- **Spring DevTools** : Rechargement automatique en développement

### Frontend

- **Thymeleaf** : Templates HTML côté serveur
- **HTML5/CSS3** : Structure et style
- **JavaScript (Vanilla)** : Interactivité côté client
- **Leaflet.js** : Cartes interactives

---

## 📁 Structure du Projet

### Arborescence des Packages Java

```
com.ville.gestionincidents/
│
├── config/                    # Configurations Spring
│   ├── AppConfig.java
│   ├── DataInitializer.java
│   ├── MailConfig.java
│   ├── SecurityConfig.java
│   ├── WebMvcConfig.java
│   └── WebSocketConfig.java
│
├── controller/                # Contrôleurs REST/Web
│   ├── admin/
│   ├── agent/
│   ├── citoyen/
│   ├── superadmin/
│   ├── incident/
│   ├── notification/
│   └── AuthController.java
│
├── dto/                       # Data Transfer Objects
│   ├── auth/
│   ├── incident/
│   ├── notification/
│   ├── utilisateur/
│   └── dashboardAdmin/
│
├── entity/                    # Entités JPA
│   ├── Incident.java
│   ├── Utilisateur.java
│   ├── Notification.java
│   ├── Photo.java
│   ├── Departement.java
│   ├── Quartier.java
│   ├── ServiceMunicipal.java
│   └── Rapport.java
│
├── enumeration/               # Énumérations
│   ├── Role.java
│   ├── StatutIncident.java
│   ├── PrioriteIncident.java
│   ├── TypeNotification.java
│   └── AuthProvider.java
│
├── exception/                 # Gestion des exceptions
│   ├── GlobalExceptionHandler.java
│   ├── NotFoundException.java
│   └── UnauthorizedException.java
│
├── mapper/                    # Mappers Entity ↔ DTO
│   ├── IncidentMapper.java
│   ├── UtilisateurMapper.java
│   └── PreferenceNotificationMapper.java
│
├── repository/                # Repositories Spring Data JPA
│   ├── IncidentRepository.java
│   ├── UtilisateurRepository.java
│   ├── NotificationRepository.java
│   └── ...
│
├── security/                  # Configuration sécurité
│   ├── SecurityConfig.java
│   ├── CustomUserDetailsService.java
│   ├── CustomOAuth2UserService.java
│   └── CurrentUserService.java
│
├── service/                   # Services métier
│   ├── incident/
│   ├── notification/
│   ├── utilisateur/
│   ├── email/
│   ├── dashboardAdmin/
│   └── rapport/
│
└── util/                      # Utilitaires
    ├── FileUploadUtil.java
    ├── PdfUtil.java
    └── LoggingAspect.java
```

## 📋 DTO et Validation

### Rôle des DTOs

Les **DTOs (Data Transfer Objects)** sont utilisés pour :

1. **Sécurité** : Ne pas exposer directement les entités JPA (éviter les fuites de données)
2. **Performance** : Ne transférer que les données nécessaires
3. **Validation** : Valider les données avant traitement
4. **Flexibilité** : Structure différente de l'entité si besoin

### Annotations de Validation Utilisées

- **`@NotNull`** : Champ obligatoire (non null)
- **`@NotBlank`** : Chaîne non vide (non null, non vide, non blanc)
- **`@Size(min, max)`** : Taille de chaîne ou collection
- **`@DecimalMin` / `@DecimalMax`** : Valeur minimale/maximale pour nombres
- **`@Email`** : Format email valide
- **`@Pattern`** : Expression régulière

````

---

## 🔐 Sécurité

### Authentification

Le système supporte **deux méthodes d'authentification** :

#### 1. Authentification Locale (Email + Mot de passe)
- Inscription avec email et mot de passe
- Vérification d'email obligatoire (token envoyé par email)
- Hashage des mots de passe avec **BCrypt**
- Connexion via formulaire Spring Security

#### 2. Authentification OAuth2 (Google)
- Connexion via compte Google
- Création automatique du compte si première connexion
- Récupération du profil (nom, email, photo)
- Support pour d'autres providers (extensible)

### Rôles et Autorisations

Le système utilise **Spring Security** avec des rôles basés sur l'enum `Role` :

```java
public enum Role {
    CITOYEN,      // ROLE_CITOYEN
    AGENT,        // ROLE_AGENT
    ADMIN,        // ROLE_ADMIN
    SUPERADMIN    // ROLE_SUPERADMIN
}
````

## 🚀 Installation et Exécution

### Prérequis Techniques

Pour les développeurs :

- **Java 17** ou supérieur
- **Maven 3.6+**
- **MySQL 8.0+**
- **IDE** (IntelliJ IDEA, Eclipse, VS Code) recommandé

Pour les DevOps :

- **Git** pour la gestion de version
- **Docker** (version 20.10+)
- **Docker Compose** (version 2.0+)
- **Java 17** et **Maven 3.6+** (pour le build local si nécessaire)

### Étapes d'Installation

#### 1. Cloner le Projet

```bash
git clone <url-du-repo>
cd Ville-intelligente
```

#### 2. Configurer la Base de Données

Créer une base de données MySQL :

```sql
CREATE DATABASE gestion_incidents CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 3. Configurer `application.properties`

Éditer `src/main/resources/application.properties` :

```properties
# Base de données
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_incidents?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=votre_mot_de_passe

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Email (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre_email@gmail.com
spring.mail.password=votre_mot_de_passe_application
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# URL de base
app.base-url=http://localhost:8080

# Upload
uploads.path=uploads/
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB

# OAuth2 Google (optionnel)
spring.security.oauth2.client.registration.google.client-id=votre_client_id
spring.security.oauth2.client.registration.google.client-secret=votre_client_secret
```

#### 4. Créer le Dossier d'Upload

```bash
mkdir uploads
```

#### 5. Compiler et Lancer

**Avec Maven** :

```bash
# Compiler
mvn clean install

# Lancer
mvn spring-boot:run
```

**Avec un IDE** :

- Importer le projet comme projet Maven
- Lancer la classe `VilleIncidentsApplication`

#### 6. Accéder à l'Application

- **URL** : http://localhost:8080
- **Page de connexion** : http://localhost:8080/auth/login

### Commandes Maven Utiles

```bash
# Lancer l'application
mvn spring-boot:run

# Exécuter les tests
mvn test

# Nettoyer le projet
mvn clean

# Compiler et créer le JAR
mvn clean package
```

---

## 🐳 Instructions pour les DevOps

Cette section décrit comment utiliser Docker et Docker Compose pour déployer l'application, ainsi que le fonctionnement du pipeline CI/CD.

### Prérequis DevOps

- **Docker** (version 20.10+)
- **Docker Compose** (version 2.0+)
- Accès au dépôt Git (GitHub)

### Conteneurisation avec Docker

#### 1. Configuration via Variables d'Environnement

L'application utilise des variables d'environnement pour la configuration. Créer un fichier `.env` à la racine du projet :

```bash
# Base de données
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/gestion_incidents?useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password

# Email (Gmail)
SPRING_MAIL_USERNAME=votre_email@gmail.com
SPRING_MAIL_PASSWORD=votre_mot_de_passe_application

# OAuth2 Google (optionnel)
GOOGLE_CLIENT_ID=votre_client_id
GOOGLE_CLIENT_SECRET=votre_client_secret

# URL de base
APP_BASE_URL=http://localhost:8080
```

#### 2. Construction de l'Image Docker

**Méthode 1 : Build manuel**

D'abord, construire le JAR avec Maven :

```bash
mvn clean package
```

Puis construire l'image Docker :

```bash
docker build -t gestion-incidents:latest .
```

**Méthode 2 : Utiliser Docker Compose (recommandé)**

Docker Compose construira automatiquement l'image si nécessaire :

```bash
docker-compose build
```

#### 3. Lancement avec Docker Compose

**Lancer tous les services** (application + base de données) :

```bash
docker-compose up -d
```

Cette commande va :

- Démarrer le conteneur MySQL avec la base de données `gestion_incidents`
- Construire l'image de l'application (si nécessaire)
- Démarrer le conteneur de l'application Spring Boot
- Créer les volumes nécessaires pour la persistance des données

**Vérifier le statut des conteneurs** :

```bash
docker-compose ps
```

**Consulter les logs** :

```bash
# Logs de tous les services
docker-compose logs -f

# Logs de l'application uniquement
docker-compose logs -f app

# Logs de la base de données uniquement
docker-compose logs -f db
```

**Arrêter les services** :

```bash
docker-compose stop
```

**Arrêter et supprimer les conteneurs** :

```bash
docker-compose down
```

**Arrêter et supprimer les conteneurs + volumes** (⚠️ supprime les données) :

```bash
docker-compose down -v
```

#### 4. Accès à l'Application

Une fois les conteneurs démarrés, l'application est accessible à :

- **URL** : http://localhost:8080
- **Page de connexion** : http://localhost:8080/auth/login

La base de données MySQL est accessible depuis le conteneur `db` sur le port `3306`.

#### 5. Structure Docker

**Dockerfile**

Le `Dockerfile` utilise :

- **Image de base** : `eclipse-temurin:17-jdk-alpine` (JDK 17)
- **JAR** : Copie du fichier JAR depuis `target/`
- **Port exposé** : 8080
- **Commande** : `java -jar app.jar`

**docker-compose.yml**

Le fichier `docker-compose.yml` définit :

- **Service `db`** : MySQL 8.0
  - Variables d'environnement pour la configuration
  - Volume persistant pour les données
  - Healthcheck pour vérifier la disponibilité
- **Service `app`** : Application Spring Boot
  - Build à partir du Dockerfile
  - Dépend de `db` (attend que la base soit saine)
  - Variables d'environnement chargées depuis `.env`
  - Redémarrage automatique en cas d'échec

#### 6. Commandes Docker Utiles

```bash
# Construire uniquement l'image
docker build -t gestion-incidents:latest .

# Lancer un conteneur manuellement
docker run -d -p 8080:8080 --name gestion-incidents-app gestion-incidents:latest

# Entrer dans un conteneur en cours d'exécution
docker exec -it gestion-incidents-app sh

# Voir les images Docker
docker images

# Nettoyer les images non utilisées
docker image prune -a
```

---

## 🔄 Pipeline CI/CD

Le projet utilise **GitHub Actions** pour l'intégration et le déploiement continu.

### Vue d'ensemble

Le pipeline CI/CD est composé de **deux workflows** :

1. **CI (Intégration Continue)** : Build et tests
2. **CD (Déploiement Continu)** : Build Docker et déploiement

### Workflow CI : Build & Test

**Fichier** : `.github/workflows/ci.yml`

#### Déclencheurs

Le workflow CI est déclenché automatiquement :

- ✅ À chaque **push** sur les branches `main` ou `dev`
- ✅ À chaque **pull request** vers les branches `main` ou `dev`

#### Étapes principales

1. **Checkout du code**

   - Récupération du code source depuis le dépôt

2. **Configuration de l'environnement**

   - Installation de **JDK 17** (distribution Temurin)
   - Mise en cache des dépendances Maven pour accélérer les builds suivants

3. **Build et Tests**
   - Compilation du projet avec `mvn clean test`
   - Exécution automatique des tests unitaires et d'intégration
   - Si les tests échouent, le pipeline est marqué comme "rouge" (failed)
   - Si tous les tests passent, le pipeline est "vert" (success)

#### Résultat

- ✅ **Pipeline vert** : Code compilable et tous les tests passent
- ❌ **Pipeline rouge** : Erreur de compilation ou échec de tests

---

### Workflow CD : Build, Push & Deploy

**Fichier** : `.github/workflows/cd.yml`

#### Déclencheurs

Le workflow CD est déclenché automatiquement :

- ✅ Lorsque le workflow **CI réussit** (`workflow_run` avec `conclusion: success`)
- ✅ Uniquement après un push/merge réussi sur `main` 

#### Étapes principales

##### Job 1 : Build Docker Image

1. **Checkout du code**

   - Récupération du code source

2. **Configuration JDK 17**

   - Installation de Java 17 pour la compilation

3. **Build de l'application**

   - Compilation et packaging avec `mvn clean package -DskipTests`
   - Génération du JAR dans `target/`

4. **Authentification Docker Hub**

   - Connexion à Docker Hub avec les credentials secrets :
     - `DOCKERHUB_USERNAME`
     - `DOCKERHUB_TOKEN`

5. **Build et Push de l'image Docker**
   - Construction de l'image Docker à partir du Dockerfile
   - Tagging avec deux versions :
     - `gestion-incidents:latest` (dernière version)
     - `gestion-incidents:<commit-sha>` (version spécifique du commit)
   - Push de l'image vers Docker Hub

##### Job 2 : Déploiement

1. **Déploiement sur VM Azure**
   - Connexion SSH à la machine virtuelle de production
   - Secrets requis :
     - `VM_HOST` : Adresse IP ou domaine de la VM
     - `VM_USER` : Nom d'utilisateur SSH
     - `VM_SSH_KEY` : Clé privée SSH pour l'authentification
   - Exécution des commandes :
     ```bash
     cd ~/ville-intelligente
     docker compose pull  # Récupère la nouvelle image
     docker compose up -d  # Relance les conteneurs avec la nouvelle version
     ```

#### Résultat

- ✅ **Déploiement réussi** : Application déployée avec la nouvelle version
- ❌ **Déploiement échoué** : Logs disponibles dans GitHub Actions pour diagnostic

---

### Secrets GitHub Requis

Pour que le pipeline fonctionne, les secrets suivants doivent être configurés dans **GitHub → Settings → Secrets and variables → Actions** :

| Secret               | Description                                      |
| -------------------- | ------------------------------------------------ |
| `DOCKERHUB_USERNAME` | Nom d'utilisateur Docker Hub                     |
| `DOCKERHUB_TOKEN`    | Token d'authentification Docker Hub              |
| `VM_HOST`            | Adresse IP ou domaine de la VM de déploiement    |
| `VM_USER`            | Nom d'utilisateur SSH pour la VM                 |
| `VM_SSH_KEY`         | Clé privée SSH pour l'authentification sur la VM |

---

### Flux de Déploiement Complet

```
1. Développeur push sur main/dev
         ↓
2. Workflow CI déclenché
         ↓
3. Build + Tests exécutés
         ↓
   ✅ Succès → 4. Workflow CD déclenché
   ❌ Échec → Pipeline arrêté
         ↓
5. Build JAR + Build Image Docker
         ↓
6. Push image vers Docker Hub
         ↓
7. Déploiement sur VM (pull + restart)
         ↓
8. Application en production ✅
```

---

### Vérification du Pipeline

**Sur GitHub** :

1. Aller dans l'onglet **Actions**
2. Voir l'historique des workflows
3. Cliquer sur un workflow pour voir les détails
4. Vérifier le statut de chaque étape (✅ ou ❌)

**Localement** (tests avant push) :

```bash
# Exécuter les tests localement
mvn clean test

# Construire l'image Docker localement
docker build -t gestion-incidents:test .

# Tester avec docker-compose
docker-compose up
```

---

## 👨‍💻 Auteur

**Équipe de Développement**

Projet développé dans le cadre d'un projet académique/professionnel de gestion d'incidents urbains.
Mayssa Ben azzouz - Eya Ouni - Sirine Berrbibe

---

## 📄 Licence

Ce projet est un projet éducatif/démonstratif.

---
