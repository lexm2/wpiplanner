@echo off
setlocal

echo Building WPI Planner GWT Application...

rem Download fresh schedule data
echo Downloading fresh schedule data from WPI servers...
curl -L -o war/new.schedb https://planner.wpi.edu/new.schedb
if %ERRORLEVEL% EQU 0 (
    echo ✓ Successfully downloaded fresh schedule data!
) else (
    echo ✗ Warning: Failed to download schedule data, using existing file
)
echo.

set GWT_HOME=%~dp0gwt-2.11.0
set PROJECT_ROOT=%~dp0
set WAR_DIR=%PROJECT_ROOT%war
set SRC_DIR=%PROJECT_ROOT%src
set CLASSES_DIR=%WAR_DIR%\WEB-INF\classes

rem Check if GWT SDK exists
if not exist "%GWT_HOME%\gwt-dev.jar" (
    echo ERROR: GWT SDK not found at %GWT_HOME%
    echo Please ensure GWT 2.11.0 is installed in the gwt-2.11.0 directory
    exit /b 1
)

echo Using GWT SDK at: %GWT_HOME%

rem Clean previous build artifacts
echo Cleaning previous build...
if exist "%WAR_DIR%\scheduler" rmdir /s /q "%WAR_DIR%\scheduler"
if exist "%CLASSES_DIR%" rmdir /s /q "%CLASSES_DIR%"

rem Create classes directory if it doesn't exist
if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"

rem Compile Java source to classes
echo.
echo Compiling Java source files...

rem Create a temporary file list of all Java files
dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\java_files.txt"

rem Compile all Java files at once
javac -cp "%GWT_HOME%\gwt-user.jar;%GWT_HOME%\gwt-dev.jar;%SRC_DIR%" ^
      -d "%CLASSES_DIR%" ^
      -sourcepath "%SRC_DIR%" ^
      @"%TEMP%\java_files.txt"

rem Clean up temporary file
del "%TEMP%\java_files.txt"

if %ERRORLEVEL% neq 0 (
    echo ERROR: Java compilation failed
    exit /b 1
)

rem Copy non-Java files to classes directory
echo.
echo Copying resources...
rem Use xcopy to copy all files preserving directory structure, then remove Java files
xcopy /E /I /Y "%SRC_DIR%\*" "%CLASSES_DIR%" >nul
rem Remove Java files from classes directory since they're already compiled
del /s /q "%CLASSES_DIR%\*.java" 2>nul

rem Run GWT Compiler
echo.
echo Running GWT Compiler...
java -cp "%GWT_HOME%\gwt-dev.jar;%GWT_HOME%\gwt-user.jar;%SRC_DIR%;%CLASSES_DIR%" ^
     com.google.gwt.dev.Compiler ^
     -war "%WAR_DIR%" ^
     -logLevel INFO ^
     edu.wpi.scheduler.Scheduler

if %ERRORLEVEL% neq 0 (
    echo ERROR: GWT compilation failed
    exit /b 1
)

rem Verify compilation results
echo.
echo Verifying build results...
if exist "%WAR_DIR%\scheduler\scheduler.nocache.js" (
    echo ✓ GWT nocache.js file generated successfully
) else (
    echo ✗ WARNING: GWT nocache.js file not found
)

if exist "%WAR_DIR%\scheduler\*.cache.js" (
    echo ✓ GWT cache.js files generated successfully
) else (
    echo ✗ WARNING: GWT cache.js files not found
)

echo.
echo Build completed successfully!
echo Generated files:
dir "%WAR_DIR%\scheduler\*.js" /b 2>nul
echo.
echo You can now serve the application from the war directory.
echo For development, use: java -cp gwt-dev.jar com.google.gwt.dev.codeserver.CodeServer edu.wpi.scheduler.Scheduler