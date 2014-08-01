#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}' | head -1`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}' | head -1`
echo $nodename

mkdir -p /var/scratch/yongguo/output/bfs




echo "--- Copy Citation_FCF ---"
./client.sh dfs -D dfs.block.size=15769600 -copyFromLocal /var/scratch/yongguo/sc_dataset/Citation_FCF /local/hadoop.tmp.yongguo/Citation_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for Citation_FCF ---"
	bash conn.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.ConnectedComponent 20 hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/Citation_FCF hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output_$i\_Citation_FCF d Citation_FCF $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Citation_FCF
done
./client.sh dfs -rm /local/hadoop.tmp.yongguo/Citation_FCF
echo "--- Citation_FCF DONE ---"

./stop-np-cluster.sh
