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
- Accès à toutes les statistiques globales

### 📝 Gestion des Incidents

#### Création d'Incident

- Formulaire intuitif avec validation en temps réel
- Description détaillée (10-1000 caractères)
- Sélection du département (catégorie)
- Sélection du quartier
- Géolocalisation interactive via carte Leaflet
- Upload de photos (jusqu'à 3 photos, max 5MB chacune)
- Validation côté client et serveur

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
- Pagination pour les grandes listes
- Recherche et tri
- Historique complet des modifications
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
- Filtrage par type et date

### 📸 Upload et Gestion des Images

- **Upload multiple** : Jusqu'à 3 photos par incident
- **Validation** : Type (images uniquement), taille (max 5MB)
- **Stockage** : Fichiers stockés dans le dossier `uploads/`
- **Photo principale** : Première photo marquée comme principale
- **Affichage** : Prévisualisation avant upload, galerie dans les détails
- **Sécurité** : Noms de fichiers uniques avec timestamp

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
- **iText PDF** : Génération de rapports PDF
- **Apache POI** : Génération de fichiers Excel
- **Leaflet** : Cartographie interactive
- **Spring DevTools** : Rechargement automatique en développement

### Frontend

- **Thymeleaf** : Templates HTML côté serveur
- **HTML5/CSS3** : Structure et style
- **JavaScript (Vanilla)** : Interactivité côté client
- **Leaflet.js** : Cartes interactives
- **Bootstrap** (optionnel) : Framework CSS

---

## 📊 Diagramme de Classes

Le projet inclut un diagramme de classes complet au format PlantUML et Mermaid :

- **`diagramme-classes.puml`** : Fichier PlantUML (compatible IntelliJ, VS Code, draw.io)
- **`diagramme-classes.md`** : Diagramme Mermaid (affichage direct sur GitHub)
- **`DIAGRAMME_CLASSES.md`** : Documentation complète du diagramme

### Visualisation

Pour visualiser le diagramme PlantUML :

- **IntelliJ IDEA** : Installer le plugin "PlantUML integration"
- **VS Code** : Installer l'extension "PlantUML"
- **En ligne** : [PlantText](https://www.planttext.com/)

Le diagramme inclut :

- ✅ Toutes les entités principales (Utilisateur, Incident, Photo, Notification, etc.)
- ✅ Toutes les énumérations (Role, StatutIncident, PrioriteIncident, etc.)
- ✅ Toutes les relations avec leurs cardinalités
- ✅ Attributs principaux de chaque classe

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

---

## 🗄️ Base de Données

### Entités Principales

#### 📋 **Incident**

Table centrale du système. Représente un incident signalé par un citoyen.

**Champs principaux** :

- `id` : Identifiant unique
- `description` : Description détaillée de l'incident
- `statut` : Statut actuel (enum `StatutIncident`)
- `priorite` : Priorité (enum `PrioriteIncident`)
- `latitude` / `longitude` : Coordonnées GPS
- `dateDeclaration` : Date de création
- `dateDerniereMiseAJour` : Dernière modification
- `dateResolutionEstimee` : Date estimée de résolution
- `dateResolution` : Date réelle de résolution

**Relations** :

- `@ManyToOne` → `Utilisateur citoyen` : Citoyen qui a déclaré l'incident
- `@ManyToOne` → `Utilisateur agent` : Agent assigné (peut être null)
- `@ManyToOne` → `Departement` : Département concerné (obligatoire)
- `@ManyToOne` → `Quartier` : Quartier concerné (obligatoire)
- `@ManyToOne` → `ServiceMunicipal` : Service assigné (peut être null)
- `@OneToMany` → `List<Photo>` : Photos associées
- `@OneToMany` → `List<Notification>` : Notifications liées

#### 👤 **Utilisateur**

Représente tous les utilisateurs du système (citoyens, agents, admins, super-admins).

**Champs principaux** :

- `id` : Identifiant unique
- `prenom` / `nom` : Nom complet
- `email` : Email (unique, utilisé pour la connexion)
- `motDePasse` : Mot de passe hashé (BCrypt)
- `role` : Rôle de l'utilisateur (enum `Role`)
- `emailVerifie` : Statut de vérification de l'email
- `authProvider` : Source d'authentification (LOCAL ou GOOGLE)
- `providerId` : ID du fournisseur OAuth2
- `imageUrl` : URL de la photo de profil (OAuth2)

**Relations** :

- `@ManyToOne` → `Departement` : Département (pour ADMIN)
- `@ManyToOne` → `ServiceMunicipal` : Service (pour AGENT)
- `@OneToMany` → `List<Notification>` : Notifications reçues
- `@OneToOne` → `PreferenceNotification` : Préférences de notification

#### 🔔 **Notification**

Notifications envoyées aux utilisateurs.

**Champs principaux** :

- `id` : Identifiant unique
- `type` : Type de notification (enum `TypeNotification`)
- `message` : Message de la notification
- `dateEnvoi` : Date d'envoi
- `lu` : Statut de lecture

**Relations** :

- `@ManyToOne` → `Utilisateur` : Destinataire
- `@ManyToOne` → `Incident` : Incident concerné

#### 📸 **Photo**

Photos associées aux incidents.

**Champs principaux** :

- `id` : Identifiant unique
- `nomFichier` : Nom original du fichier
- `typeContenu` : Type MIME (image/jpeg, etc.)
- `cheminStockage` : Chemin relatif dans `uploads/`
- `dateUpload` : Date d'upload
- `principale` : Indique si c'est la photo principale

**Relations** :

- `@ManyToOne` → `Incident` : Incident associé

#### 🏢 **Departement**

Départements municipaux (catégories d'incidents).

**Champs principaux** :

- `id` : Identifiant unique
- `nom` : Nom du département (unique)
- `description` : Description
- `email` : Email de contact
- `telephone` : Téléphone de contact

**Relations** :

- `@OneToMany` → `List<ServiceMunicipal>` : Services du département
- `@OneToMany` → `List<Incident>` : Incidents du département

#### 🏘️ **Quartier**

Quartiers de la ville.

**Champs principaux** :

- `id` : Identifiant unique
- `nom` : Nom du quartier (unique)
- `codePostal` : Code postal (4 chiffres)

**Relations** :

- `@ManyToOne` → `Departement` : Département auquel appartient le quartier
- `@OneToMany` → `List<Incident>` : Incidents du quartier

#### 🏛️ **ServiceMunicipal**

Services municipaux (ex: Voirie, Éclairage, Propreté).

**Champs principaux** :

- `id` : Identifiant unique
- `nom` : Nom du service
- `description` : Description

**Relations** :

- `@ManyToOne` → `Departement` : Département parent
- `@OneToMany` → `List<Utilisateur>` : Agents du service
- `@OneToMany` → `List<Incident>` : Incidents assignés

### Relations Principales

```
Utilisateur (CITOYEN)
    ↓ (1-N)
Incident
    ↓ (1-N)        ↓ (N-1)
Photo          Departement
                    ↓ (1-N)
                ServiceMunicipal
                    ↓ (1-N)
                Utilisateur (AGENT)

Incident
    ↓ (1-N)
Notification
    ↓ (N-1)
Utilisateur
```

---

## 📋 DTO et Validation

### Rôle des DTOs

Les **DTOs (Data Transfer Objects)** sont utilisés pour :

1. **Sécurité** : Ne pas exposer directement les entités JPA (éviter les fuites de données)
2. **Performance** : Ne transférer que les données nécessaires
3. **Validation** : Valider les données avant traitement
4. **Flexibilité** : Structure différente de l'entité si besoin

### Exemple : `IncidentCreateDto`

```java
@Data
public class IncidentCreateDto {
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    private Long departementId;

    @NotNull(message = "Le quartier est obligatoire")
    private Long quartierId;

    @NotNull(message = "La latitude est obligatoire")
    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0", message = "Latitude invalide")
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0", message = "Longitude invalide")
    private Double longitude;

    private List<MultipartFile> photos;
}
```

### Annotations de Validation Utilisées

- **`@NotNull`** : Champ obligatoire (non null)
- **`@NotBlank`** : Chaîne non vide (non null, non vide, non blanc)
- **`@Size(min, max)`** : Taille de chaîne ou collection
- **`@DecimalMin` / `@DecimalMax`** : Valeur minimale/maximale pour nombres
- **`@Email`** : Format email valide
- **`@Pattern`** : Expression régulière

### Validation dans les Contrôleurs

```java
@PostMapping("/incident/ajouter")
public String ajouterIncident(
    @Valid @ModelAttribute("incident") IncidentCreateDto dto,
    BindingResult bindingResult,
    Model model
) {
    if (bindingResult.hasErrors()) {
        // Retourner au formulaire avec les erreurs
        return "citoyen/incident_form";
    }
    // Traitement...
}
```

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
```

#### Configuration des Accès (`SecurityConfig`)

```java
.authorizeRequests()
    // Public
    .antMatchers("/auth/login", "/auth/register").permitAll()

    // Super Admin
    .antMatchers("/superadmin/**").hasRole("SUPERADMIN")

    // Admin
    .antMatchers("/admin/**").hasRole("ADMIN")

    // Citoyen
    .antMatchers("/citoyen/**").hasRole("CITOYEN")

    // Agent
    .antMatchers("/agent/**").hasRole("AGENT")

    // Autres
    .anyRequest().authenticated()
```

#### Vérification dans les Contrôleurs

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/dashboard")
public String dashboard() {
    // ...
}
```

### Protection CSRF

- Désactivée pour simplifier (à activer en production)
- Protection contre les injections SQL via JPA/Hibernate
- Validation des entrées avec Bean Validation

---

## 🚀 Installation et Exécution

### Prérequis

- **Java 17** ou supérieur
- **Maven 3.6+**
- **MySQL 8.0+**
- **IDE** (IntelliJ IDEA, Eclipse, VS Code) recommandé

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
# Compiler le projet
mvn clean compile

# Exécuter les tests
mvn test

# Créer un JAR exécutable
mvn clean package

# Lancer l'application
mvn spring-boot:run

# Nettoyer le projet
mvn clean
```

### Configuration OAuth2 Google (Optionnel)

1. Aller sur [Google Cloud Console](https://console.cloud.google.com/)
2. Créer un nouveau projet
3. Activer l'API Google+
4. Créer des identifiants OAuth 2.0
5. Ajouter l'URI de redirection : `http://localhost:8080/login/oauth2/code/google`
6. Copier le Client ID et Client Secret dans `application.properties`

---

## ✅ Bonnes Pratiques Utilisées

### 1. Séparation des Responsabilités

- **Contrôleurs** : Gestion HTTP uniquement
- **Services** : Logique métier
- **Repositories** : Accès aux données
- **DTOs** : Transfert de données
- **Mappers** : Conversion Entity ↔ DTO

### 2. Utilisation des DTOs

- Pas d'exposition directe des entités JPA
- Validation avec Bean Validation
- Structure adaptée aux besoins de l'API/UI

### 3. Gestion des Exceptions

- `GlobalExceptionHandler` pour gérer toutes les exceptions
- Exceptions métier personnalisées (`NotFoundException`, `UnauthorizedException`)
- Messages d'erreur clairs pour l'utilisateur

### 4. Transactions

- `@Transactional` sur les méthodes de service
- Rollback automatique en cas d'erreur
- Isolation des transactions

### 5. Logging

- Utilisation de SLF4J avec Lombok `@Slf4j`
- Logs structurés pour le débogage
- Aspect `LoggingAspect` pour logger les appels de méthodes

### 6. Validation

- Validation côté client (JavaScript)
- Validation côté serveur (Bean Validation)
- Messages d'erreur personnalisés

### 7. Sécurité

- Hashage des mots de passe (BCrypt)
- Vérification d'email obligatoire
- Protection des routes par rôle
- Injection sécurisée des dépendances

### 8. Code Propre

- Utilisation de Lombok pour réduire le boilerplate
- Noms de méthodes explicites
- Commentaires pour les parties complexes
- Structure de packages logique

---

## 🔮 Améliorations Futures

### Court Terme

- [ ] Activation de la protection CSRF
- [ ] Tests unitaires et d'intégration
- [ ] Documentation API avec Swagger/OpenAPI
- [ ] Amélioration de l'interface utilisateur (responsive design)
- [ ] Gestion des erreurs plus robuste

### Moyen Terme

- [ ] API REST complète (JSON) en plus de Thymeleaf
- [ ] Application mobile (React Native / Flutter)
- [ ] Système de commentaires sur les incidents
- [ ] Notifications SMS
- [ ] Export de données en CSV/Excel amélioré
- [ ] Recherche full-text sur les incidents

### Long Terme

- [ ] Intégration avec des systèmes externes (CRM municipal)
- [ ] Analyse prédictive avec Machine Learning
- [ ] Tableau de bord temps réel avec WebSocket
- [ ] Multi-tenancy (plusieurs villes)
- [ ] Système de réputation pour les citoyens
- [ ] Intégration avec des capteurs IoT

---

## 👨‍💻 Auteur

**Équipe de Développement**

Projet développé dans le cadre d'un projet académique/professionnel de gestion d'incidents urbains.

---

## 📄 Licence

Ce projet est un projet éducatif/démonstratif. Tous droits réservés.

---

## 📞 Support

Pour toute question ou problème :

- Ouvrir une issue sur le repository
- Contacter l'équipe de développement

---

**Dernière mise à jour** : 2024
