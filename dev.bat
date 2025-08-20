@echo off
setlocal

echo Starting WPI Planner Development Server...

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

rem Create classes directory if it doesn't exist
if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"

rem Compile Java source to classes first
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
xcopy /E /I /Y "%SRC_DIR%\edu\wpi\scheduler" "%CLASSES_DIR%\edu\wpi\scheduler" >nul

rem Start GWT Development Mode (SuperDev Mode)
echo.
echo Starting GWT SuperDev Mode...
echo Access your application at: http://localhost:8888/Scheduler.html
echo Development console will be at: http://localhost:9876/
echo.
java -cp "%GWT_HOME%\gwt-codeserver.jar;%GWT_HOME%\gwt-dev.jar;%GWT_HOME%\gwt-user.jar;%SRC_DIR%;%CLASSES_DIR%" ^
     com.google.gwt.dev.codeserver.CodeServer ^
     -src "%SRC_DIR%" ^
     -launcherDir "%WAR_DIR%" ^
     -port 9876 ^
     edu.wpi.scheduler.Scheduler