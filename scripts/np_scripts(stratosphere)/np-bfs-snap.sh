#!bin/bashnodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
echo $nodename
mkdir -p /var/scratch/yongguo/output/bfs

# --------------------------------------------------------------------------------------------

echo "--- Copy WikiVote_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/WikiVote_FCF /local/yongguo/hadoop.tmp.yongguo/WikiVote_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
        echo "--- Run $i Stats for WikiVote_FCF ---"
        bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/WikiVote_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiVote_FCF 8251 d WikiVote_FCF $i
        echo "--- Copy output ---"
        #./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiVote_FCF /var/scratch/yongguo/output/bfs/
        echo "--- Clear dfs ---"
        ./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiVote_FCF
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/WikiVote_FCF
echo "--- WikiVote_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy WikiTalk_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/WikiTalk_FCF /local/yongguo/hadoop.tmp.yongguo/WikiTalk_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
        echo "--- Run $i Stats for WikiTalk_FCF ---"
        bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/WikiTalk_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF 2249533 d WikiTalk_FCF $i
        echo "--- Copy output ---"
        #./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF /var/scratch/yongguo/output/bfs/
        echo "--- Clear dfs ---"
        ./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/WikiTalk_FCF
echo "--- WikiTalk_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy WebGraph_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/WebGraph_FCF /local/yongguo/hadoop.tmp.yongguo/WebGraph_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
        echo "--- Run $i Stats for WebGraph_FCF ---"
        bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/WebGraph_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_WebGraph_FCF 1 d WebGraph_FCF $i
        echo "--- Copy output ---"
        #./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_WebGraph_FCF /var/scratch/yongguo/output/bfs/
        echo "--- Clear dfs ---"
        ./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_WebGraph_FCF
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/WebGraph_FCF
echo "--- WebGraph_FCF DONE ---"

# --------------------------------------------------------------------------------------------


# --------------------------------------------------------------------------------------------

echo "--- Copy EUEmail_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/EUEmail_FCF /local/yongguo/hadoop.tmp.yongguo/EUEmail_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
        echo "--- Run $i Stats for EUEmail_FCF ---"
        bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/EUEmail_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_EUEmail_FCF 74203 d EUEmail_FCF $i
        echo "--- Copy output ---"
        #./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_EUEmail_FCF /var/scratch/yongguo/output/bfs/
        echo "--- Clear dfs ---"
        ./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_EUEmail_FCF
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/EUEmail_FCF
echo "--- EUEmail_FCF DONE ---"

# --------------------------------------------------------------------------------------------




# --------------------------------------------------------------------------------------------

echo "--- Copy Citation_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/Citation_FCF /local/yongguo/hadoop.tmp.yongguo/Citation_FCF

for i in 1 2 3 4 5 6 7 8 9 10
do
        echo "--- Run $i Stats for Citation_FCF ---"
        bash bfs.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.BreadthFirstSearch 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/Citation_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_Citation_FCF 4949326 d Citation_FCF $i
        echo "--- Copy output ---"
        #./client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_Citation_FCF /var/scratch/yongguo/output/bfs/
        echo "--- Clear dfs ---"
        ./client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_Citation_FCF
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/Citation_FCF
echo "--- Citation_FCF DONE ---"

# --------------------------------------------------------------------------------------------







