#!bin/bash

echo "testing the vertical scalabilty of graphlab"

echo "1 cores ***********************"
bash vs.glab.sh 20 1


echo "7 cores ***********************"
bash vs.glab.sh 20 7

echo "2 cores ***********************"
bash vs.glab.sh 20 2

echo "3 cores ***********************"
bash vs.glab.sh 20 3

echo "4 cores ***********************"
bash vs.glab.sh 20 4

echo "5 cores ***********************"
bash vs.glab.sh 20 5

echo "6 cores ***********************"
bash vs.glab.sh 20 6

