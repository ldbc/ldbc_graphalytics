#!/bin/bash

# create settings
. ./settings.sh

echo "@@@ stop MPI @@@"
MPI_MASTER=`head -1 $SCRIPTS/machines`
echo "mpi first node" $MPI_MASTER
ssh $USER@$MPI_MASTER 'bash -s' < stopMPI.sh



#Connect to Master and stop 
Master=`cat $HADOOP_MASTERS`
ssh $USER@$Master 'bash -s' < stopHadoop.sh $HADOOP_HOME

# wait just in case
sleep 5


qdel -u $USER

echo "THE END"
