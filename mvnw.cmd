@echo off
setlocal

set "MVNW_DIR=%~dp0"
set "MAVEN_HOME=%MVNW_DIR%.tools\apache-maven-3.9.9"
set "MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd"

if not exist "%MAVEN_BIN%" (
    echo Maven was not found at "%MAVEN_BIN%".
    echo Run the local Maven setup again or restore the .tools directory.
    exit /b 1
)

if "%JAVA_HOME%"=="" (
    for /f "usebackq delims=" %%I in (`powershell -NoProfile -Command "$jdkKey = Get-ChildItem 'HKLM:\SOFTWARE\JavaSoft\JDK' | Sort-Object PSChildName -Descending | Select-Object -First 1 -ExpandProperty Name; if ($jdkKey) { (Get-ItemProperty ('Registry::' + $jdkKey)).JavaHome }"`) do set "JAVA_HOME=%%I"
)

if "%JAVA_HOME%"=="" (
    echo JAVA_HOME is not set and no installed JDK was found in the registry.
    exit /b 1
)

call "%MAVEN_BIN%" %*
exit /b %ERRORLEVEL%
