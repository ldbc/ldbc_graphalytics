#!bin/bash


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


    echo "$time"  > /home/yongguo/jobstatus/bfs_check/$3
}


filename="$HOME"/done/bfs/tobeprocess
jobdir="$HOME"/jobstatus/bfs
jobstatus=$jobdir/$8_$9

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

mkdir -p /home/yongguo/jobstatus/bfs_check
cat /dev/null > $jobstatus
echo "filename" $5/1
./client.sh fs -mkdir $5/1
start=$(date +%s.%N)
./np-client.sh run -w -j $1 -c $2 -a $3 $4 $5/1 $6 1 $7 >> $jobstatus
#mid=$(date +%s.%N)

#getTiming $start $mid $8_$9_mid




iterationCount=1
notFirst=2

while [ -e $filename ]
do
        rm -rf $filename
        input="$5/$iterationCount"
        ((iterationCount=iterationCount+1))
        output="$5/$iterationCount"
	echo "file 2" $output
        ./client.sh fs -mkdir $output
        echo "start second" $iterationCount
        ./np-client.sh run -w -j $1 -c $2 -a $3 $input $output $6 $notFirst $7 >> $jobstatus

done
end=$(date +%s.%N)

getTiming $start $end $8_$9

echo  "Depth: " $iterationCount
