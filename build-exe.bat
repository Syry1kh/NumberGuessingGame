@echo off
setlocal EnableDelayedExpansion

rem ====================================================================
rem  Builds a standalone Windows app: dist\NumberGuess\NumberGuess.exe
rem  The Java runtime is bundled, so the result runs on PCs without Java.
rem
rem  Needs: JDK 21 or newer (javac, jar, jpackage) in PATH.
rem         Maven is NOT required.
rem
rem  Usage: build-exe.bat            builds and opens the result folder
rem         build-exe.bat nopause    for CI: no pause, no explorer
rem ====================================================================

cd /d "%~dp0"

for %%T in (javac jar jpackage) do (
    where %%T >nul 2>nul
    if errorlevel 1 (
        echo [!] %%T not found. Install JDK 21 or newer and make sure it is in PATH.
        goto :fail
    )
)

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
    echo [!] dist\NumberGuess\NumberGuess.exe was not created.
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
