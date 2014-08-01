#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}' | head -1`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}' | head -1`
echo $nodename

mkdir -p /var/scratch/yongguo/output/bfs




echo "--- Copy DotaLeague_FCF ---"
./client.sh dfs -D dfs.block.size=34316288 -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /local/hadoop.tmp.yongguo/DotaLeague_FCF

for i in 1 #2 3 4 5 #6 7 8 9 10
do
	echo "--- Run $i Stats for DotaLeague_FCF ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/DotaLeague_FCF hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF 0 u DotaLeague_FCF $i
	echo "--- Copy output ---"
	./client.sh dfs -copyToLocal /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
#	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output_$i\_DotaLeague_FCF
done
#./client.sh dfs -rm /local/hadoop.tmp.yongguo/DotaLeague_FCF
echo "--- DotaLeague_FCF DONE ---"

# --------------------------------------------------------------------------------------------
