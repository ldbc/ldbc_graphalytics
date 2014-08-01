./client.sh fs -mkdir -p /local/hadoop.tmp.yongguo

echo "--- Copy road_CA_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/road_CA_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=2779648 -copyFromLocal /var/scratch/yongguo/sc_dataset/road_CA_FCF /local/hadoop.tmp.yongguo/road_CA_FCF
mkdir -p /var/scratch/yongguo/output/yarn_stats/output_road_CA_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for road_CA_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.GraphStatsJob undirected filtered false true 20 20 /local/hadoop.tmp.yongguo/road_CA_FCF /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_stats/output_road_CA_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    rm -rf /var/scratch/${USER}/yarn_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------


echo "--- Copy road_CA_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/road_CA_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=2779648 -copyFromLocal /var/scratch/yongguo/sc_dataset/road_CA_FCF /local/hadoop.tmp.yongguo/road_CA_FCF
mkdir -p /var/scratch/yongguo/output/yarn_bfs/output_road_CA_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for road_CA_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.BFSJob undirected filtered false false 20 20 /local/hadoop.tmp.yongguo/road_CA_FCF /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF 1
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_bfs/output_road_CA_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    rm -rf /var/scratch/${USER}/yarn_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------
echo "--- Copy road_CA_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/road_CA_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=2779648 -copyFromLocal /var/scratch/yongguo/sc_dataset/road_CA_FCF /local/hadoop.tmp.yongguo/road_CA_FCF
mkdir -p /var/scratch/yongguo/output/yarn_comm/output_road_CA_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for road_CA_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.CambridgeLPADriver undirected filtered false false 20 20 /local/hadoop.tmp.yongguo/road_CA_FCF /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF 0.1 0.1 5 das
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_comm/output_road_CA_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    rm -rf /var/scratch/${USER}/yarn_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------
echo "--- Copy road_CA_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/road_CA_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=2779648 -copyFromLocal /var/scratch/yongguo/sc_dataset/road_CA_FCF /local/hadoop.tmp.yongguo/road_CA_FCF
mkdir -p /var/scratch/yongguo/output/yarn_conn/output_road_CA_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for road_CA_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.FilterDatasetJob undirected false true 20 20 /local/hadoop.tmp.yongguo/road_CA_FCF /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF filtered
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_conn/output_road_CA_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    rm -rf /var/scratch/${USER}/yarn_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------
echo "--- Copy road_CA_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/road_CA_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=2779648 -copyFromLocal /var/scratch/yongguo/sc_dataset/road_CA_FCF /local/hadoop.tmp.yongguo/road_CA_FCF
mkdir -p /var/scratch/yongguo/output/yarn_evo/output_road_CA_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for road_CA_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.ForestFireModelDriver undirected filtered false false 20 20 /local/hadoop.tmp.yongguo/road_CA_FCF /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF 1971281 1900 0.5 0.5 6 das
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF/benchmark.txt /var/scratch/yongguo/output/yarn_evo/output_road_CA_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_road_CA_FCF
    rm -rf /var/scratch/${USER}/yarn_giraph_logs/userlogs
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/road_CA_FCF
echo "--- road_CA_FCF DONE ---"

# --------------------------------------------------------------------------------------------

