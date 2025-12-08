#!/bin/bash
APPDIR=~/app2
PIDFILE=$APPDIR/pid.file
PID=$(cat $PIDFILE)
echo "Stop Benew application"
kill $PID
rm $PIDFILE && echo "Application stopped"