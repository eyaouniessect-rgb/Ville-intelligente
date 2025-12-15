#!/bin/bash

# Script pour corriger automatiquement tous les templates Thymeleaf
# pour compatibilité OAuth2

echo "🔍 Recherche des fichiers HTML dans templates..."

# Trouver tous les fichiers HTML
find src/main/resources/templates -name "*.html" | while read file; do
    echo "📝 Traitement de $file..."

    # Remplacer sec:authentication="principal.username"
    sed -i 's/sec:authentication="principal\.username"/th:text="${utilisateur.email}"/g' "$file"

    # Remplacer ${#authentication.principal.username}
    sed -i 's/\${#authentication\.principal\.username}/${utilisateur.email}/g' "$file"

    # Remplacer sec:authentication="name"
    sed -i 's/sec:authentication="name"/th:text="${utilisateur.prenom + '\'' '\'' + utilisateur.nom}"/g' "$file"

    # Remplacer ${#authentication.name}
    sed -i 's/\${#authentication\.name}/${utilisateur.prenom + " " + utilisateur.nom}/g' "$file"
done

echo "✅ Correction terminée !"
echo ""
echo "⚠️  IMPORTANT : Vérifiez que tous vos contrôleurs ajoutent 'utilisateur' au modèle :"
echo "   model.addAttribute(\"utilisateur\", utilisateurService.findByEmail(email));"