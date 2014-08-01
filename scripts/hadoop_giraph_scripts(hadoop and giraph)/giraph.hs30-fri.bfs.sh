#!/bin/bash

############################################################################################
# each dataset is copied in a way to have exactly 30 blocks, thus fully utilize 30 wrokers #
############################################################################################





echo "--- Copy Friendster_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/Friendster_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=1098633728 -copyFromLocal /var/scratch/yongguo/sc_dataset/Friendster_FCF /local/hadoop.tmp.yongguo/Friendster_FCF
mkdir -p /var/scratch/yongguo/output/giraph_bfs/hs30_output_Friendster_FCF

for i in 1 2 3 #4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for Friendster_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.BFSJob -D mapred.child.java.opts="-Xms20480m -Xmx20480m" undirected /local/hadoop.tmp.yongguo/Friendster_FCF /local/hadoop.tmp.yongguo/hs30_output_$i\_Friendster_FCF 30 71768986
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/hs30_output_$i\_Friendster_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_bfs/hs30_output_Friendster_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/hs30_output_$i\_Friendster_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

#./client.sh dfs -rm /local/hadoop.tmp.yongguo/Friendster_FCF
echo "--- Friendster_FCF DONE ---"

# --------------------------------------------------------------------------------------------
./stop-hadoop-cluster.sh
