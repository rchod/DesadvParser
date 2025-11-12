@echo off
REM Setup check script for DESADV Parser (Windows)
REM This script checks if all required dependencies are in place

echo Checking DESADV Parser setup...
echo.

set ERRORS=0

REM Check for EDIReader library
if not exist "lib\edireader-4.7.3.jar" (
    echo [ERROR] EDIReader library not found!
    echo    Expected location: lib\edireader-4.7.3.jar
    echo.
    echo    To fix this:
    echo    1. Download edireader-4.7.3.jar from https://www.berryworks.com/
    echo    2. Create the lib directory: mkdir lib
    echo    3. Place the JAR file in the lib\ directory
    echo    4. Run this script again to verify
    echo.
    set /a ERRORS+=1
) else (
    echo [OK] EDIReader library found: lib\edireader-4.7.3.jar
)

REM Check for Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH
    set /a ERRORS+=1
) else (
    echo [OK] Java found
    java -version 2>&1 | findstr /C:"version"
)

REM Check for Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven is not installed or not in PATH
    set /a ERRORS+=1
) else (
    echo [OK] Maven found
    mvn -version | findstr /C:"Apache Maven"
)

REM Check for required config files
if not exist "edi\src\_base.xml" (
    echo [WARNING] Config file not found: edi\src\_base.xml
) else (
    echo [OK] Config file found: edi\src\_base.xml
)

if not exist "edi\src\_segments-ordre.txt" (
    echo [WARNING] Config file not found: edi\src\_segments-ordre.txt
) else (
    echo [OK] Config file found: edi\src\_segments-ordre.txt
)

echo.
if %ERRORS%==0 (
    echo [OK] All required dependencies are in place!
    echo    You can now run: mvn clean compile
    exit /b 0
) else (
    echo [ERROR] Setup incomplete. Please fix the errors above before building.
    exit /b 1
)

