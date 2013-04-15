@echo off


color 0F
title terraform
goto start

:start
set javacmd=java

set main_class=com.urbancode.terraform.main.Main

set currdir=%cd%
set SCRIPT_HOME="%~dp0"
cd %SCRIPT_HOME%
pushd..
set TERRAFORM_HOME=%cd%
set JARFILE=%cd%\target

cd %currdir%
IF EXIST "%TERRAFORM_HOME%\lib" ( 
	echo "lib directory exists"
	set CLASSPATH=%TERRAFORM_HOME%\lib\*
) ELSE (
	echo "building cp with maven"
	cd %TERRAFORM_HOME%
	mvn dependency:build-classpath -Dmdep.outputFile="%SCRIPT_HOME%\classpath"
	set CLASSPATH=type %SCRIPT_HOME%\classpath
	cd %currdur%
	goto cont
)


:cont
set CLASSPATH="%CLASSPATH%;%TERRAFORM_HOME%\target\*;%TERRAFORM_HOME%\conf"
echo %CLASSPATH%

set OPTION=%1
set INFILE=%2
set CRFILE=%3

shift 
shift
shift
shift

:startlaunch
echo OPTION %OPTION%
if "%OPTION%" == "create" goto launch
if "%OPTION%" == "destroy" goto launch
goto displayhelp



:launch
echo "LAUNCHING"
set TERRAFORM_COMMAND=%javacmd% -cp %CLASSPATH% "%main_class%" %*
echo %TERRAFORM_COMMAND%
%TERRAFORM_COMMAND%
goto finished

:displayhelp
echo "Usage: terraform.cmd {create|destroy} {input-file} {credentials-file} {prop1=val1 prop2=val2 ... }"

:finished
echo DONE
