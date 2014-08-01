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

echo "--- Copy u_n_36 ---"
./client.sh dfs -copyFromLocal /var/scratch/yongguo/dataset/u_n_36 /local/yongguo/hadoop.tmp.yongguo/u_n_36

# i the the counter of iteration
i=1
while [ $i -le $iterationMax ]
do
	echo "--- Run $i Stats for u_n_36 ---"
	bash commcamb.sh ../biggraph.jar eu.stratosphere.pact.example.biggraph.CommunityDetectionCamb 20 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/u_n_36 hdfs://$nodename.cm.cluster:54310/local/yongguo/hadoop.tmp.yongguo/output_$i\_u_n_36 u u_n_36 $i eu.stratosphere.pact.example.biggraph.ConnCollect
        #itemIndex=2
	#lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_$i\_u_n_36| grep "Found" | awk -v col=$itemIndex '{print $col}'`	
	echo "--- Copy output ---"
	bash client.sh dfs -copyToLocal /local/yongguo/hadoop.tmp.yongguo/output_$i\_u_n_36 /var/scratch/yongguo/output/commcamb/output_$i\_u_n_36
	echo "--- Clear dfs ---"
	#bash client.sh dfs -rmr /local/yongguo/hadoop.tmp.yongguo/output_$i\_u_n_36
	((i+=1))
done
#./client.sh dfs -rm /local/yongguo/hadoop.tmp.yongguo/u_n_36
echo "--- u_n_36 DONE ---"

# --------------------------------------------------------------------------------------------

