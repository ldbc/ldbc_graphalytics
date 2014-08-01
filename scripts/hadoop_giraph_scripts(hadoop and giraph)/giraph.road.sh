echo "--- Copy road_CA_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/road_CA_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=2796544  -copyFromLocal /var/scratch/yongguo/sc_dataset/road_CA_FCF /local/hadoop.tmp.yongguo/road_CA_FCF
mkdir -p /var/scratch/yongguo/output/giraph_stats/output_road_CA_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for road_CA_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.StatsJob undirected /local/hadoop.tmp.yongguo/road_CA_FCF /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF 40 0
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_stats/output_road_CA_FCF/benchmark_$i
    echo "--- Clear dfs ---"
#    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------



echo "--- Copy road_CA_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/road_CA_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=1398272 -copyFromLocal /var/scratch/yongguo/sc_dataset/road_CA_FCF /local/hadoop.tmp.yongguo/road_CA_FCF
mkdir -p /var/scratch/yongguo/output/giraph_bfs/output_road_CA_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for road_CA_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.BFSJob -D mapred.child.java.opts="-Xms21000m -Xmx21000m" undirected /local/hadoop.tmp.yongguo/road_CA_FCF /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF 40 1
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_bfs/output_road_CA_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------




echo "--- Copy road_CA_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/road_CA_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=1398272 -copyFromLocal /var/scratch/yongguo/sc_dataset/road_CA_FCF /local/hadoop.tmp.yongguo/road_CA_FCF
mkdir -p /var/scratch/yongguo/output/giraph_conn/output_road_CA_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for road_CA_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ConnectedComponentDriver undirected /local/hadoop.tmp.yongguo/road_CA_FCF /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF 40
    echo "DEL RDUNDANT DATA"
    if [ $i -gt "1" ]
      then
        ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/Max_ConnComp
        ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/labeledConnComp
    fi
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_conn/output_road_CA_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------

echo "--- Copy road_CA_FCF ---"
./client.sh dfs -D dfs.block.size=1398272 -copyFromLocal /var/scratch/yongguo/sc_dataset/road_CA_FCF /local/hadoop.tmp.yongguo/road_CA_FCF
mkdir -p /var/scratch/yongguo/output/giraph_comm/output_road_CA_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for road_CA_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.LabelPropagationDriver undirected /local/hadoop.tmp.yongguo/road_CA_FCF /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF 40 1 0.1 0.1 6
    echo "--- Copy output ---"
    #./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/communities
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_comm/output_road_CA_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------




echo "--- Copy road_CA_FCF ---"
./client.sh dfs -D dfs.block.size=1398272 -copyFromLocal /var/scratch/yongguo/sc_dataset/road_CA_FCF /local/hadoop.tmp.yongguo/road_CA_FCF
mkdir -p /var/scratch/yongguo/output/giraph_evo/output_road_CA_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for road_CA_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ForestFireModelDriver undirected /local/hadoop.tmp.yongguo/road_CA_FCF /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF 40 1971281 1900 0.5 0.5 6
    echo "--- Copy output ---"
    if [ $i -gt "1" ]
      then
        ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/ffm
    fi
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_evo/output_road_CA_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    rm -rf /var/scratch/${USER}/hadoop_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------

