#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
echo $nodename
mkdir -p /home/yongguo/glab_scripts/glab_bfs


./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output

echo "--- Copy Citation_FCF ---"
./client.sh dfs -D dfs.block.size=15769600 -copyFromLocal /var/scratch/yongguo/sc_dataset/Citation_FCF /local/hadoop.tmp.yongguo/Citation_FCF

for i in 1 #2 3 4 5 6 7 8 9 10
do
        ./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output/Citation_FCF_output
        echo "--- Run $i BFS for Citation_FCF ---"
	mpiexec -n $1 -f machines  env CLASSPATH=`/home/yongguo/hadoop-0.20.203.0/bin/hadoop classpath` ./../graphlabapi/build/apps/bfs_multi --graph=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/Citation_FCF --saveprefix=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output/Citation_FCF_output/out_  --rootvertex=4949326 --ncpus=$2 --direct=d > /home/yongguo/glab_scripts/glab_bfs/full_Citation_FCF_$1_$2_$i
        tail -2 /home/yongguo/glab_scripts/glab_bfs/full_Citation_FCF_$1_$2_$i | head -1 | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/exe_Citation_FCF_$1_$2_$i 
        tail -1 /home/yongguo/glab_scripts/glab_bfs/full_Citation_FCF_$1_$2_$i | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/Citation_FCF_$1_$2_$i 
        echo "--- Clear dfs ---"
    	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output/Citation_FCF_output
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/Citation_FCF
echo "--- Citation_FCF DONE ---"


#-------------------------------------------------------------------------------------
