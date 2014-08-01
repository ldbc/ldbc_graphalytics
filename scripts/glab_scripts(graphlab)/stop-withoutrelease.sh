#!/bin/bash

# create settings
. ./settings.sh

#Connect to Master and stop 
Master=`cat $HADOOP_MASTERS`
ssh $USER@$Master 'bash -s' < stopHadoop.sh $HADOOP_HOME

# wait just in case
sleep 5


echo "THE END"
