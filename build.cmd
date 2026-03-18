@echo off
setlocal

set "PROJECT_DIR=%~dp0"
pushd "%PROJECT_DIR%" >nul

call :resolve_java
if errorlevel 1 goto :fail

if /I "%USE_MAVEN%"=="1" if exist "mvnw.cmd" (
    echo Building with Maven wrapper...
    call .\mvnw.cmd -q -DskipTests compile
    if errorlevel 1 goto :fail
    echo Build completed successfully.
    popd >nul
    exit /b 0
)

if not exist "lib\flatlaf*.jar" (
    echo Missing FlatLaf dependency in lib\ ^(expected flatlaf-*.jar^).
    echo Add flatlaf and flatlaf-intellij-themes jars to lib\ for non-Maven builds.
    echo Or run with Maven once: set USE_MAVEN=1 ^&^& build.cmd
    goto :fail
)

if not exist "lib\flatlaf-intellij-themes*.jar" (
    echo Missing FlatLaf IntelliJ themes dependency in lib\ ^(expected flatlaf-intellij-themes-*.jar^).
    echo Add flatlaf-intellij-themes jar to lib\ for non-Maven builds.
    echo Or run with Maven once: set USE_MAVEN=1 ^&^& build.cmd
    goto :fail
)

if not exist "target\classes" mkdir "target\classes"

set "SOURCE_LIST=%TEMP%\rmi-sources-%RANDOM%-%RANDOM%.txt"
if exist "%SOURCE_LIST%" del "%SOURCE_LIST%"
setlocal enabledelayedexpansion
for /R "src\main\java" %%I in (*.java) do (
    set "FILE=%%I"
    set "FILE=!FILE:\=/!"
    echo "!FILE!">>"%SOURCE_LIST%"
)
endlocal

if not exist "%SOURCE_LIST%" (
    echo No Java source files were found.
    goto :cleanup_fail
)

for %%A in ("%SOURCE_LIST%") do if %%~zA==0 (
    echo No Java source files were found.
    goto :cleanup_fail
)

echo Compiling sources...
javac -cp "lib/*" -d "target\classes" @"%SOURCE_LIST%"
if errorlevel 1 goto :cleanup_fail

echo Build completed successfully.
del "%SOURCE_LIST%" >nul 2>&1
popd >nul
exit /b 0

:cleanup_fail
del "%SOURCE_LIST%" >nul 2>&1
:fail
popd >nul
exit /b 1

:resolve_java
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

where javac >nul 2>&1
if %ERRORLEVEL%==0 exit /b 0

for /f "usebackq delims=" %%I in (`powershell -NoProfile -Command "$jdkKey = Get-ChildItem 'HKLM:\SOFTWARE\JavaSoft\JDK' | Sort-Object PSChildName -Descending | Select-Object -First 1 -ExpandProperty Name; if ($jdkKey) { (Get-ItemProperty ('Registry::' + $jdkKey)).JavaHome }"`) do set "JAVA_HOME=%%I"

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

where javac >nul 2>&1
if %ERRORLEVEL%==0 exit /b 0

echo JDK not found. Install Java and make sure javac is available.
exit /b 1
