@echo off
REM ========================================
REM Script de création d'installateur pour SenBibliotheque
REM ========================================
echo ====================================
echo  SenBibliotheque - Creation d'installateur
echo ====================================
echo.
REM Configuration du projet
set PROJECT_NAME=SenBibliotheque
set JAR_NAME=SenBibliotheque.jar
set MAIN_CLASS=com.example.SenBibliotheque.SenBibliothequeApp
set APP_VERSION=1.0
set VENDOR=Mansour-Seck
REM Chemins
set PROJECT_DIR=D:\LPRI\Java\SenBibliotheque
set JAR_PATH=%PROJECT_DIR%\out\artifacts\SenBibliotheque_jar\%JAR_NAME%
set BUILD_DIR=%PROJECT_DIR%\installer-build
set OUTPUT_DIR=%PROJECT_DIR%\installer
echo [DEBUG] Configuration:
echo - Projet: %PROJECT_NAME%
echo - JAR: %JAR_NAME%
echo - Chemin JAR: %JAR_PATH%
echo - Classe principale: %MAIN_CLASS%
echo - JAVA_HOME: %JAVA_HOME%
echo.
REM Vérifier Java et jpackage
java -version
if %errorlevel% neq 0 (
    echo [ERREUR] Java n'est pas installé.
    pause
    exit /b 1
)
jpackage --version
if %errorlevel% neq 0 (
    echo [ERREUR] jpackage n'est pas disponible.
    pause
    exit /b 1
)
REM Vérifier le JAR
if not exist "%JAR_PATH%" (
    echo [ERREUR] Le JAR n'existe pas: %JAR_PATH%
    pause
    exit /b 1
)
echo [OK] JAR trouvé.
REM Créer les dossiers
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"
mkdir "%BUILD_DIR%"
mkdir "%OUTPUT_DIR%"
REM Copier le JAR
copy "%JAR_PATH%" "%BUILD_DIR%\%JAR_NAME%"
echo [OK] JAR copié.
REM Créer le JRE embarqué AVEC JAVAFX
echo [4/6] Création du JRE embarqué avec JavaFX...
jlink ^
    --module-path "%JAVA_HOME%\jmods" ^
    --add-modules java.base,java.desktop,java.sql,java.naming,java.management,java.instrument,java.xml,java.logging,java.prefs,javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.swing,javafx.web,jdk.unsupported,jdk.crypto.ec ^
    --output "%BUILD_DIR%\java-runtime" ^
    --strip-debug ^
    --no-header-files ^
    --no-man-pages ^
    --compress=2
if %errorlevel% neq 0 (
    echo [ERREUR] Échec de création du JRE.
    pause
    exit /b 1
)
echo [OK] JRE avec JavaFX créé.
echo.
REM Créer l'installateur
echo [6/6] Création de l'installateur...
jpackage ^
    --input "%BUILD_DIR%" ^
    --name SenBibliotheque ^
    --main-jar %JAR_NAME% ^
    --main-class %MAIN_CLASS% ^
    --type exe ^
    --dest "%OUTPUT_DIR%" ^
    --app-version %APP_VERSION% ^
    --vendor "%VENDOR%" ^
    --description "Système de gestion de bibliothèque" ^
    --runtime-image "%BUILD_DIR%\java-runtime" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --win-menu-group "SenBibliotheque" ^
    --java-options "-Xms256m" ^
    --java-options "-Xmx1024m" ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --icon "%PROJECT_DIR%\resources\senbibliotheque.ico"
if %errorlevel% neq 0 (
    echo [ERREUR] Échec de création de l'installateur.
    pause
    exit /b 1
)
echo.
echo ========================================
echo  SUCCES! Installateur créé!
echo ========================================
echo.
echo CARACTERISTIQUES:
echo [✓] JRE Java 25 + JavaFX embarqué
echo [✓] Raccourci bureau
echo [✓] Raccourci menu démarrer
echo [✓] Choix du répertoire
echo.
dir "%OUTPUT_DIR%\*.exe"
echo.
pause
explorer "%OUTPUT_DIR%"