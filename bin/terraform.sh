#!/bin/sh

# == BEGIN INSTALLER MODIFICATIONS ===============================================

JAVA_OPTS="-cp"
#JAVA_DEBUG_OPTS="@JAVA_DEBUG_OPTS@"
#JAVA_HOME="@JAVA_HOME@"
#ANT_HOME="$ANTHILL_HOME/opt/apache-ant-1.7.1"
#GROOVY_HOME="$ANTHILL_HOME/opt/@GROOVY_VER@"
javacmd="$JAVA_HOME/bin/java"

# user extensible script, preserved on upgrade
#if [ -r "$ANTHILL_HOME/bin/setenv.sh" ];
#then
#  . "$ANTHILL_HOME/bin/setenv.sh"
#fi

# == END INSTALLER MODIFICATIONS =================================================

INPUT_FILE=$2
INPUT_FILE=`readlink -f $INPUT_FILE`
CREDS=$3
create_class=org.urbancode.terraform.main.Main
destroy_class=org.urbancode.terraform.main.Main

export JAVA_HOME
export INPUT_FILE
export CREDS

# -- Create ---------------------------------------------------------------------
CP="$TERRAFORM_HOME/lib/*:$TERRAFORM_HOME/build/main/jar/*:$TERRAFORM_HOME/src/conf:$TERRAFORM_HOME/dist/*"
PROPS=""

for ARG in $@; do
  if [ "$ARG" = "$1" ]; then
   echo "skip"
  elif [ "$ARG" = "$2" ] ; then
   echo "skip"
  elif [ "$ARG" = "$3" ] ; then
   echo "skip"
  else 
    PROPS="$PROPS $ARG" 
  fi
done
echo "Properties: $PROPS" 

if [ "$1" = "create" ] ; then
  cd "$TERRAFORM_HOME/bin"
  command_line="\"$javacmd\" $JAVA_OPTS 
    \"$CP\" \
    $create_class create \"$INPUT_FILE\" \"$CREDS\" $PROPS >\"$TERRAFORM_HOME/bin/stdout\" 2>&1 &"
  echo $command_line
  eval $command_line

# -- Destroy ----------------------------------------------------------------------

elif [ "$1" = "destroy" ] ; then

  shift
  FORCE=0
  cd "$TERRAFORM_HOME/bin"
  command_line="exec \"$javacmd\" $JAVA_OPTS 
    \"$CP\" \
    $destroy_class destroy \"$INPUT_FILE\" \"$CREDS\" >\"$TERRAFORM_HOME/bin/stdout\" 2>&1 &"
  echo $command_line
  eval $command_line

# -- Usage ---------------------------------------------------------------------

else
#	Also, create -debug|run -debug to instruct java to listen on port 10000
#	for remote JPDA Debugger connections.
  echo "Usage: uprovision {create|destroy} {input-file} {credentials-file} {prop1=val1 prop2=val2 ... }"
  exit 1
fi
