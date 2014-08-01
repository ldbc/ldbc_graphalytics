#!/bin/bash

############################################################################################
# each dataset is copied in a way to have exactly 50 blocks, thus fully utilize 50 wrokers (10mappers n 10reducers) #
############################################################################################
./client.sh fs -mkdir -p /local/hadoop.tmp.yongguo

echo "--- Copy DotaLeague_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/DotaLeague_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=13731328 -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /local/hadoop.tmp.yongguo/DotaLeague_FCF
mkdir -p /var/scratch/yongguo/output/yarn_bfs/hs50_output_DotaLeague_FCF

for i in 1 2 3 #4 5 #6 7 8 9 10
do
    echo "--- Run $i Stats for DotaLeague_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.BFSJob undirected filtered false false 50 50 /local/hadoop.tmp.yongguo/DotaLeague_FCF /local/hadoop.tmp.yongguo/hs50_output_$i\_DotaLeague_FCF 0
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/hs50_output_$i\_DotaLeague_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_bfs/hs50_output_DotaLeague_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/hs50_output_$i\_DotaLeague_FCF
    rm -rf /var/scratch/${USER}/yarn_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/DotaLeague_FCF
echo "--- DotaLeague_FCF DONE ---"



./stop-hadoop-cluster.sh
