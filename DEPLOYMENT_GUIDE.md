# Guide de Déploiement de l'API ANAC sur Render

Ce guide vous explique étape par étape comment déployer votre API Spring Boot avec PostgreSQL sur Render.

## 📋 Prérequis

1. Un compte Render (gratuit) : https://render.com
2. Un compte GitHub avec votre code poussé dans un repository
3. **Aucune base de données externe nécessaire** - Render gère PostgreSQL nativement !

## 🗄️ Base de Données PostgreSQL

✅ **Avantage** : Render propose PostgreSQL en natif, donc pas besoin de service externe !
- La base de données sera créée automatiquement lors du déploiement
- Les variables d'environnement seront configurées automatiquement
- Gratuit avec le plan free (1GB de stockage)

## 🚀 Étapes de Déploiement

### Étape 1 : Préparer votre Code

**Aucune préparation de base de données nécessaire !** Render créera automatiquement la base PostgreSQL.

1. **Vérifiez que votre code est sur GitHub** :
   ```bash
   git add .
   git commit -m "Configuration pour déploiement Render avec PostgreSQL"
   git push origin main
   ```

2. **Vérifiez que le fichier `render.yaml` est présent** à la racine du projet

### Étape 2 : Créer le Service sur Render

1. **Connectez-vous à Render** : https://dashboard.render.com

2. **Cliquez sur "New +"** puis **"Blueprint"**

3. **Connectez votre repository GitHub** :
   - Autorisez Render à accéder à votre compte GitHub
   - Sélectionnez le repository `anac-api`

4. **Render détectera automatiquement le fichier `render.yaml`**

5. **Cliquez sur "Apply"** pour créer les services
   - ✅ Render créera automatiquement :
     - Le service PostgreSQL (`anac-postgres-db`)
     - Le service Web (`anac-api`)
     - Les variables d'environnement de connexion à la base seront configurées automatiquement !

### Étape 3 : Configurer les Variables d'Environnement Manuelles

Une fois le service créé, allez dans les **Settings** du service web `anac-api` :

1. **Allez dans "Environment"**

2. **Les variables de base de données sont déjà configurées automatiquement !** ✅

3. **Ajoutez uniquement les variables suivantes** (non liées à la base de données) :

   ```
   SPRING_MAIL_USERNAME=votre-email@gmail.com
   SPRING_MAIL_PASSWORD=votre-app-password-gmail
   
   HUGGINGFACE_API_TOKEN=votre-token-huggingface
   
   APP_ALERT_EMAIL=gemailor136@gmail.com
   ```

   **Important** : 
   - Pour `SPRING_MAIL_PASSWORD` et `HUGGINGFACE_API_TOKEN`, utilisez le type "Secret" dans Render
   - Ne commitez JAMAIS ces valeurs dans votre code
   - Les variables `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` sont déjà configurées automatiquement par Render !

### Étape 4 : Configurer Gmail pour l'Envoi d'Emails

Si vous utilisez Gmail, vous devez créer un **App Password** :

1. Allez sur https://myaccount.google.com/apppasswords
2. Sélectionnez "Mail" et "Other (Custom name)"
3. Entrez "Render API" comme nom
4. Copiez le mot de passe généré (16 caractères)
5. Utilisez ce mot de passe dans `SPRING_MAIL_PASSWORD`

### Étape 5 : Déployer

1. **Retournez dans le dashboard Render**
2. **Cliquez sur "Manual Deploy"** → **"Deploy latest commit"**
3. **Surveillez les logs** pour voir le processus de build et de démarrage

### Étape 6 : Vérifier le Déploiement

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
3. **Vérifiez la connexion PostgreSQL** :
   - Assurez-vous que le service PostgreSQL est démarré
   - Vérifiez que les variables d'environnement de la base sont bien configurées automatiquement

### Erreur de connexion à la base de données

1. **Vérifiez que le service PostgreSQL est bien démarré** dans le dashboard Render
2. **Vérifiez les variables d'environnement** dans les Settings du service web
3. **Vérifiez les logs** du service PostgreSQL pour voir s'il y a des erreurs
4. **Assurez-vous que les deux services sont dans la même région** (frankfurt par défaut)

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

