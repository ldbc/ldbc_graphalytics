#!bin/bash

echo "testing the vertical scalabilty of graphlab"



echo "50 ***********************"
bash msvs.glab.sh 50 1


./stop-glab-cluster.sh
