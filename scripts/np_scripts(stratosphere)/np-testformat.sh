#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
echo $nodename

mkdir -p /var/scratch/yongguo/output/bfs
function getTiming() {
    start=$1
    end=$2

    start_s=$(echo $start | cut -d '.' -f 1)
    start_ns=$(echo $start | cut -d '.' -f 2)
    end_s=$(echo $end | cut -d '.' -f 1)
    end_ns=$(echo $end | cut -d '.' -f 2)


# for debug..  
#    echo $start  
#    echo $end  


    time=$(( ( 10#$end_s - 10#$start_s ) * 1000 + ( 10#$end_ns / 1000000 - 10#$start_ns / 1000000 ) ))


    echo "$time ms"  
}


# --------------------------------------------------------------------------------------------

echo "--- Copy dotaleague_edgetonode ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /local/yongguo/hadoop.tmp.yongguo/dotaleague_edgetonode

start=$(date +%s.%N)
echo $start
for i in 1 # 2 3 4 5 6 7 8 9 10
do
	
echo "--- Run $i Stats for dotaleague_edgetonode ---"
	bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/dotaleague_edgetonode hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_dotaleague_edgetonode 0 u dotaleague_edgetonode $i
	echo "--- Copy output ---"
	#./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotaleague_edgetonode /var/scratch/yongguo/output/bfs/
	echo "--- Clear dfs ---"
	#./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotaleague_edgetonode
done
#./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/dotaleague_edgetonode
echo "--- dotaleague_edgetonode DONE ---"
end=$(date +%s.%N)
echo $end
getTiming $start $end
# --------------------------------------------------------------------------------------------
