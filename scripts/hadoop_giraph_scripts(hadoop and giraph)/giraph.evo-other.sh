#!/bin/bash

############################################################################################
# each dataset is copied in a way to have exactly 20 blocks, thus fully utilize 20 wrokers #
############################################################################################

# Adding 0.1% of original graph n 6 hoops limit



# --------------------------------------------------------------------------------------------

echo "--- Copy KGS_1_FCF ---"
./client.sh dfs -D dfs.block.size=10998272 -copyFromLocal /var/scratch/yongguo/sc_dataset/KGS_1_FCF /local/hadoop.tmp.yongguo/KGS_1_FCF
mkdir -p /var/scratch/yongguo/output/giraph_evo/output_KGS_1_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for KGS_1_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ForestFireModelDriver undirected /local/hadoop.tmp.yongguo/KGS_1_FCF /local/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF 20 832246 290 0.5 0.5 6
    echo "--- Copy output ---"
    if [ $i -gt "1" ]
      then
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF/ffm
    fi
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_evo/output_KGS_1_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/KGS_1_FCF
echo "--- KGS_1_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy Friendster_FCF ---"
./client.sh dfs -D dfs.block.size=1647956992 -copyFromLocal /var/scratch/yongguo/sc_dataset/Friendster_FCF /local/hadoop.tmp.yongguo/Friendster_FCF
mkdir -p /var/scratch/yongguo/output/giraph_evo/output_Friendster_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for Friendster_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ForestFireModelDriver undirected /local/hadoop.tmp.yongguo/Friendster_FCF /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF 20 124836180 65000 0.5 0.5 6
    echo "--- Copy output ---"
    if [ $i -gt "1" ]
      then
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF/ffm
    fi
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_evo/output_Friendster_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

#./client.sh dfs -rm /local/hadoop.tmp.yongguo/Friendster_FCF
echo "--- Friendster_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy WikiTalk_FCF ---"
./client.sh dfs -D dfs.block.size=4561920 -copyFromLocal /var/scratch/yongguo/sc_dataset/WikiTalk_FCF /local/hadoop.tmp.yongguo/WikiTalk_FCF
mkdir -p /var/scratch/yongguo/output/giraph_evo/output_WikiTalk_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for WikiTalk_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ForestFireModelDriver directed /local/hadoop.tmp.yongguo/WikiTalk_FCF /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF 20 2394385 2400 0.5 0.5 6
    echo "--- Copy output ---"
    if [ $i -gt "1" ]
      then
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF/ffm
    fi
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_evo/output_WikiTalk_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/WikiTalk_FCF
echo "--- WikiTalk_FCF DONE ---"
