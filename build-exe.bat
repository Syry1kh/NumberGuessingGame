@echo off
setlocal EnableDelayedExpansion

rem ====================================================================
rem  Builds a standalone Windows app: dist\NumberGuess\NumberGuess.exe
rem  The Java runtime is bundled, so the result runs on PCs without Java.
rem
rem  Needs: JDK 21 or newer (javac, jar, jpackage). It is searched in PATH,
rem         JAVA_HOME and the usual install folders (including the
rem         %USERPROFILE%\.jdks folder used by IntelliJ IDEA).
rem         Maven is NOT required.
rem
rem  Usage: build-exe.bat            builds and opens the result folder
rem         build-exe.bat nopause    for CI: no pause, no explorer
rem ====================================================================

cd /d "%~dp0"

call :ensure_jdk
if errorlevel 1 (
    echo.
    echo ERROR: JDK 21 or newer was not found. javac and jpackage are required.
    echo   1. Install a JDK, for example Eclipse Temurin 21 from https://adoptium.net
    echo      and tick "Add to PATH" and "Set JAVA_HOME" in the installer.
    echo   2. Or skip building: the ready NumberGuess.exe is built by GitHub.
    echo      Repository - Actions - Build Windows exe - latest run - Artifacts.
    goto :fail
)
echo Using JDK: !JDK_HOME!
echo.

if exist build rmdir /s /q build
if exist dist rmdir /s /q dist
mkdir build\classes
mkdir build\input

set "FILES="
for /r "src\main\java" %%f in (*.java) do set FILES=!FILES! "%%f"

echo [1/3] Compiling...
javac -encoding UTF-8 --release 21 -d build\classes !FILES!
if errorlevel 1 goto :fail

echo [2/3] Packing jar...
jar --create --file build\input\NumberGuessing.jar --main-class org.example.NumberGuessGUI -C build\classes .
if errorlevel 1 goto :fail

echo [3/3] Building NumberGuess.exe with bundled Java runtime...
jpackage --type app-image ^
  --name NumberGuess ^
  --input build\input ^
  --main-jar NumberGuessing.jar ^
  --main-class org.example.NumberGuessGUI ^
  --app-version 1.0 ^
  --vendor "Syry1kh" ^
  --description "Number Guessing Game" ^
  --add-modules java.desktop ^
  --jlink-options "--strip-debug --no-header-files --no-man-pages --compress=zip-6" ^
  --dest dist
if errorlevel 1 goto :fail

if not exist dist\NumberGuess\NumberGuess.exe (
    echo ERROR: dist\NumberGuess\NumberGuess.exe was not created.
    goto :fail
)

echo.
echo Done: dist\NumberGuess\NumberGuess.exe
echo Keep the whole NumberGuess folder together - the exe needs the files next to it.
if /i not "%~1"=="nopause" (
    explorer dist\NumberGuess
    pause
)
exit /b 0

:fail
echo.
echo Build failed.
if /i not "%~1"=="nopause" pause
exit /b 1


rem --------------------------------------------------------------------
rem  :ensure_jdk  - finds a JDK 21+, puts its bin folder first in PATH and
rem                 sets JDK_HOME. Returns 1 when there is none.
rem --------------------------------------------------------------------
:ensure_jdk
set "JDK_HOME="

rem 1) javac that is already in PATH
for /f "delims=" %%P in ('where javac 2^>nul') do (
    if not defined JDK_HOME call :try_jdk "%%~dpP.."
)

rem 2) JAVA_HOME
if not defined JDK_HOME if defined JAVA_HOME call :try_jdk "!JAVA_HOME!"

rem 3) usual install folders; IntelliJ IDEA keeps its JDKs in %USERPROFILE%\.jdks
for %%R in ("%USERPROFILE%\.jdks" "%ProgramFiles%\Java" "%ProgramFiles%\Eclipse Adoptium" "%ProgramFiles%\Microsoft" "%ProgramFiles%\Zulu" "%ProgramFiles%\BellSoft" "%ProgramFiles%\Amazon Corretto" "%ProgramFiles%\Semeru" "%LOCALAPPDATA%\Programs\Eclipse Adoptium") do (
    if not defined JDK_HOME if exist "%%~R\" (
        for /d %%J in ("%%~R\*") do (
            if not defined JDK_HOME call :try_jdk "%%~J"
        )
    )
)

if not defined JDK_HOME exit /b 1
set "PATH=!JDK_HOME!\bin;!PATH!"
exit /b 0

rem --------------------------------------------------------------------
rem  :try_jdk "folder"  - sets JDK_HOME when the folder is a JDK 21+
rem                       that contains javac and jpackage.
rem --------------------------------------------------------------------
:try_jdk
if not exist "%~1\bin\javac.exe" exit /b 1
if not exist "%~1\bin\jpackage.exe" exit /b 1
set "JVER="
for /f "tokens=1,2" %%A in ('"%~1\bin\javac.exe" -version 2^>^&1') do (
    if /i "%%A"=="javac" if not defined JVER set "JVER=%%B"
)
set "JMAJOR="
for /f "delims=.-+ " %%M in ("!JVER!") do set "JMAJOR=%%M"
if not defined JMAJOR exit /b 1
if !JMAJOR! LSS 21 exit /b 1
set "JDK_HOME=%~f1"
exit /b 0
