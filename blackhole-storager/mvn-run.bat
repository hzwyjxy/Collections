@echo off
echo Starting Maven with custom configuration...

REM Set Maven path
set MAVEN_HOME=D:\Trae area\apache-maven-3.9.9
set PATH=%MAVEN_HOME%\bin;%PATH%

REM Set local repository path and SSL options
set MAVEN_OPTS=-Dmaven.repo.local="D:\Trae area\.m2\repository" -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true

echo Maven Home: %MAVEN_HOME%
echo Local Repository: D:\Trae area\.m2\repository
echo.

REM Execute Maven command
if "%1"=="" (
    echo Usage: mvn-run.bat [maven-commands]
    echo Example: mvn-run.bat clean install
    echo Example: mvn-run.bat compile
    echo Example: mvn-run.bat test
) else (
    echo Executing: mvn %*
    call mvn %*
)

echo.
echo Maven execution completed.
pause