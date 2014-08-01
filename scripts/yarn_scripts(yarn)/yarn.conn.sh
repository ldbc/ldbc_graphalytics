#!/bin/bash

./client.sh fs -mkdir -p /local/hadoop.tmp.yongguo
echo "--- Copy amazon.302_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/amazon.302_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=939520 -copyFromLocal /var/scratch/yongguo/sc_dataset/amazon.302_FCF /local/hadoop.tmp.yongguo/amazon.302_FCF
mkdir -p /var/scratch/yongguo/output/yarn_conn/output_amazon.302_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for amazon.302_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.FilterDatasetJob directed false true 20 20 /local/hadoop.tmp.yongguo/amazon.302_FCF /local/hadoop.tmp.yongguo/output_$i\_amazon.302_FCF filtered
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_amazon.302_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_conn/output_amazon.302_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_amazon.302_FCF
    rm -rf /var/scratch/${USER}/yarn_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/amazon.302_FCF
echo "--- amazon.302_FCF DONE ---"

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


echo "--- Copy WikiTalk_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/WikiTalk_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=4561920 -copyFromLocal /var/scratch/yongguo/sc_dataset/WikiTalk_FCF /local/hadoop.tmp.yongguo/WikiTalk_FCF
mkdir -p /var/scratch/yongguo/output/yarn_conn/output_WikiTalk_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for WikiTalk_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.FilterDatasetJob directed false true 20 20 /local/hadoop.tmp.yongguo/WikiTalk_FCF /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF filtered
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_conn/output_WikiTalk_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF
    rm -rf /var/scratch/${USER}/yarn_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/WikiTalk_FCF
echo "--- WikiTalk_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy Xfire_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/Xfire_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=23996416 -copyFromLocal /var/scratch/yongguo/sc_dataset/Xfire_FCF /local/hadoop.tmp.yongguo/Xfire_FCF
mkdir -p /var/scratch/yongguo/output/yarn_conn/output_Xfire_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for Xfire_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.FilterDatasetJob undirected false true 20 20 /local/hadoop.tmp.yongguo/Xfire_FCF /local/hadoop.tmp.yongguo/output_$i\_Xfire_FCF filtered
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_Xfire_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_conn/output_Xfire_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Xfire_FCF
    rm -rf /var/scratch/${USER}/yarn_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/Xfire_FCF
echo "--- Xfire_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy DotaLeague_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/DotaLeague_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=34316288 -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /local/hadoop.tmp.yongguo/DotaLeague_FCF
mkdir -p /var/scratch/yongguo/output/yarn_conn/output_DotaLeague_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for DotaLeague_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.FilterDatasetJob undirected false true 20 20 /local/hadoop.tmp.yongguo/DotaLeague_FCF /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF filtered
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_conn/output_DotaLeague_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF
    rm -rf /var/scratch/${USER}/yarn_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/DotaLeague_FCF
echo "--- DotaLeague_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy Friendster_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/Friendster_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=1647956992 -copyFromLocal /var/scratch/yongguo/sc_dataset/Friendster_FCF /local/hadoop.tmp.yongguo/Friendster_FCF
mkdir -p /var/scratch/yongguo/output/yarn_conn/output_Friendster_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for Friendster_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.FilterDatasetJob undirected false true 20 20 /local/hadoop.tmp.yongguo/Friendster_FCF /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF filtered
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_conn/output_Friendster_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF
    rm -rf /var/scratch/${USER}/yarn_logs/userlogs
done

#./client.sh dfs -rm /local/hadoop.tmp.yongguo/Friendster_FCF
echo "--- Friendster_FCF DONE ---"

# --------------------------------------------------------------------------------------------

