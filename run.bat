@echo off
title RecallRhythm
cd /d "%~dp0"
echo Compiling RecallRhythm...
javac *.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Starting RecallRhythm...
java Main
