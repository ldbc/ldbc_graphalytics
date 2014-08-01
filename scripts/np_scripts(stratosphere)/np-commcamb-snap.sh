#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
echo $nodename

# define the iterations of each dataset
iterationMax=10

# make the directory for the output
mkdir -p /var/scratch/yongguo/output/commcamb

# --------------------------------------------------------------------------------------------

echo "--- Copy WikiVote_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/WikiVote_FCF /local/yongguo/hadoop.tmp.yongguo/WikiVote_FCF

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for WikiVote_FCF ---"
        bash commcamb.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.CommunityDetectionCamb 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/WikiVote_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiVote_FCF d WikiVote_FCF $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiVote_FCF| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
       #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiVote_FCF/$lastfileNum /var/scratch/yongguo/output/commcamb/output_$i\_WikiVote_FCF
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiVote_FCF
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/WikiVote_FCF
echo "--- WikiVote_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy WikiTalk ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/WikiTalk /local/yongguo/hadoop.tmp.yongguo/WikiTalk

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for WikiTalk ---"
        bash commcamb.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.CommunityDetectionCamb 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/WikiTalk hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk d WikiTalk $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
        #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk/$lastfileNum /var/scratch/yongguo/output/commcamb/output_$i\_WikiTalk
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_WikiTalk
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/WikiTalk
echo "--- WikiTalk DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy WebGraph_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/WebGraph_FCF /local/yongguo/hadoop.tmp.yongguo/WebGraph_FCF

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for WebGraph_FCF ---"
        bash commcamb.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.CommunityDetectionCamb 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/WebGraph_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_WebGraph_FCF d WebGraph_FCF $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_WebGraph_FCF| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
       #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_WebGraph_FCF/$lastfileNum /var/scratch/yongguo/output/commcamb/output_$i\_WebGraph_FCF
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_WebGraph_FCF
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/WebGraph_FCF
echo "--- WebGraph_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy EUEmail_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/EUEmail_FCF /local/yongguo/hadoop.tmp.yongguo/EUEmail_FCF

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for EUEmail_FCF ---"
        bash commcamb.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.CommunityDetectionCamb 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/EUEmail_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_EUEmail_FCF d  EUEmail_FCF $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_EUEmail_FCF| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
       #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_EUEmail_FCF/$lastfileNum /var/scratch/yongguo/output/commcamb/output_$i\_EUEmail_FCF
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_EUEmail_FCF
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/EUEmail_FCF
echo "--- EUEmail_FCF DONE ---"

# --------------------------------------------------------------------------------------------

# --------------------------------------------------------------------------------------------

echo "--- Copy Citation_FCF ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/Citation_FCF /local/yongguo/hadoop.tmp.yongguo/Citation_FCF

# i is the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
        echo "--- Run $i Stats for Citation_FCF ---"
        bash commcamb.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.CommunityDetectionCamb 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/Citation_FCF hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_Citation_FCF d Citation_FCF $i
        itemIndex=2
        lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_Citation_FCF| grep "Found" | awk -v col=$itemIndex '{print $col}'`    
        echo "--- Copy output ---"
       #bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_Citation_FCF/$lastfileNum /var/scratch/yongguo/output/commcamb/output_$i\_Citation_FCF
        echo "--- Clear dfs ---"
        bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_Citation_FCF
        ((i+=1))
done
./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/Citation_FCF
echo "--- Citation_FCF DONE ---"

# --------------------------------------------------------------------------------------------
