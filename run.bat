@echo off
chcp 65001 > nul
echo ======================================================
echo   Moteur de Recherche Morphologique Arabe
echo   Compilation et Execution
echo ======================================================

if not exist "out" mkdir out

echo   Compilation...
javac -encoding UTF-8 -d out src\main\java\morphology\*.java
if errorlevel 1 (
    echo ERREUR: Compilation echouee. Verifiez votre JDK.
    pause
    exit /b 1
)
echo   Compilation reussie.
echo.
echo ======================================================
java -Dfile.encoding=UTF-8 -cp out morphology.Main
pause
