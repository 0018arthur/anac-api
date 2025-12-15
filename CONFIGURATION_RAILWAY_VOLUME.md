# Configuration d'un Volume Persistant sur Railway

## Problème

Les images sont stockées dans `/tmp/uploads/incidents/`, un répertoire temporaire qui est nettoyé lors des redémarrages sur Railway, causant la perte des fichiers.

## Solution : Volume Persistant Railway

Railway offre des **volumes persistants** qui conservent les données entre les redémarrages et les déploiements.

## Étapes de Configuration

### Étape 1 : Créer un Volume sur Railway

1. Connectez-vous à votre projet sur [Railway](https://railway.app/)
2. Allez dans votre service (l'API)
3. Cliquez sur l'onglet **"Volumes"** dans le menu latéral
4. Cliquez sur **"New Volume"**
5. Configurez le volume :
   - **Name** : `anac-uploads` (ou un nom de votre choix)
   - **Mount Path** : `/data` (c'est le chemin où le volume sera monté dans le conteneur)
   - **Size** : Choisissez la taille selon vos besoins (1GB devrait suffire pour commencer)
6. Cliquez sur **"Create"**

### Étape 2 : Configurer la Variable d'Environnement

1. Dans votre service Railway, allez dans l'onglet **"Variables"**
2. Ajoutez ou modifiez la variable suivante :

```
UPLOAD_DIR=/data/uploads/incidents/
```

**Important** : Le chemin doit commencer par `/data` car c'est là que le volume est monté.

### Étape 3 : Redéployer l'Application

1. Railway redéploiera automatiquement votre application
2. Les nouvelles images seront maintenant stockées dans le volume persistant `/data/uploads/incidents/`
3. Ces fichiers seront conservés même après les redémarrages

## Vérification

Après le déploiement :

1. Uploadez une nouvelle image d'incident
2. Vérifiez que l'image est accessible via l'URL
3. Redémarrez votre service Railway
4. Vérifiez que l'image est toujours accessible (elle devrait l'être maintenant !)

## Avantages

✅ **Pas besoin d'AWS S3** - Solution intégrée à Railway  
✅ **Gratuit** - Les volumes sont inclus dans le plan Railway  
✅ **Simple** - Configuration en quelques clics  
✅ **Persistant** - Les fichiers survivent aux redémarrages  

## Limitations

⚠️ **Taille limitée** - Selon votre plan Railway, la taille du volume peut être limitée  
⚠️ **Liaison au service** - Le volume est lié à un service spécifique  
⚠️ **Pas de sauvegarde automatique** - Pensez à faire des sauvegardes si nécessaire  

## Migration des Images Existantes

Les images déjà uploadées dans `/tmp/` seront perdues lors du prochain redémarrage. Pour migrer les images existantes :

1. Téléchargez-les depuis votre base de données
2. Re-uploadez-les via l'API (elles seront maintenant dans le volume persistant)

## Alternative : Si les Volumes ne sont pas Disponibles

Si votre plan Railway ne supporte pas les volumes persistants, vous avez deux options :

1. **Upgrader votre plan Railway** pour avoir accès aux volumes
2. **Utiliser AWS S3** (voir le guide CONFIGURATION_S3.md si vous changez d'avis)

## Dépannage

### Le volume n'apparaît pas dans le conteneur

- Vérifiez que le volume est bien créé et attaché au service
- Vérifiez que le `Mount Path` est `/data`
- Vérifiez les logs Railway pour voir les erreurs de montage

### Les fichiers ne sont pas persistants

- Vérifiez que `UPLOAD_DIR` est bien configuré sur `/data/uploads/incidents/`
- Vérifiez que le volume est bien monté en regardant les logs au démarrage
- Assurez-vous que le répertoire existe (il sera créé automatiquement par l'application)

### Erreur de permissions

- Railway devrait gérer les permissions automatiquement
- Si vous avez des problèmes, vérifiez que l'application a les droits d'écriture dans `/data`

