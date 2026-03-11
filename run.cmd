@echo off
setlocal

set "PROJECT_DIR=%~dp0"
pushd "%PROJECT_DIR%" >nul

if "%~1"=="" goto :usage

set "TARGET=%~1"
set "MAIN_CLASS="

if /I "%TARGET%"=="server" set "MAIN_CLASS=HRMServer"
if /I "%TARGET%"=="gui" set "MAIN_CLASS=HRMGUIClient"
if /I "%TARGET%"=="client" set "MAIN_CLASS=HRMClient"

if not defined MAIN_CLASS goto :usage

call "%PROJECT_DIR%build.cmd"
if errorlevel 1 goto :fail

call :resolve_java
if errorlevel 1 goto :fail

echo Running %MAIN_CLASS%...
java -cp "target\classes;lib/*" %MAIN_CLASS%
set "EXIT_CODE=%ERRORLEVEL%"
popd >nul
exit /b %EXIT_CODE%

:usage
echo Usage: run.cmd [server^|gui^|client]
popd >nul
exit /b 1

:fail
popd >nul
exit /b 1

:resolve_java
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

where java >nul 2>&1
if %ERRORLEVEL%==0 exit /b 0

for /f "usebackq delims=" %%I in (`powershell -NoProfile -Command "$jdkKey = Get-ChildItem 'HKLM:\SOFTWARE\JavaSoft\JDK' | Sort-Object PSChildName -Descending | Select-Object -First 1 -ExpandProperty Name; if ($jdkKey) { (Get-ItemProperty ('Registry::' + $jdkKey)).JavaHome }"`) do set "JAVA_HOME=%%I"

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

where java >nul 2>&1
if %ERRORLEVEL%==0 exit /b 0

echo Java runtime not found. Install Java and make sure java is available.
exit /b 1
