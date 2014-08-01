#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
echo $nodename
mkdir -p /home/yongguo/glab_scripts/glab_bfs


./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output

echo "--- Copy amazon.302_FCF ---"
./client.sh dfs -D dfs.block.size=939520 -copyFromLocal /var/scratch/yongguo/sc_dataset/amazon.302_FCF /local/hadoop.tmp.yongguo/amazon.302_FCF

for i in 1 2 3 4 5 #6 7 8 9 10
do
        ./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output/amazon.302_FCF_output
        echo "--- Run $i BFS for amazon.302_FCF ---"
	mpiexec -n $1 -f machines  env CLASSPATH=`/home/yongguo/hadoop-0.20.203.0/bin/hadoop classpath` ./../graphlabapi/release/apps/bfs_multi --graph=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/amazon.302_FCF --saveprefix=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output/amazon.302_FCF_output/out_  --rootvertex=99843 --ncpus=$2 --direct=d > /home/yongguo/glab_scripts/glab_bfs/full_amazon.302_FCF_$1_$2_$i
        tail -2 /home/yongguo/glab_scripts/glab_bfs/full_amazon.302_FCF_$1_$2_$i | head -1 | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/exe_amazon.302_FCF_$1_$2_$i 
        tail -1 /home/yongguo/glab_scripts/glab_bfs/full_amazon.302_FCF_$1_$2_$i | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/amazon.302_FCF_$1_$2_$i 
        echo "--- Clear dfs ---"
    	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output/amazon.302_FCF_output
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/amazon.302_FCF
echo "--- amazon.302_FCF DONE ---"


#-------------------------------------------------------------------------------------


echo "--- Copy Citation_FCF ---"
./client.sh dfs -D dfs.block.size=15769600 -copyFromLocal /var/scratch/yongguo/sc_dataset/Citation_FCF /local/hadoop.tmp.yongguo/Citation_FCF

for i in 1 2 3 4 5 #6 7 8 9 10
do
        ./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output/Citation_FCF_output
        echo "--- Run $i BFS for Citation_FCF ---"
        mpiexec -n $1 -f machines  env CLASSPATH=`/home/yongguo/hadoop-0.20.203.0/bin/hadoop classpath` ./../graphlabapi/release/apps/bfs_multi --graph=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/Citation_FCF --saveprefix=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output/Citation_FCF_output/out_  --rootvertex=4949326 --ncpus=$2 --direct=d > /home/yongguo/glab_scripts/glab_bfs/full_Citation_FCF_$1_$2_$i
        tail -2 /home/yongguo/glab_scripts/glab_bfs/full_Citation_FCF_$1_$2_$i | head -1 | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/exe_Citation_FCF_$1_$2_$i
        tail -1 /home/yongguo/glab_scripts/glab_bfs/full_Citation_FCF_$1_$2_$i | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/Citation_FCF_$1_$2_$i
        echo "--- Clear dfs ---"
        ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output/Citation_FCF_output
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/Citation_FCF
echo "--- Citation_FCF DONE ---"


#-------------------------------------------------------------------------------------

echo "--- Copy WikiTalk_FCF ---"
./client.sh dfs -D dfs.block.size=4561920 -copyFromLocal /var/scratch/yongguo/sc_dataset/WikiTalk_FCF /local/hadoop.tmp.yongguo/WikiTalk_FCF
for i in 1 2 3 4 5 #6 7 8 9 10
do
        ./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output/WikiTalk_FCF_output
        echo "--- Run $i BFS for WikiTalk_FCF ---"
        mpiexec -n $1 -f machines  env CLASSPATH=`/home/yongguo/hadoop-0.20.203.0/bin/hadoop classpath` ./../graphlabapi/release/apps/bfs_multi --graph=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/WikiTalk_FCF --saveprefix=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output/WikiTalk_FCF_output/out_  --rootvertex=14591 --ncpus=$2 --direct=d > /home/yongguo/glab_scripts/glab_bfs/full_WikiTalk_FCF_$1_$2_$i
        tail -2 /home/yongguo/glab_scripts/glab_bfs/full_WikiTalk_FCF_$1_$2_$i | head -1 | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/exe_WikiTalk_FCF_$1_$2_$i
        tail -1 /home/yongguo/glab_scripts/glab_bfs/full_WikiTalk_FCF_$1_$2_$i | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/WikiTalk_FCF_$1_$2_$i
        echo "--- Clear dfs ---"
        ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output/WikiTalk_FCF_output
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/WikiTalk_FCF
echo "--- WikiTalk_FCF DONE ---"


#-------------------------------------------------------------------------------------

echo "--- Copy KGS_1_FCF ---"
./client.sh dfs -D dfs.block.size=10998272 -copyFromLocal /var/scratch/yongguo/sc_dataset/KGS_1_FCF /local/hadoop.tmp.yongguo/KGS_1_FCF
for i in 1 2 3 4 5 #6 7 8 9 10
do
        ./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output/KGS_1_FCF_output
        echo "--- Run $i BFS for KGS_1_FCF ---"
        mpiexec -n $1 -f machines  env CLASSPATH=`/home/yongguo/hadoop-0.20.203.0/bin/hadoop classpath` ./../graphlabapi/release/apps/bfs_multi --graph=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/KGS_1_FCF --saveprefix=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output/KGS_1_FCF_output/out_  --rootvertex=88814 --ncpus=$2 --direct=u > /home/yongguo/glab_scripts/glab_bfs/full_KGS_1_FCF_$1_$2_$i
        tail -2 /home/yongguo/glab_scripts/glab_bfs/full_KGS_1_FCF_$1_$2_$i | head -1 | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/exe_KGS_1_FCF_$1_$2_$i
        tail -1 /home/yongguo/glab_scripts/glab_bfs/full_KGS_1_FCF_$1_$2_$i | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_bfs/KGS_1_FCF_$1_$2_$i
        echo "--- Clear dfs ---"
        ./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output/KGS_1_FCF_output
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/KGS_1_FCF
echo "--- KGS_1_FCF DONE ---"


#-------------------------------------------------------------------------------------
