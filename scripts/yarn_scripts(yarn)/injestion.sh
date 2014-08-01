#!/bin/bash

############################################################################################
# each dataset is copied in a way to have exactly 20 blocks, thus fully utilize 20 wrokers (20mappers n 20reducers) #
############################################################################################

function getTiming() {
    start=$1
    end=$2

    start_s=$(echo $start | cut -d '.' -f 1)
    start_ns=$(echo $start | cut -d '.' -f 2)
    end_s=$(echo $end | cut -d '.' -f 1)
    end_ns=$(echo $end | cut -d '.' -f 2)


# for debug..  
#    echo $start  
#    echo $end  


    time=$(( ( 10#$end_s - 10#$start_s ) * 1000 + ( 10#$end_ns / 1000000 - 10#$start_ns / 1000000 ) ))


    echo "$time"  > /home/yongguo/yarn_scripts/injestion_time/$3
}

./client.sh fs -mkdir -p /local/hadoop.tmp.yongguo


echo "--- Copy amazon.302_FCF ---"

for i in 1 2 3 4 5 6 7 8 9 10
do
	
    echo "--- Run $i Stats for amazon.302_FCF ---"
    start=$(date +%s.%N)
    ./client.sh dfs -D dfs.block.size=939520 -copyFromLocal /var/scratch/yongguo/sc_dataset/amazon.302_FCF /local/hadoop.tmp.yongguo/amazon.302_FCF
    end=$(date +%s.%N)
    getTiming $start $end amazon.302_FCF_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rm /local/hadoop.tmp.yongguo/amazon.302_FCF
    sleep 5
done

echo "--- amazon.302_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy Citation_FCF ---"

for i in 1 2 3 4 5 6 7 8 9 10
do
	
    echo "--- Run $i Stats for Citation_FCF ---"
    start=$(date +%s.%N)
    ./client.sh dfs -D dfs.block.size=15769600 -copyFromLocal /var/scratch/yongguo/sc_dataset/Citation_FCF /local/hadoop.tmp.yongguo/Citation_FCF
    end=$(date +%s.%N)
    getTiming $start $end Citation_FCF_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rm /local/hadoop.tmp.yongguo/Citation_FCF 
    sleep 5
done

echo "--- Citation_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy WikiTalk_FCF ---"

for i in 1 2 3 4 5 6 7 8 9 10
do
	
    echo "--- Run $i Stats for WikiTalk_FCF ---"
    start=$(date +%s.%N)
    ./client.sh dfs -D dfs.block.size=4561920 -copyFromLocal /var/scratch/yongguo/sc_dataset/WikiTalk_FCF /local/hadoop.tmp.yongguo/WikiTalk_FCF
    end=$(date +%s.%N)
    getTiming $start $end WikiTalk_FCF_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rm /local/hadoop.tmp.yongguo/WikiTalk_FCF
    sleep 5
done

echo "--- WikiTalk_FCF DONE ---"


# --------------------------------------------------------------------------------------------


echo "--- Copy KGS_1_FCF ---"

for i in 1 2 3 4 5 6 7 8 9 10
do
	
    echo "--- Run $i Stats for KGS_1_FCF ---"
    start=$(date +%s.%N)
    ./client.sh dfs -D dfs.block.size=10998272 -copyFromLocal /var/scratch/yongguo/sc_dataset/KGS_1_FCF /local/hadoop.tmp.yongguo/KGS_1_FCF
    end=$(date +%s.%N)
    getTiming $start $end KGS_1_FCF_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rm /local/hadoop.tmp.yongguo/KGS_1_FCF
    sleep 5
done


echo "--- KGS_1_FCF DONE ---"
# --------------------------------------------------------------------------------------------

echo "--- Copy DotaLeague_FCF ---"

for i in 1 2 3 4 5 6 7 8 9 10
do
	
    echo "--- Run $i Stats for DotaLeague_FCF ---"
    start=$(date +%s.%N)
    ./client.sh dfs -D dfs.block.size=34316288 -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /local/hadoop.tmp.yongguo/DotaLeague_FCF
    end=$(date +%s.%N)
    getTiming $start $end DotaLeague_FCF_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rm /local/hadoop.tmp.yongguo/DotaLeague_FCF
    sleep 10
done


echo "--- DotaLeague_FCF DONE ---"
# --------------------------------------------------------------------------------------------

echo "--- Copy Friendster_FCF ---"

for i in 1 2 3 4 5 6 7 8 9 10
do
	
    echo "--- Run $i Stats for Friendster_FCF ---"
    start=$(date +%s.%N)
    ./client.sh dfs -D dfs.block.size=1647956992 -copyFromLocal /var/scratch/yongguo/sc_dataset/Friendster_FCF /local/hadoop.tmp.yongguo/Friendster_FCF
    end=$(date +%s.%N)
    getTiming $start $end Friendster_FCF_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rm /local/hadoop.tmp.yongguo/Friendster_FCF
    sleep 20
done


echo "--- Friendster_FCF DONE ---"
./stop-hadoop-cluster.sh
