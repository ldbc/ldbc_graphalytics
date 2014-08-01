#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
echo $nodename
mkdir -p /home/yongguo/glab_scripts/glab_bfs


./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output

#-------------------------------------------------------------------------------------

echo "--- Copy DotaLeague_FCF ---"
./client.sh dfs -D dfs.block.size=34316288 -copyFromLocal /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /local/hadoop.tmp.yongguo/DotaLeague_FCF

for i in 1 2 3 4 5 #6 7 8 9 10
do
        ./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output/DotaLeague_FCF_output
        echo "--- Run $i BFS for DotaLeague_FCF ---"
	mpiexec -n $1 -f machines  env CLASSPATH=`/home/yongguo/hadoop-0.20.203.0/bin/hadoop classpath` ./../graphlabapi/release/apps/bfs_multi --graph=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/DotaLeague_FCF --saveprefix=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output/DotaLeague_FCF_output/out_  --rootvertex=0 --ncpus=$2 --direct=u > /home/yongguo/glab_scripts/glab_bfs/full_DotaLeague_FCF_$1_$2_$i
        tail -2 /home/yongguo/glab_scripts/glab_bfs/full_DotaLeague_FCF_$1_$2_$i | head -1 | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/exe_DotaLeague_FCF_$1_$2_$i 
        tail -1 /home/yongguo/glab_scripts/glab_bfs/full_DotaLeague_FCF_$1_$2_$i | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/DotaLeague_FCF_$1_$2_$i 
        echo "--- Clear dfs ---"
    	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output/DotaLeague_FCF_output
done

#./client.sh dfs -rm /local/hadoop.tmp.yongguo/DotaLeague_FCF
echo "--- DotaLeague_FCF DONE ---"


#-------------------------------------------------------------------------------------

echo "--- Copy Friendster_FCF ---"
./client.sh dfs -D dfs.block.size=1647956992 -copyFromLocal /var/scratch/yongguo/sc_dataset/Friendster_FCF /local/hadoop.tmp.yongguo/Friendster_FCF

for i in 1 2 3 #4 5 # 6 7 8 9 10
do
        ./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output/Friendster_FCF_output
        echo "--- Run $i BFS for Friendster_FCF ---"
	mpiexec -n $1 -f machines  env CLASSPATH=`/home/yongguo/hadoop-0.20.203.0/bin/hadoop classpath` ./../graphlabapi/release/apps/bfs_multi --graph=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/Friendster_FCF --saveprefix=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output/Friendster_FCF_output/out_  --rootvertex=71768986 --ncpus=$2 --direct=u > /home/yongguo/glab_scripts/glab_bfs/full_Friendster_FCF_$1_$2_$i
        tail -2 /home/yongguo/glab_scripts/glab_bfs/full_Friendster_FCF_$1_$2_$i | head -1 | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/exe_Friendster_FCF_$1_$2_$i 
        tail -1 /home/yongguo/glab_scripts/glab_bfs/full_Friendster_FCF_$1_$2_$i | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/Friendster_FCF_$1_$2_$i 
        echo "--- Clear dfs ---"
    	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output/Friendster_FCF_output
done

#./client.sh dfs -rm /local/hadoop.tmp.yongguo/Friendster_FCF
echo "--- Friendster_FCF DONE ---"


#-------------------------------------------------------------------------------------
./stop-glab-cluster.sh
