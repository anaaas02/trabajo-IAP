@echo off
setlocal enabledelayedexpansion
set ROOT=%~dp0..
set MAIN_SRC=%ROOT%\src\main\java
set TEST_SRC=%ROOT%\src\test\java
set MAIN_OUT=%ROOT%\build\classes
set TEST_OUT=%ROOT%\build\test-classes
set LIB=%ROOT%\lib

if not exist "%MAIN_OUT%" mkdir "%MAIN_OUT%"
if not exist "%TEST_OUT%" mkdir "%TEST_OUT%"

set CP=%MAIN_OUT%;%TEST_OUT%
for %%f in ("%LIB%\*.jar") do (
  set CP=!CP!;%%~f
)

for /f "delims=" %%f in ('dir /s /b "%MAIN_SRC%\*.java"') do (
  set MAIN_SOURCES=!MAIN_SOURCES! "%%f"
)
for /f "delims=" %%f in ('dir /s /b "%TEST_SRC%\*.java"') do (
  set TEST_SOURCES=!TEST_SOURCES! "%%f"
)

if "%MAIN_SOURCES%"=="" (
  echo No main Java sources found.
  exit /b 1
)
if "%TEST_SOURCES%"=="" (
  echo No test Java sources found.
  exit /b 1
)

javac -encoding UTF-8 -d "%MAIN_OUT%" -cp "%CP%" %MAIN_SOURCES%
if errorlevel 1 (
  echo Main compilation failed.
  exit /b 1
)

javac -encoding UTF-8 -d "%TEST_OUT%" -cp "%CP%" %TEST_SOURCES%
if errorlevel 1 (
  echo Test compilation failed.
  exit /b 1
)

set JUNIT_JAR=
for %%f in ("%LIB%\junit-platform-console-standalone-*.jar") do (
  set JUNIT_JAR=%%~f
)

if "%JUNIT_JAR%"=="" (
  echo JUnit console jar not found in %LIB%.
  exit /b 1
)

java -jar "%JUNIT_JAR%" --class-path "%CP%" --scan-classpath
