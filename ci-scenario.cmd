@echo off
set "SCN=%~1"
set "JH21=%JAVA_HOME%"
rem strip PATH so that javac is NOT found there
set "PATH=%SystemRoot%\System32;%SystemRoot%"
where javac >nul 2>nul && (echo FAIL: javac still in PATH & exit /b 1)
goto :%SCN%

:java_home
set "JAVA_HOME=%JH21%"
goto :run

:jdks_folder
mkdir "%USERPROFILE%\.jdks"
mklink /J "%USERPROFILE%\.jdks\temurin-test" "%JH21%"
set "JAVA_HOME="
goto :run

:reject_old_jdk
if not defined JAVA_HOME_17_X64 (echo no JDK 17 on runner - skip & exit /b 0)
set "JAVA_HOME=%JAVA_HOME_17_X64%"
goto :run

:run
call build-exe.bat nopause > out.txt 2>&1
set RC=%errorlevel%
type out.txt
echo build exit code: %RC%
echo ::notice title=%SCN%-exitcode::build exit code %RC%
for /f "delims=" %%L in ('findstr /r /c:"Using JDK" /c:"ERROR:" out.txt') do echo ::notice title=%SCN%-log::%%L
if exist dist\NumberGuess\NumberGuess.exe echo ::notice title=%SCN%-exe::NumberGuess.exe exists
if "%SCN%"=="reject_old_jdk" goto :check_reject
if not "%RC%"=="0" (echo FAIL: build failed & exit /b 1)
if not exist dist\NumberGuess\NumberGuess.exe (echo FAIL: no exe & exit /b 1)
if "%SCN%"=="jdks_folder" (findstr /c:"Using JDK" out.txt | findstr /c:".jdks" >nul || (echo FAIL: JDK was not taken from .jdks & exit /b 1))
echo PASS %SCN%
exit /b 0

:check_reject
findstr /r /c:"Using JDK.*\\17\." out.txt >nul && (echo FAIL: JDK 17 was accepted & exit /b 1)
echo PASS reject_old_jdk
exit /b 0
