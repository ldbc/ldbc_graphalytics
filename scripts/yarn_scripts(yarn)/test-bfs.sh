#!/bin/bash

#ich dataset is copied in a way to have exactly 10 blocks, thus fully utilize 20 wrokers (10mappers n 10reducers) #
############################################################################################

echo "--- Copy WikiVote_FCF ---"
#./client.sh dfs -copyFromLocal /var/scratch/mbiczak/input/filtered/WikiVote_FCF /local/hadoop.tmp.mbiczak
./client.sh fs -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /DotaLeague_FCF
mkdir -p /var/scratch/yongguo/output/bfs/output_wiki.vote

for i in 1 # 2 3 4 5 6 7 8 9 10
do
    echo "--- Run $i Stats for WikiVote_FCF ---"
    ./client.sh jar /home/yongguo/hadoopJobs.jar org.hadoop.test.jobs.BFSJob undirected filtered false false 20 20 /DotaLeague_FCF /output_$i\_kgs43333 0  #8251
    echo "--- Copy output ---"
    #./client.sh dfs -copyToLocal /local/hadoop.tmp.mbiczak/output_$i\_wiki.vote /var/scratch/mbiczak/output/bfs/output_wiki.vote
    echo "--- Clear dfs ---"
    #./client.sh dfs -rmr /local/hadoop.tmp.mbiczak/output_$i\_wiki.vote
done

#./client.sh dfs -rm /local/hadoop.tmp.mbiczak/WikiVote_FCF
echo "--- WikiVote_FCF DONE ---"
