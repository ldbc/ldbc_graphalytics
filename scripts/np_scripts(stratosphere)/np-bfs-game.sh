#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
echo $nodename

mkdir -p /var/scratch/yongguo/output/bfs

# --------------------------------------------------------------------------------------------

echo "--- Copy KGS_0_FCF ---"
./client.sh dfs -D dfs.block.size=14594048 -copyFromLocal /var/scratch/yongguo/dataset/KGS_0_FCF /local/yongguo/hadoop.tmp.yongguo/KGS_0_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for KGS_0_FCF ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/KGS_0_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_0_FCF 239044 u KGS_0_FCF $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_0_FCF /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_0_FCF
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/KGS_0_FCF
echo "--- KGS_0_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy KGS_1_FCF ---"
./client.sh dfs -D dfs.block.size=21993472 -copyFromLocal /var/scratch/yongguo/dataset/KGS_1_FCF /local/yongguo/hadoop.tmp.yongguo/KGS_1_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for KGS_1_FCF ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/KGS_1_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF 88814 u KGS_1_FCF $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/KGS_1_FCF
echo "--- KGS_1_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy KGS_2_FCF ---"
./client.sh dfs -D dfs.block.size=422912 -copyFromLocal /var/scratch/yongguo/dataset/KGS_2_FCF /local/yongguo/hadoop.tmp.yongguo/KGS_2_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for KGS_2_FCF ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/KGS_2_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_2_FCF 229713 u KGS_2_FCF $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_2_FCF /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_2_FCF
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/KGS_2_FCF
echo "--- KGS_2_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy bbo_edgetonode ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/bbo_edgetonode /local/yongguo/hadoop.tmp.yongguo/bbo_edgetonode

for i in 1 2 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for bbo_edgetonode ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/bbo_edgetonode hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_bbo_edgetonode 0 u bbo_edgetonode $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_bbo_edgetonode /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_bbo_edgetonode
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/bbo_edgetonode
echo "--- bbo_edgetonode DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy dotaleague_edgetonode ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/dotaleague_edgetonode /local/yongguo/hadoop.tmp.yongguo/dotaleague_edgetonode

for i in 1 2 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for dotaleague_edgetonode ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/dotaleague_edgetonode hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_dotaleague_edgetonode 0 u dotaleague_edgetonode $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotaleague_edgetonode /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotaleague_edgetonode
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/dotaleague_edgetonode
echo "--- dotaleague_edgetonode DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy dotalicious_edgetonode ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/dotalicious_edgetonode /local/yongguo/hadoop.tmp.yongguo/dotalicious_edgetonode

for i in 1 2 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for dotalicious_edgetonode ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/dotalicious_edgetonode hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_dotalicious_edgetonode 0 u dotalicious_edgetonode $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotalicious_edgetonode /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotalicious_edgetonode
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/dotalicious_edgetonode
echo "--- dotaleague_edgetonode DONE ---"

# --------------------------------------------------------------------------------------------













