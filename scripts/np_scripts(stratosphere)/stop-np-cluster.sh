#!/bin/bash

# create settings
. ./settings.sh

#Connect to Master and stop 
Master=`cat $HADOOP_MASTERS`
#stop stratosphere
ssh $USER@$Master 'bash -s' < stopStratosphere.sh $NP_HOME

# wait just in case
sleep 5


#stop hadoop
ssh $USER@$Master 'bash -s' < stopHadoop.sh $HADOOP_HOME

# wait just in case
sleep 5


qdel -u $USER

echo "THE END"
