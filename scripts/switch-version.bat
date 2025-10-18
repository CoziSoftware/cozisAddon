@echo off
setlocal enabledelayedexpansion

REM Minecraft Version Switcher Script for Windows
REM Usage: scripts\switch-version.bat [1.21.4|1.21.5]

set "TARGET_VERSION="
set "CURRENT_VERSION="

REM Function to show usage
:show_usage
echo Usage: %0 [1.21.4^|1.21.5]
echo.
echo This script switches the Minecraft version in gradle.properties
echo.
echo Examples:
echo   %0 1.21.4    # Switch to Minecraft 1.21.4
echo   %0 1.21.5    # Switch to Minecraft 1.21.5
echo.
echo If no version is specified, it will toggle between 1.21.4 and 1.21.5
goto :eof

REM Function to get current version
:get_current_version
if not exist "gradle.properties" (
    echo [ERROR] gradle.properties not found!
    exit /b 1
)

for /f "tokens=2 delims==" %%i in ('findstr "minecraft_version=" gradle.properties') do set "CURRENT_VERSION=%%i"
exit /b 0

REM Function to validate version
:validate_version
if "%1"=="1.21.4" exit /b 0
if "%1"=="1.21.5" exit /b 0
exit /b 1

REM Function to toggle version
:toggle_version
call :get_current_version
if "%CURRENT_VERSION%"=="1.21.4" (
    set "TARGET_VERSION=1.21.5"
) else if "%CURRENT_VERSION%"=="1.21.5" (
    set "TARGET_VERSION=1.21.4"
) else (
    echo [WARNING] Unknown current version: %CURRENT_VERSION%, defaulting to 1.21.5
    set "TARGET_VERSION=1.21.5"
)
exit /b 0

REM Function to update version
:update_version
set "TARGET_VERSION=%1"
call :get_current_version

echo [INFO] Current version: %CURRENT_VERSION%
echo [INFO] Target version: %TARGET_VERSION%

if "%CURRENT_VERSION%"=="%TARGET_VERSION%" (
    echo [WARNING] Already on version %TARGET_VERSION%
    exit /b 0
)

echo [INFO] Updating gradle.properties...

REM Create backup
copy "gradle.properties" "gradle.properties.backup" >nul

REM Update minecraft_version
powershell -Command "(Get-Content 'gradle.properties') -replace 'minecraft_version=.*', 'minecraft_version=%TARGET_VERSION%' | Set-Content 'gradle.properties'"

REM Update yarn_mappings
powershell -Command "(Get-Content 'gradle.properties') -replace 'yarn_mappings=.*', 'yarn_mappings=%TARGET_VERSION%+build.1' | Set-Content 'gradle.properties'"

REM Update fabric_version
powershell -Command "(Get-Content 'gradle.properties') -replace 'fabric_version=.*', 'fabric_version=0.128.2+%TARGET_VERSION%' | Set-Content 'gradle.properties'"

REM Update meteor_version
powershell -Command "(Get-Content 'gradle.properties') -replace 'meteor_version=.*', 'meteor_version=%TARGET_VERSION%-SNAPSHOT' | Set-Content 'gradle.properties'"

REM Update baritone_version
powershell -Command "(Get-Content 'gradle.properties') -replace 'baritone_version=.*', 'baritone_version=%TARGET_VERSION%-SNAPSHOT' | Set-Content 'gradle.properties'"

REM Update XaeroPlus version
powershell -Command "(Get-Content 'gradle.properties') -replace 'xaeroplus_version=.*', 'xaeroplus_version=2.28.1+fabric-%TARGET_VERSION%' | Set-Content 'gradle.properties'"

REM Update XaeroWorldmap version
powershell -Command "(Get-Content 'gradle.properties') -replace 'xaeros_worldmap_version=.*', 'xaeros_worldmap_version=1.39.12_Fabric_%TARGET_VERSION%' | Set-Content 'gradle.properties'"

REM Update XaeroMinimap version
powershell -Command "(Get-Content 'gradle.properties') -replace 'xaeros_minimap_version=.*', 'xaeros_minimap_version=25.2.10_Fabric_%TARGET_VERSION%' | Set-Content 'gradle.properties'"

echo [SUCCESS] Updated to Minecraft %TARGET_VERSION%
exit /b 0

REM Main script logic
:main
echo [INFO] Minecraft Version Switcher
echo [INFO] =========================

REM Check if gradle.properties exists
if not exist "gradle.properties" (
    echo [ERROR] gradle.properties not found in current directory!
    echo [ERROR] Please run this script from the project root directory.
    exit /b 1
)

REM Determine target version
if "%1"=="" (
    REM No arguments provided, toggle version
    call :toggle_version
    echo [INFO] No version specified, toggling to: !TARGET_VERSION!
) else (
    REM Version provided as argument
    set "TARGET_VERSION=%1"
    call :validate_version "%TARGET_VERSION%"
    if errorlevel 1 (
        echo [ERROR] Invalid version: %TARGET_VERSION%
        call :show_usage
        exit /b 1
    )
)

REM Update version
call :update_version "%TARGET_VERSION%"

REM Show updated configuration
echo [INFO] Updated configuration:
for /f "tokens=2 delims==" %%i in ('findstr "minecraft_version=" gradle.properties') do echo   Minecraft: %%i
for /f "tokens=2 delims==" %%i in ('findstr "fabric_version=" gradle.properties') do echo   Fabric: %%i
for /f "tokens=2 delims==" %%i in ('findstr "meteor_version=" gradle.properties') do echo   Meteor: %%i
for /f "tokens=2 delims==" %%i in ('findstr "baritone_version=" gradle.properties') do echo   Baritone: %%i

echo [SUCCESS] Version switch completed!
echo [INFO] You may want to run 'gradlew clean build' to test the new configuration.

REM Clean up backup if successful
if exist "gradle.properties.backup" del "gradle.properties.backup"

goto :eof

REM Run main function
call :main %*
