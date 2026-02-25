# محرك البحث الصرفي العربي
# Moteur de Recherche Morphologique Arabe

Mini-projet Algorithmique — 1ING — 2025-2026

## Structure du Projet

```
arabic-morphology/
├── src/main/java/morphology/
│   ├── Main.java              ← Point d'entrée
│   ├── CLI.java               ← Interface interactive en ligne de commande
│   ├── MorphologyEngine.java  ← Cœur du moteur (dérivation + validation)
│   ├── AVLTree.java           ← Arbre AVL pour les racines
│   ├── AVLNode.java           ← Nœud de l'arbre (racine + dérivés + fréquence)
│   ├── SchemeHashTable.java   ← Table de hachage pour les schèmes
│   ├── Scheme.java            ← Représentation d'un schème morphologique
│   └── ValidationResult.java  ← Résultat d'une validation
├── RAPPORT_TECHNIQUE.md       ← Rapport technique (2-3 pages)
├── run.sh                     ← Script Linux/macOS
└── run.bat                    ← Script Windows
```

## Compilation et Exécution

### Prérequis
- JDK 11 ou supérieur
- Terminal avec support UTF-8 (pour l'affichage des caractères arabes)

### Linux / macOS
```bash
chmod +x run.sh
./run.sh
```

### Windows
```cmd
run.bat
```

### Manuel
```bash
# Compilation
javac -encoding UTF-8 -d out src/main/java/morphology/*.java

# Exécution
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp out morphology.Main
```

> **Important :** Sur Windows, ouvrez cmd.exe ou PowerShell avec la commande `chcp 65001` avant d'exécuter pour activer l'encodage UTF-8.

## Fonctionnalités

| # | Fonctionnalité | Structure utilisée |
|---|----------------|-------------------|
| 1 | Insertion/recherche/suppression de racines | Arbre AVL |
| 2 | Gestion des schèmes (CRUD) | Table de hachage |
| 3 | Génération de mots dérivés | Moteur de templates |
| 4 | Validation morphologique | Inversion de templates |
| 5 | Analyse inverse d'un mot | Parcours exhaustif des schèmes |
| 6 | Statistiques du système | Métriques AVL + hachage |

## Complexité Algorithmique

- **Arbre AVL :** O(log n) pour insertion, recherche, suppression
- **Table de hachage :** O(1) amorti pour accès, insertion, suppression
- **Génération :** O(s) où s = nombre de schèmes
- **Validation :** O(s × |template|) ≈ O(s)
