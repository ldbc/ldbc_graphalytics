#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
echo $nodename

# define the iterations of each dataset
iterationMax=10

# make the directory for the output
mkdir -p /var/scratch/yongguo/output/conn

# --------------------------------------------------------------------------------------------

echo "--- Copy KGS_0_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/KGS_0_FCF /local/yongguo/hadoop.tmp.yongguo/KGS_0_FCF

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for KGS_0_FCF ---"
        bash conn.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.ConnectedComponent 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/KGS_0_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_0_FCF u KGS_0_FCF $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_0_FCF| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
       #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_0_FCF/$lastfileNum /var/scratch/yongguo/output/conn/output_$i\_KGS_0_FCF
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_0_FCF
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/KGS_0_FCF
echo "--- KGS_0_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy KGS_1_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/KGS_1_FCF /local/yongguo/hadoop.tmp.yongguo/KGS_1_FCF

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for KGS_1_FCF ---"
        bash conn.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.ConnectedComponent 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/KGS_1_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF u KGS_1_FCF $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
        #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF/$lastfileNum /var/scratch/yongguo/output/conn/output_$i\_KGS_1_FCF
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_1_FCF
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/KGS_1_FCF
echo "--- KGS_1_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy KGS_2_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/KGS_2_FCF /local/yongguo/hadoop.tmp.yongguo/KGS_2_FCF

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for KGS_2_FCF ---"
        bash conn.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.ConnectedComponent 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/KGS_2_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_2_FCF u KGS_2_FCF $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_2_FCF| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
       #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_2_FCF/$lastfileNum /var/scratch/yongguo/output/conn/output_$i\_KGS_2_FCF
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_KGS_2_FCF
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/KGS_2_FCF
echo "--- KGS_2_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy bbo_edgetonode ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/bbo_edgetonode /local/yongguo/hadoop.tmp.yongguo/bbo_edgetonode

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for bbo_edgetonode ---"
        bash conn.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.ConnectedComponent 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/bbo_edgetonode hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_bbo_edgetonode u bbo_edgetonode $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_bbo_edgetonode| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
       #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_bbo_edgetonode/$lastfileNum /var/scratch/yongguo/output/conn/output_$i\_bbo_edgetonode
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_bbo_edgetonode
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/bbo_edgetonode
echo "--- bbo_edgetonode DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy dotaleague_edgetonode ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/dotaleague_edgetonode /local/yongguo/hadoop.tmp.yongguo/dotaleague_edgetonode

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for dotaleague_edgetonode ---"
        bash conn.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.ConnectedComponent 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/dotaleague_edgetonode hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_dotaleague_edgetonode u dotaleague_edgetonode $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotaleague_edgetonode| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
       #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotaleague_edgetonode/$lastfileNum /var/scratch/yongguo/output/conn/output_$i\_dotaleague_edgetonode
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotaleague_edgetonode
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/dotaleague_edgetonode
echo "--- dotaleague_edgetonode DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy dotalicious_edgetonode ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/dotalicious_edgetonode /local/yongguo/hadoop.tmp.yongguo/dotalicious_edgetonode

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for dotalicious_edgetonode ---"
        bash conn.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.ConnectedComponent 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/dotalicious_edgetonode hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_dotalicious_edgetonode u dotalicious_edgetonode $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotalicious_edgetonode| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
       #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotalicious_edgetonode/$lastfileNum /var/scratch/yongguo/output/conn/output_$i\_dotalicious_edgetonode
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_dotalicious_edgetonode
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/dotalicious_edgetonode
echo "--- dotalicious_edgetonode DONE ---"

# --------------------------------------------------------------------------------------------
