#!bin/bash

echo "testing the vertical scalabilty of graphlab"


echo "4 cores ***********************"
bash vs.glab.sh 20 4

echo "5 cores ***********************"
bash vs.glab.sh 20 5

echo "6 cores ***********************"
bash vs.glab.sh 20 6

