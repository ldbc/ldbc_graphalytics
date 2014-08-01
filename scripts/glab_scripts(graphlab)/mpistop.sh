#!/bin/bash

# create settings
. ./settings.sh

echo "@@@ stop MPI @@@"
MPI_MASTER=`head -1 $SCRIPTS/machines`
echo "mpi first node" $MPI_MASTER
ssh $USER@$MPI_MASTER 'bash -s' < stopMPI.sh



sleep 5

echo "THE END"
