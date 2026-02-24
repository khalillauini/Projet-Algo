#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
#  Moteur Morphologique Arabe — Script de compilation et d'exécution
# ─────────────────────────────────────────────────────────────────────────────

set -e

SRC="src/main/java/morphology"
OUT="out"

echo "══════════════════════════════════════════════════════"
echo "  Moteur de Recherche Morphologique Arabe"
echo "  Compilation et Exécution"
echo "══════════════════════════════════════════════════════"

# 1. Vérifier Java
if ! command -v javac &> /dev/null; then
    echo "ERREUR : javac introuvable. Installez JDK 11 ou supérieur."
    echo "  Ubuntu/Debian : sudo apt install default-jdk"
    echo "  macOS         : brew install openjdk"
    exit 1
fi

echo "  JDK : $(javac -version 2>&1)"

# 2. Compiler
echo ""
echo "  Compilation..."
mkdir -p "$OUT"
javac -encoding UTF-8 -d "$OUT" "$SRC"/*.java
echo "  ✓ Compilation réussie."

# 3. Exécuter
echo ""
echo "  Démarrage de l'application..."
echo "══════════════════════════════════════════════════════"
echo ""
java -Dfile.encoding=UTF-8 \
     -Dstdout.encoding=UTF-8 \
     -Dstdin.encoding=UTF-8 \
     -cp "$OUT" morphology.Main
