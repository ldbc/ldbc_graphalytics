#!/bin/bash

############################################################################################
# each dataset is copied in a way to have exactly 20 blocks, thus fully utilize 20 wrokers (20mappers n 20reducers) #
############################################################################################

function getTiming() {
    start=$1
    end=$2

    start_s=$(echo $start | cut -d '.' -f 1)
    start_ns=$(echo $start | cut -d '.' -f 2)
    end_s=$(echo $end | cut -d '.' -f 1)
    end_ns=$(echo $end | cut -d '.' -f 2)


# for debug..  
#    echo $start  
#    echo $end  


    time=$(( ( 10#$end_s - 10#$start_s ) * 1000 + ( 10#$end_ns / 1000000 - 10#$start_ns / 1000000 ) ))


    echo "$time"  > /home/yongguo/yarn_scripts/injestion_time/$3
}


./client.sh fs -mkdir -p /local/hadoop.tmp.yongguo

echo "--- Copy graph500_FCF ---"

for i in 1 2 3 4 5 6 7 8 9 10
do
	
    echo "--- Run $i Stats for graph500_FCF ---"
    start=$(date +%s.%N)
    ./client.sh dfs -D dfs.block.size=50535424 -copyFromLocal /var/scratch/yongguo/sc_dataset/graph500_FCF /local/hadoop.tmp.yongguo/graph500_FCF
    end=$(date +%s.%N)
    getTiming $start $end graph500_FCF_$i
    echo "--- Clear dfs ---"
    ./client.sh dfs -rm /local/hadoop.tmp.yongguo/graph500_FCF
    sleep 20
done

echo "--- graph500_FCF DONE ---"

# --------------------------------------------------------------------------------------------
