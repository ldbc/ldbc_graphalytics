#!/bin/bash

#Copy Input to DAS
sshpass -p '3jyJl1Ou' scp -rp /home/alien01/delft/MGR/competition/datasets/delft/directed/d_n_12 mbiczak@fs3.das4.tudelft.nl:/var/scratch/mbiczak/input

#Copy run script to das
sshpass -p '3jyJl1Ou' scp -rp runHadoop.sh mbiczak@fs3.das4.tudelft.nl:/home/mbiczak/scripts

#Connect to DAS4
sshpass -p '3jyJl1Ou' ssh mbiczak@fs3.das4.tudelft.nl 'bash -s' < initCluster.sh

exit