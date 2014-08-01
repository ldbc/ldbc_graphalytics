#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}'| head -1`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}' | head -1`
echo $nodename

mkdir -p /var/scratch/yongguo/output/np_bfs

echo "--- Copy bbo_edgetonode ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/bbo_edgetonode /local/yongguo/hadoop.tmp.yongguo/bbo_edgetonode

for i in 1 2 # 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for bbo_edgetonode ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 49  hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/bbo_edgetonode hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_bbo_edgetonode 0 u bbo_edgetonode_dir $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_bbo_edgetonode /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_bbo_edgetonode
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/bbo_edgetonode
echo "--- bbo_edgetonode DONE ---"

# --------------------------------------------------------------------------------------------

