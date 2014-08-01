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


    echo "$time"  > /home/yongguo/jobstatus/commcamb_check/$3
}


filename="$HOME"/done/commcamb/tobeprocess
jobdir="$HOME"/jobstatus/commcamb
jobstatus=$jobdir/$7_$8
mkdir -p /home/yongguo/jobstatus/commcamb_check
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
./np-client.sh run -w -j $1 -c $2 -a $3 $4 $5/1 1 $6 >> $jobstatus

iterationCount=1
notFirst=2

while [ -e $filename ]  && [ $iterationCount -lt 5 ]
do 
        rm -rf $filename
        input="$5/$iterationCount"
        ((iterationCount=iterationCount+1))
        output="$5/$iterationCount"
        echo "file 2" $output
        ./client.sh fs -mkdir $output
        echo "start second" $iterationCount
        ./np-client.sh run -w -j $1 -c $2 -a $3 $input $output $notFirst $6 >> $jobstatus
	#tobecollect="$5/$iterationCount"
	#conncollect="$5/conn_$iterationCount"
	#echo $tobecollect
	#echo $conncollect
	#./np-client.sh run -w -j $1 -c $9 -a $3 $tobecollect $conncollect >> $jobstatus
done
end=$(date +%s.%N)

getTiming $start $end $7_$8

echo "iteration num: " $iterationCount



#cat /home/yg/Desktop/stratosphere/results/commcamb/Karate/Karate_conn >> /home/yg/Desktop/stratosphere/results/commcamb/Karate_results
#echo "-------------------------------------------------" >> /home/yg/Desktop/stratosphere/results/commcamb/Karate_results

#rm -r /home/yg/Desktop/stratosphere/results/commcamb/Karate


