@echo off
title Intelligent Quiz App Launcher
color 0A

echo ==========================================
echo      Intelligent Quiz App Launcher
echo ==========================================
echo.

echo [1/2] Compiling source code...
javac IntelligentQuizApp.java

if %errorlevel% neq 0 (
    color 0C
    echo.
    echo [ERROR] Compilation failed!
    echo Please check the error messages above.
    echo.
    pause
    exit /b
)

echo [2/2] Starting application...
echo.
java IntelligentQuizApp

echo.
echo Application closed.
pause