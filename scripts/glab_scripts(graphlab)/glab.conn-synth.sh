#!bin/bash

nodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
let "nodesNr -= 8"
index=9
nodename=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
echo $nodename
mkdir -p /home/yongguo/glab_scripts/glab_conn


./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output




echo "--- Copy graph500_FCF ---"
./client.sh dfs -D dfs.block.size=50535424 -copyFromLocal /var/scratch/yongguo/sc_dataset/graph500_FCF /local/hadoop.tmp.yongguo/graph500_FCF

for i in 1 2 3 4 5  6 7 8 9 10
do
        ./client.sh dfs -mkdir -p /local/hadoop.tmp.yongguo/output/graph500_FCF_output
        echo "--- Run $i BFS for graph500_FCF ---"
	mpiexec -n $1 -f machines  env CLASSPATH=`/home/yongguo/hadoop-0.20.203.0/bin/hadoop classpath` ./../graphlabapi/release/apps/conn_multi --graph=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/graph500_FCF --saveprefix=hdfs://$nodename.cm.cluster:54310/local/hadoop.tmp.yongguo/output/graph500_FCF_output/out_   --ncpus=$2 --direct=u > /home/yongguo/glab_scripts/glab_conn/full_graph500_FCF_$1_$2_$i
        tail -2 /home/yongguo/glab_scripts/glab_conn/full_graph500_FCF_$1_$2_$i | head -1 | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_conn/exe_graph500_FCF_$1_$2_$i 
        tail -1 /home/yongguo/glab_scripts/glab_conn/full_graph500_FCF_$1_$2_$i | awk '{print $NF}' > /home/yongguo/glab_scripts/glab_conn/graph500_FCF_$1_$2_$i 
        echo "--- Clear dfs ---"
    	./client.sh dfs -rmr /local/hadoop.tmp.yongguo/output/graph500_FCF_output
done

./client.sh dfs -rm /local/hadoop.tmp.yongguo/graph500_FCF
echo "--- graph500_FCF DONE ---"


#-------------------------------------------------------------------------------------
./stop-glab-cluster.sh
