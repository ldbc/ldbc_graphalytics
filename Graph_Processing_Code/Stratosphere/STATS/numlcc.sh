#!bin/bash

# driver code for running STATS

function getTiming() {
    start=$1
    end=$2

    start_s=$(echo $start | cut -d '.' -f 1)
    start_ns=$(echo $start | cut -d '.' -f 2)
    end_s=$(echo $end | cut -d '.' -f 1)
    end_ns=$(echo $end | cut -d '.' -f 2)

    time=$(( ( 10#$end_s - 10#$start_s ) * 1000 + ( 10#$end_ns / 1000000 - 10#$start_ns / 1000000 ) ))


    echo "$time"  > /home/yongguo/jobstatus/numlcc_check/$3
}


filename="$HOME"/done/numlcc/tobeprocess
jobdir="$HOME"/jobstatus/numlcc
jobstatus=$jobdir/$7_$8
mkdir -p /home/yongguo/jobstatus/numlcc_check

if [ ! -e $jobdir ]
then
        mkdir -p $jobdir
fi

if [ ! -e $jobstatus ]
then
        touch $jobstatus
fi

if [ -e $filename ]
then
        rm -rf $filename
fi

cat /dev/null > $jobstatus
echo "filename" $5/1
./client.sh fs -mkdir $5/1
start=$(date +%s.%N)
echo "start********"

./np-client.sh run -w -j $1 -c $2 -a $3 $4 $5/1 $6  >> $jobstatus

end=$(date +%s.%N)

getTiming $start $end $7_$8

