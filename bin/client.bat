@echo off

if "%OS%"=="Windows_NT" @setlocal

set S_ADDRESS="-Dibis.server.address=localhost-8888"
set POOL_NAME="-Dibis.pool.name=ibisDist"
set PROG_NAME="MyApp"

rem %~dp0 is expanded pathname of the current script under NT
rem set CLIENT_HOME=%~dp0
set CLIENT_HOME=C:\Java\SCALE
echo "%CLIENT_HOME%"

set JAVA_EXEC=java
set JAVACLASSPATH=%CLASSPATH%;

for %%i in ("%CLIENT_HOME%\external\ibis\*.jar") do call "%CLIENT_HOME%\bin\AddToClassPath.bat" %%i

for %%i in ("%CLIENT_HOME%\external\freetts\*.jar") do call "%CLIENT_HOME%\bin\AddToClassPath.bat" %%i

for %%i in ("%CLIENT_HOME%\jars\*.jar") do call "%CLIENT_HOME%\bin\AddToClassPath.bat" %%i

rem From Ant:
rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway)
set APP_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:setupArgs
if ""%1""=="""" goto doneStart
set APP_ARGS=%APP_ARGS% %1
shift
goto setupArgs
rem This label provides a place for the argument list loop to break out
rem and for NT handling to skip to.

:doneStart

if not "%JAVA_HOME%"=="" (
    set JAVA_EXEC=%JAVA_HOME%\bin\%JAVA_EXEC%
)

echo "%JAVA_EXEC%" -classpath "%JAVACLASSPATH%" %APP_ARGS%
"%JAVA_EXEC%" -classpath "%JAVACLASSPATH%" %S_ADDRESS% %POOL_NAME% %PROG_NAME% %APP_ARGS% 

if "%OS%"=="Windows_NT" @endlocal

