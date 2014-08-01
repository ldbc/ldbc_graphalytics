#!/bin/bash

############################################################################################
# each dataset is copied in a way to have exactly 20 blocks, thus fully utilize 20 wrokers (10mappers n 10reducers) #
############################################################################################
./client.sh fs -mkdir -p /yongguo_local/hadoop.tmp.yongguo

echo "--- Copy Citation_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/Citation_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=15769600 -copyFromLocal /var/scratch/yongguo/sc_dataset/Citation_FCF /yongguo_local/hadoop.tmp.yongguo/Citation_FCF
mkdir -p /var/scratch/yongguo/output/hadoop_bfs/output_Citation_FCF
echo "***test****"

./client.sh fs -mkdir -p /yongguo_local/hadoop.tmp.yongguo
