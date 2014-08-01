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

echo "--- Copy WikiTalk_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/WikiTalk_FCF /local/yongguo/hadoop.tmp.yongguo/WikiTalk_FCF

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for WikiTalk_FCF ---"
        bash conn.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.ConnectedComponent 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/WikiTalk_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF d WikiTalk_FCF $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
        #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF/$lastfileNum /var/scratch/yongguo/output/conn/output_$i\_WikiTalk_FCF
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk_FCF
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/WikiTalk_FCF
echo "--- WikiTalk_FCF DONE ---"

# --------------------------------------------------------------------------------------------
