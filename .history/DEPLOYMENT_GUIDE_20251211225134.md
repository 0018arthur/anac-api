# Guide de Déploiement de l'API ANAC sur Render

Ce guide vous explique étape par étape comment déployer votre API Spring Boot avec MySQL sur Render.

## 📋 Prérequis

1. Un compte Render (gratuit) : https://render.com
2. Un compte GitHub avec votre code poussé dans un repository
3. Une base de données MySQL externe (voir options ci-dessous)

## 🗄️ Options pour MySQL

Render ne propose pas MySQL en natif. Vous devez utiliser une base MySQL externe. Voici les meilleures options :

### Option 1 : PlanetScale (Recommandé - Gratuit)
- Site : https://planetscale.com
- Plan gratuit : 1 base de données, 1GB de stockage
- Avantages : MySQL compatible, très rapide, facile à configurer

### Option 2 : Aiven (Gratuit avec crédits)
- Site : https://aiven.io
- Plan gratuit : 300$ de crédits gratuits
- Avantages : MySQL géré, bonne performance

### Option 3 : Railway (Gratuit avec limites)
- Site : https://railway.app
- Plan gratuit : 5$ de crédits par mois
- Avantages : MySQL facile à déployer

### Option 4 : MySQL hébergé ailleurs
- Vous pouvez utiliser n'importe quelle instance MySQL accessible publiquement

## 🚀 Étapes de Déploiement

### Étape 1 : Préparer votre Base de Données MySQL

1. **Créer une base MySQL** sur votre service choisi (PlanetScale, Aiven, etc.)
2. **Notez les informations de connexion** :
   - Host (adresse du serveur)
   - Port (généralement 3306)
   - Nom de la base de données
   - Nom d'utilisateur
   - Mot de passe

### Étape 2 : Préparer votre Code

1. **Vérifiez que votre code est sur GitHub** :
   ```bash
   git add .
   git commit -m "Préparation pour déploiement Render"
   git push origin main
   ```

2. **Vérifiez que le fichier `render.yaml` est présent** à la racine du projet

### Étape 3 : Créer le Service sur Render

1. **Connectez-vous à Render** : https://dashboard.render.com

2. **Cliquez sur "New +"** puis **"Blueprint"**

3. **Connectez votre repository GitHub** :
   - Autorisez Render à accéder à votre compte GitHub
   - Sélectionnez le repository `anac-api`

4. **Render détectera automatiquement le fichier `render.yaml`**

5. **Cliquez sur "Apply"** pour créer les services

### Étape 4 : Configurer les Variables d'Environnement

Une fois le service créé, allez dans les **Settings** du service web `anac-api` :

1. **Allez dans "Environment"**

2. **Ajoutez les variables suivantes** :

   ```
   DB_HOST=votre-host-mysql.example.com
   DB_PORT=3306
   DB_NAME=anac_db
   DB_USERNAME=votre_username
   DB_PASSWORD=votre_password_secret
   
   SPRING_MAIL_USERNAME=votre-email@gmail.com
   SPRING_MAIL_PASSWORD=votre-app-password-gmail
   
   HUGGINGFACE_API_TOKEN=votre-token-huggingface
   
   APP_ALERT_EMAIL=gemailor136@gmail.com
   ```

   **Important** : 
   - Pour `DB_PASSWORD`, `SPRING_MAIL_PASSWORD`, et `HUGGINGFACE_API_TOKEN`, utilisez le type "Secret" dans Render
   - Ne commitez JAMAIS ces valeurs dans votre code

### Étape 5 : Configurer Gmail pour l'Envoi d'Emails

Si vous utilisez Gmail, vous devez créer un **App Password** :

1. Allez sur https://myaccount.google.com/apppasswords
2. Sélectionnez "Mail" et "Other (Custom name)"
3. Entrez "Render API" comme nom
4. Copiez le mot de passe généré (16 caractères)
5. Utilisez ce mot de passe dans `SPRING_MAIL_PASSWORD`

### Étape 6 : Déployer

1. **Retournez dans le dashboard Render**
2. **Cliquez sur "Manual Deploy"** → **"Deploy latest commit"**
3. **Surveillez les logs** pour voir le processus de build et de démarrage

### Étape 7 : Vérifier le Déploiement

1. **Attendez que le déploiement soit terminé** (peut prendre 5-10 minutes la première fois)
2. **Vérifiez l'URL** : Render vous donnera une URL comme `https://anac-api.onrender.com`
3. **Testez votre API** :
   ```
   https://anac-api.onrender.com/api/v1/swagger-ui.html
   ```
   (pour accéder à Swagger UI)

## 🔧 Configuration Avancée

### Gestion des Uploads de Fichiers

Le répertoire `/tmp/uploads/` est temporaire sur Render. Pour un stockage permanent :

1. **Option 1** : Utiliser un service de stockage cloud (AWS S3, Cloudinary, etc.)
2. **Option 2** : Utiliser Render Disk (payant)

### Monitoring et Logs

- **Logs** : Disponibles dans le dashboard Render sous "Logs"
- **Métriques** : Disponibles dans "Metrics" (plan gratuit limité)

### Mises à Jour Automatiques

Par défaut, Render déploie automatiquement à chaque push sur la branche `main`.

Pour désactiver :
1. Allez dans **Settings** → **Build & Deploy**
2. Désactivez **"Auto-Deploy"**

## 🐛 Dépannage

### L'application ne démarre pas

1. **Vérifiez les logs** dans le dashboard Render
2. **Vérifiez les variables d'environnement** sont correctement définies
3. **Vérifiez la connexion MySQL** :
   - Assurez-vous que l'IP de Render est autorisée dans votre base MySQL
   - Vérifiez que les credentials sont corrects

### Erreur de connexion à la base de données

1. **Vérifiez que votre base MySQL accepte les connexions externes**
2. **Vérifiez le firewall** de votre fournisseur MySQL
3. **Testez la connexion** avec un client MySQL depuis votre machine

### Build échoue

1. **Vérifiez les logs de build** dans Render
2. **Assurez-vous que Java 17 est utilisé** (vérifié dans `pom.xml`)
3. **Vérifiez que Maven peut télécharger les dépendances**

## 📝 Notes Importantes

- **Plan Gratuit** : L'application se met en veille après 15 minutes d'inactivité
- **Premier démarrage** : Peut prendre 30-60 secondes après la veille
- **Limites** : 750 heures gratuites par mois (suffisant pour un service 24/7)
- **SSL** : Automatiquement configuré par Render (HTTPS)

## 🔐 Sécurité

1. **Ne commitez JAMAIS** :
   - Mots de passe
   - Tokens API
   - Clés secrètes JWT

2. **Utilisez les variables d'environnement** pour toutes les valeurs sensibles

3. **Activez 2FA** sur votre compte Render

## 📞 Support

- Documentation Render : https://render.com/docs
- Support Render : support@render.com
- Status Page : https://status.render.com

---

**Bon déploiement ! 🚀**

