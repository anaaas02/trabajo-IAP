@echo off
setlocal enabledelayedexpansion
set ROOT=%~dp0..
set SRC=%ROOT%\src\main\java
set OUT=%ROOT%\build\classes
set LIB=%ROOT%\lib

if not exist "%OUT%" mkdir "%OUT%"

set CP=%OUT%
for %%f in ("%LIB%\*.jar") do (
  set CP=!CP!;%%~f
)

for /f "delims=" %%f in ('dir /s /b "%SRC%\*.java"') do (
  set SOURCES=!SOURCES! "%%f"
)

if "%SOURCES%"=="" (
  echo No Java sources found.
  exit /b 1
)

javac -encoding UTF-8 -d "%OUT%" -cp "%CP%" %SOURCES%
if errorlevel 1 (
  echo Compilation failed.
  exit /b 1
)

echo Compilation completed.
