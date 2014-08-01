
# --------------------------------------------------------------------------------------------

echo "--- Copy Citation_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/yongguo/input/filtered/Citation_FCF /local/hadoop.tmp.yongguo
./client.sh dfs -D dfs.block.size=7884800 -copyFromLocal /var/scratch/yongguo/sc_dataset/Citation_FCF /local/hadoop.tmp.yongguo/Citation_FCF
mkdir -p /var/scratch/yongguo/output/giraph_conn/output_Citation_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for Citation_FCF ---"
    ./client.sh jar /home/mbiczak/hadoopJobs/hadoopJobs.jar org.hadoop.test.jobs.FilterDatasetJob directed false true 20 20 /local/hadoop.tmp.yongguo/Citation_FCF /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF filtered    
#./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ConnectedComponentDriver directed /local/hadoop.tmp.yongguo/Citation_FCF /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF 40
    echo "DEL RDUNDANT DATA"
    if [ $i -gt "1" ]
      then
echo "if"
   #     ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF/Max_ConnComp
    #    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF/labeledConnComp
    fi
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF/benchmark.txt /var/scratch/yongguo/output/giraph_conn/output_Citation_FCF/benchmark_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/Citation_FCF
echo "--- Citation_FCF DONE ---"

# --------------------------------------------------------------------------------------------
