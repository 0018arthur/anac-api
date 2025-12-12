# Migration de MySQL vers PostgreSQL

Ce document décrit les changements effectués pour migrer de MySQL vers PostgreSQL pour le déploiement sur Render.

## ✅ Changements Effectués

### 1. Configuration (`render.yaml`)
- ✅ Ajout d'un service PostgreSQL natif (`pspg`)
- ✅ Configuration automatique des variables d'environnement de connexion
- ✅ Suppression de la nécessité d'une base MySQL externe

### 2. Configuration de Production (`application-prod.properties`)
- ✅ Changement du driver : `com.mysql.cj.jdbc.Driver` → `org.postgresql.Driver`
- ✅ Changement de l'URL JDBC : `jdbc:mysql://` → `jdbc:postgresql://`
- ✅ Changement du dialect Hibernate : `MySQLDialect` → `PostgreSQLDialect`
- ✅ Ajout de `sslmode=require` pour la sécurité
- ✅ Ajout de `hibernate.jdbc.lob.non_contextual_creation=true` pour PostgreSQL

### 3. Dépendances (`pom.xml`)
- ✅ PostgreSQL déjà présent dans les dépendances
- ℹ️ MySQL reste présent pour le développement local (optionnel)

## 🔄 Compatibilité du Code

### Requêtes JPQL/HQL
✅ **Toutes les requêtes sont compatibles** car elles utilisent JPQL standard :
- `CONCAT()` fonctionne aussi en PostgreSQL
- Les fonctions JPA sont portables
- Les types de données JPA sont mappés automatiquement

### Entités JPA
✅ **Toutes les entités sont compatibles** :
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` fonctionne avec PostgreSQL
- Les types Java (`String`, `Long`, `LocalDateTime`, `UUID`) sont mappés correctement
- `columnDefinition = "TEXT"` est supporté par PostgreSQL

### Types de Données
| MySQL | PostgreSQL | Statut |
|-------|------------|--------|
| `TEXT` | `TEXT` | ✅ Compatible |
| `VARCHAR` | `VARCHAR` | ✅ Compatible |
| `BIGINT` | `BIGINT` | ✅ Compatible |
| `DATETIME` | `TIMESTAMP` | ✅ Mappé automatiquement par JPA |
| `UUID` | `UUID` | ✅ Compatible |

## 📝 Notes Importantes

### Développement Local
- Le fichier `application.properties` reste configuré pour MySQL pour le développement local
- Pour tester avec PostgreSQL localement, créez un fichier `application-local.properties` avec la config PostgreSQL

### Migration des Données
Si vous avez déjà des données en MySQL et souhaitez les migrer vers PostgreSQL :

1. **Exporter les données MySQL** :
   ```bash
   mysqldump -u root -p anac_db > dump.sql
   ```

2. **Convertir le dump pour PostgreSQL** :
   - Utilisez un outil comme `pgloader` ou `mysql2pgsql`
   - Ou convertissez manuellement les différences de syntaxe

3. **Importer dans PostgreSQL** :
   ```bash
   psql -U anac_user -d anac_db -f converted_dump.sql
   ```

### Différences Potentielles

1. **Sensibilité à la casse** :
   - MySQL : Insensible par défaut
   - PostgreSQL : Sensible par défaut
   - ✅ Résolu : Les noms de tables/colonnes en minuscules dans les entités

2. **Backticks vs Double Quotes** :
   - MySQL utilise les backticks `` ` ``
   - PostgreSQL utilise les double quotes `"`
   - ✅ Résolu : JPA gère cela automatiquement

3. **Fonctions de chaînes** :
   - `CONCAT()` : ✅ Supporté par les deux
   - `LOWER()` : ✅ Supporté par les deux
   - `LIKE` : ✅ Supporté par les deux

## 🚀 Déploiement

Le déploiement sur Render est maintenant simplifié :
1. ✅ Pas besoin de service externe pour la base de données
2. ✅ Configuration automatique des variables d'environnement
3. ✅ Base de données créée automatiquement
4. ✅ SSL/TLS configuré automatiquement

## 🔍 Vérification Post-Déploiement

Après le déploiement, vérifiez :
1. ✅ Les tables sont créées correctement (`ddl-auto=update`)
2. ✅ Les données sont accessibles
3. ✅ Les requêtes fonctionnent correctement
4. ✅ Les relations JPA sont préservées

---

**Migration réussie ! 🎉**

