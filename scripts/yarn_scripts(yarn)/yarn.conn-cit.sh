#!/bin/bash

./client.sh fs -mkdir -p /local/hadoop.tmp.yongguo
# --------------------------------------------------------------------------------------------

echo "--- Copy Citation_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/Citation_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=15769600 -copyFromLocal /var/scratch/yongguo/sc_dataset/Citation_FCF /local/hadoop.tmp.yongguo/Citation_FCF
mkdir -p /var/scratch/yongguo/output/yarn_conn/output_Citation_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for Citation_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.FilterDatasetJob directed false true 20 20 /local/hadoop.tmp.yongguo/Citation_FCF /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF filtered
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_conn/output_Citation_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF
    rm -rf /var/scratch/${USER}/yarn_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/Citation_FCF
echo "--- Citation_FCF DONE ---"

# --------------------------------------------------------------------------------------------

./stop-hadoop-cluster.sh
