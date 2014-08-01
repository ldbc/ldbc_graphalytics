#!/bin/bash


#ch dataset is copied in a way to have exactly 20 blocks, thus fully utilize 20 wrokers #
############################################################################################

echo "--- Copy WikiVote_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/mbiczak/input/filtered/WikiVote_FCF /local/hadoop.tmp.mbiczak
./client.sh dfs -D dfs.block.size=53760 -copyFromLocal /var/scratch/mbiczak/input/filtered/WikiVote_FCF /local/hadoop.tmp.mbiczak/WikiVote_FCF
mkdir -p /var/scratch/yongguo/output/giraph.ConnComp/output_wiki.vote

for i in 1 # 2 3 4 5 6 7 8 9 10
do  
    echo "--- Run $i Stats for WikiVote_FCF ---"
    ./client.sh jar /home/yongguo/exeLibs/giraphJobs.jar org.test.giraph.ConnectedComponentDriver directed /local/hadoop.tmp.mbiczak/WikiVote_FCF /local/hadoop.tmp.mbiczak/output_$i\_wiki.vote1 20
    echo "DEL RDUNDANT DATA"
    if [ $i -gt "1" ]
      then
	./client.sh dfs -rmr /local/hadoop.tmp.mbiczak/output_$i\_wiki.vote1/Max_ConnComp
	./client.sh dfs -rmr /local/hadoop.tmp.mbiczak/output_$i\_wiki.vote1/labeledConnComp
    fi
    echo "--- Copy output ---"
    ./client.sh dfs -copyToLocal /local/hadoop.tmp.mbiczak/output_$i\_wiki.vote1 /var/scratch/yongguo/output/giraph.ConnComp/output_wiki.vote
    echo "--- Clear dfs ---"
    #/client.sh dfs -rmr /local/hadoop.tmp.mbiczak/output_$i\_wiki.vote
done

#/client.sh dfs -rm /local/hadoop.tmp.mbiczak/WikiVote_FCF
echo "--- WikiVote_FCF DONE ---"

