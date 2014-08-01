#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
echo $nodename
mkdir -p /home/yongguo/glab_scripts/glab_bfs_mp


./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output
./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/input
#-------------------------------------------------------------------------------------

echo "--- Copy DotaLeague_FCF ---"

let "linenum=62000/($1*$2)"
echo $linenum
split -l $linenum /var/scratch/yongguo/sc_dataset/DotaLeague_FCF /var/scratch/yongguo/sc_dataset/dota_part_
./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/input/dota
./client.sh dfs -D dfs.block.size=34316288 -copyFromLocal /var/scratch/yongguo/sc_dataset/dota_part_* /local/hadoop.tmp.yongguo/input/dota

for i in 1 2 3 4 5 #6 7 8 9 10
do
        ./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output/DotaLeague_FCF_output
        echo "--- Run $i BFS for DotaLeague_FCF ---"
	mpiexec -n $1 -f machines  env CLASSPATH=`/home/yongguo/hadoop-0.20.203.0/bin/hadoop classpath` ./../graphlabapi/release/apps/bfs_multi --graph=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/input/dota --saveprefix=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output/DotaLeague_FCF_output/out_  --rootvertex=0 --ncpus=$2 --direct=u > /home/yongguo/glab_scripts/glab_bfs_mp/full_DotaLeague_FCF_$1_$2_$i
        tail -2 /home/yongguo/glab_scripts/glab_bfs_mp/full_DotaLeague_FCF_$1_$2_$i | head -1 | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs_mp/exe_DotaLeague_FCF_$1_$2_$i 
        tail -1 /home/yongguo/glab_scripts/glab_bfs_mp/full_DotaLeague_FCF_$1_$2_$i | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs_mp/DotaLeague_FCF_$1_$2_$i 
        echo "--- Clear dfs ---"
    	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output/DotaLeague_FCF_output
done

./client.sh dfs -rmr /local/hadoop.tmp.yongguo/input/dota
rm -rf /var/scratch/yongguo/sc_dataset/dota_part_*
echo "--- DotaLeague_FCF DONE ---"


#-------------------------------------------------------------------------------------

echo "--- Copy Friendster_FCF ---"

let "linenum=65610000/($1*$2)"
echo $linenum
split -l $linenum /var/scratch/yongguo/sc_dataset/Friendster_FCF /var/scratch/yongguo/sc_dataset/fri_part_
./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/input/fri
./client.sh dfs -D dfs.block.size=1647956992 -copyFromLocal /var/scratch/yongguo/sc_dataset/fri_part_* /local/hadoop.tmp.yongguo/input/fri

for i in 1 2 3 4 5 # 6 7 8 9 10
do
        ./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output/Friendster_FCF_output
        echo "--- Run $i BFS for Friendster_FCF ---"
	mpiexec -n $1 -f machines  env CLASSPATH=`/home/yongguo/hadoop-0.20.203.0/bin/hadoop classpath` ./../graphlabapi/release/apps/bfs_multi --graph=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/input/fri --saveprefix=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output/Friendster_FCF_output/out_  --rootvertex=71768986 --ncpus=$2 --direct=u > /home/yongguo/glab_scripts/glab_bfs_mp/full_Friendster_FCF_$1_$2_$i
        tail -2 /home/yongguo/glab_scripts/glab_bfs_mp/full_Friendster_FCF_$1_$2_$i | head -1 | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs_mp/exe_Friendster_FCF_$1_$2_$i 
        tail -1 /home/yongguo/glab_scripts/glab_bfs_mp/full_Friendster_FCF_$1_$2_$i | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs_mp/Friendster_FCF_$1_$2_$i 
        echo "--- Clear dfs ---"
    	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output/Friendster_FCF_output
done

./client.sh dfs -rmr /local/hadoop.tmp.yongguo/input/fri
rm -rf /var/scratch/yongguo/sc_dataset/fri_part_*
echo "--- Friendster_FCF DONE ---"


#-------------------------------------------------------------------------------------

