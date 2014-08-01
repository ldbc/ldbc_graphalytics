#!/bin/bash

echo 'Connecting to DAS4 delft site'
sshpass -p '3jyJl1Ou' ssh mbiczakk@fs3.das4.tudelft.nl
echo 'Checking if test have finished'
read -r screenId < /home/mbiczak/scripts/screenId
isRunning=`kill -0 $screenId`
if [ "$isRunning" != "0" ]; then
	echo 'Test are still running, will exit now'
else
#Copy OUTPUT to Local
sshpass -p '3jyJl1Ou' scp -rp mbiczak@fs3.das4.tudelft.nl:/var/scratch/mbiczak/output/output_d_n_12* /home/alien01/delft/MGR/competition/Hadoop/output

todo shutdown cluster
fi
echo 'DONE :)'
