@echo off

if "%OS%"=="Windows_NT" @setlocal

set ADAPTORS="-Dgat.adaptor.path=external\deploy\adaptors"
set PROG_NAME="ibis.dog.client.Main"

rem %~dp0 is expanded pathname of the current script under NT
rem set CLIENT_HOME=%~dp0
set CLIENT_HOME=E:\ccgrid08-copy
echo "%CLIENT_HOME%"

set JAVA_EXEC=java
set JAVACLASSPATH=%CLASSPATH%;

for %%i in ("%CLIENT_HOME%\external\ibis\*.jar") do call "%CLIENT_HOME%\bin\AddToClassPath.bat" %%i

for %%i in ("%CLIENT_HOME%\external\freetts\*.jar") do call "%CLIENT_HOME%\bin\AddToClassPath.bat" %%i

for %%i in ("%CLIENT_HOME%\external\jfreechart\*.jar") do call "%CLIENT_HOME%\bin\AddToClassPath.bat" %%i

for %%i in ("%CLIENT_HOME%\external\video4J\*.jar") do call "%CLIENT_HOME%\bin\AddToClassPath.bat" %%i

for %%i in ("%CLIENT_HOME%\external\deploy\*.jar") do call "%CLIENT_HOME%\bin\AddToClassPath.bat" %%i

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

rem echo "%JAVA_EXEC%" -classpath "%JAVACLASSPATH%" %APP_ARGS%
echo "%JAVA_EXEC%" -classpath "%JAVACLASSPATH%" %ADAPTORS% %PROG_NAME% %APP_ARGS% 
"%JAVA_EXEC%" -classpath "%JAVACLASSPATH%" -Djava.library.path=%CLIENT_HOME%\external\native_libraries\ %ADAPTORS% %PROG_NAME% %APP_ARGS% 

if "%OS%"=="Windows_NT" @endlocal

