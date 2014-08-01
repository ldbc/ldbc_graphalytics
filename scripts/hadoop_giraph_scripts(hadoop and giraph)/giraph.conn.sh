#!/bin/bash

############################################################################################
# each dataset is copied in a way to have exactly 40 blocks, thus fully utilize 20 wrokers #
############################################################################################


echo "--- Copy amazon.302_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/amazon.302_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=470016 -copyFromLocal /var/scratch/yongguo/sc_dataset/amazon.302_FCF /local/hadoop.tmp.yongguo/amazon.302_FCF
mkdir -p /var/scratch/yongguo/output/giraph_conn/output_amazon.302_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for amazon.302_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ConnectedComponentDriver directed /local/hadoop.tmp.yongguo/amazon.302_FCF /local/hadoop.tmp.yongguo/output_$i\_amazon.302_FCF 40
    echo "DEL RDUNDANT DATA"
    if [ $i -gt "1" ]
      then
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_amazon.302_FCF/Max_ConnComp
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_amazon.302_FCF/labeledConnComp
    fi
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_amazon.302_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_conn/output_amazon.302_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_amazon.302_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/amazon.302_FCF
echo "--- amazon.302_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy Citation_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/Citation_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=7884800 -copyFromLocal /var/scratch/yongguo/sc_dataset/Citation_FCF /local/hadoop.tmp.yongguo/Citation_FCF
mkdir -p /var/scratch/yongguo/output/giraph_conn/output_Citation_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for Citation_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ConnectedComponentDriver directed /local/hadoop.tmp.yongguo/Citation_FCF /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF 40
    echo "DEL RDUNDANT DATA"
    if [ $i -gt "1" ]
      then
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF/Max_ConnComp
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF/labeledConnComp
    fi
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_conn/output_Citation_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/Citation_FCF
echo "--- Citation_FCF DONE ---"

# --------------------------------------------------------------------------------------------


echo "--- Copy WikiTalk_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/WikiTalk_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=2280960 -copyFromLocal /var/scratch/yongguo/sc_dataset/WikiTalk_FCF /local/hadoop.tmp.yongguo/WikiTalk_FCF
mkdir -p /var/scratch/yongguo/output/giraph_conn/output_WikiTalk_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for WikiTalk_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ConnectedComponentDriver directed /local/hadoop.tmp.yongguo/WikiTalk_FCF /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF 40
    echo "DEL RDUNDANT DATA"
    if [ $i -gt "1" ]
      then
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF/Max_ConnComp
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF/labeledConnComp
    fi
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_conn/output_WikiTalk_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/WikiTalk_FCF
echo "--- WikiTalk_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy Xfire_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/Xfire_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=11998208 -copyFromLocal /var/scratch/yongguo/sc_dataset/Xfire_FCF /local/hadoop.tmp.yongguo/Xfire_FCF
mkdir -p /var/scratch/yongguo/output/giraph_conn/output_Xfire_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for Xfire_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ConnectedComponentDriver undirected /local/hadoop.tmp.yongguo/Xfire_FCF /local/hadoop.tmp.yongguo/output_$i\_Xfire_FCF 40
    echo "DEL RDUNDANT DATA"
    if [ $i -gt "1" ]
      then
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Xfire_FCF/Max_ConnComp
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Xfire_FCF/labeledConnComp
    fi
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_Xfire_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_conn/output_Xfire_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Xfire_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/Xfire_FCF
echo "--- Xfire_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy DotaLeague_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/DotaLeague_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=17158144 -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /local/hadoop.tmp.yongguo/DotaLeague_FCF
mkdir -p /var/scratch/yongguo/output/giraph_conn/output_DotaLeague_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for DotaLeague_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ConnectedComponentDriver undirected /local/hadoop.tmp.yongguo/DotaLeague_FCF /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF 40
    echo "DEL RDUNDANT DATA"
    if [ $i -gt "1" ]
      then
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF/Max_ConnComp
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF/labeledConnComp
    fi
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_conn/output_DotaLeague_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/DotaLeague_FCF
echo "--- DotaLeague_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy Friendster_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/Friendster_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=823978496 -copyFromLocal /var/scratch/yongguo/sc_dataset/Friendster_FCF /local/hadoop.tmp.yongguo/Friendster_FCF
mkdir -p /var/scratch/yongguo/output/giraph_conn/output_Friendster_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for Friendster_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ConnectedComponentDriver undirected /local/hadoop.tmp.yongguo/Friendster_FCF /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF 40
    echo "DEL RDUNDANT DATA"
    if [ $i -gt "1" ]
      then
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF/Max_ConnComp
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF/labeledConnComp
    fi
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_conn/output_Friendster_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

#./client.sh dfs -rm /local/hadoop.tmp.yongguo/Friendster_FCF
echo "--- Friendster_FCF DONE ---"

# --------------------------------------------------------------------------------------------

