@echo off
echo Starting Maven with custom configuration...

REM Set Maven home
set "MAVEN_HOME=D:\Trae area\apache-maven-3.9.9"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

echo Maven Home: %MAVEN_HOME%
echo.

REM Execute Maven command
if "%1"=="" (
    echo Usage: mvn-simple.bat [maven-commands]
    echo Example: mvn-simple.bat clean install
    echo Example: mvn-simple.bat compile
    echo Example: mvn-simple.bat test
) else (
    echo Executing: mvn %*
    call mvn %*
)

echo.
echo Maven execution completed.
pause