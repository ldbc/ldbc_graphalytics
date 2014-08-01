#!/bin/bash

#####################################################################################################################
# each dataset is copied in a way to have exactly 20 blocks, thus fully utilize 20 wrokers (20mappers n 20reducers) #
#####################################################################################################################


echo "--- Copy Citation_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/mbiczak/input/filtered/Citation_FCF /local/hadoop.tmp.mbiczak
./client.sh dfs -D dfs.block.size=63078400 -copyFromLocal /var/scratch/mbiczak/input/filtered/Citation_FCF /local/hadoop.tmp.mbiczak/
mkdir /var/scratch/mbiczak/output/hadoop.conComp/output_citation

for i in 1 2 3 4 5 6 7 8 9 10 4 5 6 7 8 9 10
do
    echo "--- Run $i ConnComp for Citation_FCF ---"
    ./client.sh jar /home/mbiczak/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.FilterDatasetJob directed false true 5 5 /local/hadoop.tmp.mbiczak/Citation_FCF /local/hadoop.tmp.mbiczak/output_$i\_citation filtered
    echo "--- Removing redundant data ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.mbiczak/output_$i\_citation/filteredGraph
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.mbiczak/output_$i\_citation /var/scratch/mbiczak/output/hadoop.conComp/output_citation
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.mbiczak/output_$i\_citation
done

./client.sh dfs -rm /local/hadoop.tmp.mbiczak/Citation_FCF
echo "--- Citation_FCF DONE ---"

# --------------------------------------------------------------------------------------------


echo "--- Copy roadNet-CA.txt ---"
#./client.sh dfs -copyFromLocal /var/scratch/mbiczak/input/filtered/road_CA_FCF /local/hadoop.tmp.mbiczak
./client.sh dfs -D dfs.block.size=19353600 -copyFromLocal /var/scratch/mbiczak/input/filtered/road_CA_FCF /local/hadoop.tmp.mbiczak/
mkdir /var/scratch/mbiczak/output/hadoop.conComp/output_road_CA

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i ConnComp for road_CA_FCF ---"
    ./client.sh jar /home/mbiczak/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.FilterDatasetJob directed false true 5 5 /local/hadoop.tmp.mbiczak/road_CA_FCF /local/hadoop.tmp.mbiczak/output_$i\_road_CA filtered
    echo "--- Removing redundant data ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.mbiczak/output_$i\_road_CA/filteredGraph
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.mbiczak/output_$i\_road_CA /var/scratch/mbiczak/output/hadoop.conComp/output_road_CA
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.mbiczak/output_$i\_road_CA
done

./client.sh dfs -rm /local/hadoop.tmp.mbiczak/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------

