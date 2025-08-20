@echo off
setlocal

echo Starting HTTP server for WPI Planner...

set WAR_DIR=%~dp0war

rem Check if war directory exists
if not exist "%WAR_DIR%" (
    echo ERROR: war directory not found. Please run build.bat first.
    exit /b 1
)

rem Check if Python is available for simple HTTP server
python --version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo Starting Python HTTP server on port 8000...
    echo Access the application at: http://localhost:8000/
    cd "%WAR_DIR%"
    python -m http.server 8000
) else (
    echo Python not found. Checking for Node.js...
    node --version >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        echo Installing and starting http-server...
        npm install -g http-server
        cd "%WAR_DIR%"
        http-server -p 8000
    ) else (
        echo ERROR: Neither Python nor Node.js found.
        echo Please install Python or Node.js to run a local HTTP server.
        echo Alternatively, copy the contents of the war directory to your web server.
        exit /b 1
    )
)