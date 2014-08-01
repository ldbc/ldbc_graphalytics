#!/bin/bash

############################################################################################
# each dataset is copied in a way to have exactly 20 blocks, thus fully utilize 20 wrokers #
############################################################################################


echo "--- Copy graph500_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/graph500_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=50535424 -copyFromLocal /var/scratch/yongguo/sc_dataset/graph500_FCF /local/hadoop.tmp.yongguo/graph500_FCF
mkdir -p /var/scratch/yongguo/output/giraph_bfs/output_graph500_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for graph500_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.BFSJob -D mapred.child.java.opts="-Xms21000m -Xmx21000m" undirected /local/hadoop.tmp.yongguo/graph500_FCF /local/hadoop.tmp.yongguo/output_$i\_graph500_FCF 20 287144
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_graph500_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_bfs/output_graph500_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_graph500_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/graph500_FCF
echo "--- graph500_FCF DONE ---"
./stop-hadoop-cluster.sh
