#!/bin/bash

#Starting run script
echo 'run script invoked'
cd /home/mbiczak/scripts/
screen -d -m ./runHadoop.sh

#Recording screen session ID
screenId=`ps -u mbiczak | grep screen | grep -v grep | awk '{print $1}'`
echo $screenId > /home/mbiczak/scripts/screenId

exit