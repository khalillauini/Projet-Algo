# Rapport Technique — Mini Projet Algorithmique
## Moteur de Recherche Morphologique et Générateur de Dérivation Arabe

**Année universitaire :** 2025-2026  
**Niveau :** 1ING — Département GLSI  
**Enseignants :** Narjes Ben Hariz, Sahbi Bahroun

---

## 1. Présentation des Structures de Données

### 1.1 Arbre AVL des Racines

Les racines arabes trilitères sont indexées dans un **arbre AVL** (Adelson-Velsky & Landis), variante des arbres binaires de recherche garantissant l'équilibre automatique.

**Structure d'un nœud :**

```
AVLNode {
    String root              // Racine arabe (ex: كتب)
    List<String> derivedWords // Dérivés validés associés
    int frequency            // Nombre de dérivés validés
    AVLNode left, right      // Fils gauche et droit
    int height               // Hauteur du sous-arbre
}
```

**Propriété AVL :** Pour chaque nœud, |hauteur(fils gauche) − hauteur(fils droit)| ≤ 1.

La comparaison des racines utilise `String.compareTo()` sur les codes Unicode arabes (U+0600–U+06FF), assurant un ordre lexicographique cohérent avec l'alphabet arabe.

**Rotations d'équilibrage :**
- Rotation simple droite (cas Gauche-Gauche)
- Rotation simple gauche (cas Droite-Droite)
- Rotation double gauche-droite (cas Gauche-Droite)
- Rotation double droite-gauche (cas Droite-Gauche)

---

### 1.2 Table de Hachage des Schèmes

Les schèmes morphologiques sont stockés dans une **table de hachage à chaînage** implémentée manuellement.

**Structure :**

```
SchemeHashTable {
    Entry[] buckets         // Tableau de listes chaînées
    int capacity = 16       // Capacité initiale (doublée si charge > 0.75)
    int size                // Nombre de schèmes stockés
}

Entry {
    String key   // Nom arabe du schème (ex: فاعل)
    Scheme value // Objet schème complet
    Entry  next  // Maillon suivant (chaînage)
}
```

**Fonction de hachage polynomiale :**

```
h(k) = (Σ k[i] × 31^(n−1−i)) mod capacity
```

Ce choix de base 31 est classique en Java (même principe que `String.hashCode()`) et offre une bonne distribution sur les caractères arabes Unicode.

**Résolution des collisions :** Chaînage séparé. En cas de collision, les éléments sont ajoutés en tête de la liste du bucket correspondant (insertion O(1)).

**Facteur de charge cible :** 0,75. Au-delà, la capacité est doublée et tous les éléments sont re-hachés.

---

### 1.3 Liste des Dérivés Validés

Chaque nœud AVL contient une `ArrayList<String>` des mots dérivés validés, mise à jour automatiquement lors des opérations de génération ou de validation. Un compteur de fréquence est maintenu pour mesurer la richesse lexicale de chaque racine.

---

## 2. Description des Algorithmes

### 2.1 Algorithme de Génération Morphologique

Le moteur de dérivation repose sur un système de **templates de transformation** :

```
Template : '1' → C1 (1ère consonne), '2' → C2, '3' → C3
           autres caractères → lettres fixes du schème

Exemple : فاعل  → template "1ا2ِ3"
          Racine كتب → C1=ك, C2=ت, C3=ب
          Résultat : ك + ا + ت + ِ + ب = كاتِب ✓
```

**Pseudocode :**

```
FONCTION appliquer(template, consonnes[3]) :
    résultat ← ""
    POUR chaque char c dans template :
        SI c == '1' : résultat += consonnes[0]
        SINON SI c == '2' : résultat += consonnes[1]
        SINON SI c == '3' : résultat += consonnes[2]
        SINON : résultat += c
    RETOURNER résultat
```

---

### 2.2 Algorithme de Validation Morphologique

La validation inverse le processus de génération en tentant d'extraire les consonnes d'un mot et de les comparer à la racine fournie.

**Pseudocode :**

```
FONCTION valider(mot, racine) :
    consonnes_racine ← extraire_consonnes(racine)
    POUR chaque schème S dans la table :
        consonnes_extraites ← inverser_template(S.template, mot)
        SI consonnes_extraites == consonnes_racine :
            RETOURNER (OUI, S)
    RETOURNER (NON, null)
```

L'inversion du template vérifie lettre par lettre que les positions fixes correspondent, et extrait les consonnes aux positions variables (1, 2, 3).

---

### 2.3 Algorithme d'Analyse (Décomposition)

Pour un mot inconnu, le moteur parcourt tous les schèmes disponibles et tente une décomposition :

```
FONCTION analyser(mot) :
    résultats ← []
    POUR chaque schème S :
        consonnes ← inverser_template(S.template, mot)
        SI consonnes ≠ null :
            racine ← nouvelle_chaine(consonnes)
            dans_arbre ← AVL.rechercher(racine) ≠ null
            résultats.ajouter(S, racine, dans_arbre)
    RETOURNER résultats
```

---

## 3. Justification des Choix Algorithmiques

| Choix | Alternative | Justification |
|-------|-------------|---------------|
| **AVL** pour les racines | BST non équilibré | Garantit O(log n) dans tous les cas, évite la dégénérescence en liste |
| **AVL** plutôt que B-tree | B-tree, Red-Black | Implémentation plus simple, efficace pour des milliers de racines |
| **Table de hachage** pour les schèmes | Tableau trié + recherche binaire | O(1) amorti contre O(log n), idéal pour un petit ensemble fixe |
| **Chaînage** pour les collisions | Adressage ouvert | Plus simple à implémenter, performance stable sous haute charge |
| **Templates '1','2','3'** | Expressions régulières | Simples, rapides, inversibles — permettent la validation sans parsing complexe |

---

## 4. Analyse de la Complexité Algorithmique

### Arbre AVL

| Opération | Complexité Temporelle | Complexité Spatiale |
|-----------|----------------------|---------------------|
| Insertion | O(log n) | O(1) |
| Recherche | O(log n) | O(1) |
| Suppression | O(log n) | O(1) |
| Affichage ordonné | O(n) | O(log n) (pile récursion) |

Où **n** = nombre de racines indexées.

### Table de Hachage

| Opération | Cas moyen | Cas pire |
|-----------|-----------|----------|
| put / get / delete | O(1) | O(n) (toutes les clés dans le même bucket) |
| Redimensionnement | O(n) | O(n) |
| Listage complet | O(n + capacité) | O(n + capacité) |

### Moteur de Dérivation

| Opération | Complexité |
|-----------|------------|
| Générer 1 dérivé | O(|template|) ≈ O(1) |
| Générer tous les dérivés | O(s × |template|) ≈ O(s) |
| Valider un mot | O(s × |template|) ≈ O(s) |

Où **s** = nombre de schèmes dans la table.

---

## 5. Principales Difficultés Rencontrées

### 5.1 Gestion de l'Unicode Arabe
L'arabe est une langue boustrophédon (droite-vers-gauche) avec des caractères composés. La plage Unicode U+0600–U+06FF comprend à la fois les lettres consonantiques et les diacritiques (harakat : U+064B–U+065F). Il a fallu implémenter un filtre spécifique pour isoler les consonnes lors de l'extraction des racines.

### 5.2 Conception des Templates
Le système de templates (1, 2, 3) est une simplification nécessaire : la morphologie arabe réelle fait intervenir des règles phonologiques complexes (assimilation, gémination, faibles). Les schèmes implémentés couvrent les formes les plus régulières et courantes.

### 5.3 Validation Morphologique Inverse
L'inversion du template requiert que la longueur du mot corresponde exactement à celle du template, et que toutes les lettres fixes soient identiques. Les mots avec harakat (diacritiques) écrits ou omis peuvent créer des discordances ; un prétraitement de normalisation s'avère donc important en pratique.

### 5.4 Équilibrage AVL
L'implémentation de l'équilibrage AVL (4 cas de rotation) avec la mise à jour correcte des hauteurs et la propagation récursive a nécessité une attention particulière pour éviter les erreurs de pointeurs lors de la suppression de nœuds avec deux fils.

---

## 6. Fonctionnalités Additionnelles Implémentées

- **Analyse inverse** : décomposition d'un mot quelconque pour identifier toutes les paires (schème, racine) possibles.
- **Chargement par lot** : insertion d'un ensemble de racines depuis une liste (extensible à la lecture de fichiers).
- **Statistiques du système** : tableau de bord affichant les performances de l'arbre et de la table de hachage.
- **Dérivés validés** : association automatique des mots validés à leur racine dans l'arbre, avec compteur de fréquence.
- **Redimensionnement dynamique** de la table de hachage pour maintenir les performances à l'ajout de nouveaux schèmes.

---

*Rapport généré pour le mini-projet Algorithmique — Année 2025-2026*
