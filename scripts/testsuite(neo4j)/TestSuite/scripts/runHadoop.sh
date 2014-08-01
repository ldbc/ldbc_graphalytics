#!/bin/bash

cd ~/scripts
#Overwrite cluster config
sed -ie '4s/.*/#$ -t 1-4/' hadoop-cluster.sh
#Start Hadoop cluster
qsub -p 0 hadoop-cluster.sh

#Checking for Cluster Barriers
until [ -f ./BARR* ]; do
	echo "Waiting for Barriers"
	sleep 1
done
echo "Barriers found"
until [ ! -f ./BARR* ]; do
	 echo "Barriers still present"
	sleep 10
done
echo "Cluster ready"
#wait 10s just in case
sleep 10

cd ~/scripts
echo "--------- d_n_12 ---------"
echo 'copy d_n_12 to dfs'
./client.sh dfs -copyFromLocal /var/scratch/mbiczak/input/d_n_12 /local/hadoop.tmp.mbiczak/
echo "### Running stats for d_n_12 ###"
./client.sh jar /home/mbiczak/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.GraphStatsJob directed delft false true 1 1 /local/hadoop.tmp.mbiczak/d_n_12 /local/hadoop.tmp.mbiczak/output_d_n_12_0
echo "### Fetching output from DFS ###"
./client.sh dfs -copyToLocal /local/hadoop.tmp.mbiczak/output_d_n_12_0 /var/scratch/mbiczak/output
echo "### Cleaning wiki output in DFS ###"
./client.sh dfs -rmr /local/hadoop.tmp.mbiczak/output_d_n_12_0
echo "Done"
echo "--------- END d_n_12 ---------"

