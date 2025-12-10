#!/bin/bash
APPDIR=~/app2
LOGDIR=$APPDIR/log
CONFIG=$APPDIR/config/application.properties
echo "Start benew application"
mkdir -p $LOGDIR
nohup java -Dspring.config.location="$CONFIG" -jar benew*.jar >> $LOGDIR/app.log 2>&1 &
echo $! > $APPDIR/pid.file