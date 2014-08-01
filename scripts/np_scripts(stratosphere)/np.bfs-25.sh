#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}' | sed -n '2,2p'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}' | sed -n '2,2p'`
echo $nodename

mkdir -p /var/scratch/yongguo/output/bfs


echo "--- Copy DotaLeague_FCF ---"
./client.sh dfs -D dfs.block.size=27458048 -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /local/hadoop.tmp.yongguo/DotaLeague_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for DotaLeague_FCF ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 25 hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/DotaLeague_FCF hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF 0 u DotaLeague_FCF_25 $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF
done
./client.sh dfs -rm /local/hadoop.tmp.yongguo/DotaLeague_FCF
echo "--- DotaLeague_FCF DONE ---"

# --------------------------------------------------------------------------------------------



echo "--- Copy Friendster_FCF ---"
./client.sh dfs -D dfs.block.size=1318358016 -copyFromLocal /var/scratch/yongguo/sc_dataset/Friendster_FCF /local/hadoop.tmp.yongguo/Friendster_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
	echo "--- Run $i Stats for Friendster_FCF ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 25 hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/Friendster_FCF hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF 71768986 u Friendster_FCF_25 $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_Friendster_FCF
done
./client.sh dfs -rm /local/hadoop.tmp.yongguo/Friendster_FCF
echo "--- Friendster_FCF DONE ---"

# --------------------------------------------------------------------------------------------

