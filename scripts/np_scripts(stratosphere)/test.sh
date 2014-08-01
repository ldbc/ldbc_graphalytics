#a=$1
#if [ "${a:5:1}" = "0" ]
#  then
#    echo "yes, it is 0"
#  else
#    echo "no, it is not 0"
#fi

#. ./settings.sh

#ssh $USER@10.141.0.10 'bash -s' < np-mktempdir.sh $NP_TEMP_DIR
#echo "yes"

#it=10
#i=1
#while [ $i -le $it ]
#do
#        echo "--- Run $i Stats for KGS_0_FCF ---"
#	((i+=1))
#done
#echo "first done"
#i=1
#while [ $i -le $it ]
#do
#        echo "--- Run $i Stats for KGS_1_FCF ---"
#	((i+=1))
#done

#lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_1_KGS_0_FCF | wc -l`
#index=2
#lastfileNum=`bash client.sh dfs -ls /local/yongguo/hadoop.tmp.yongguo/output_6_KGS_0_FCF| grep "Found" | awk -v col=$index '{print $col}'`
#((lastfileNum=lastfileNum-1))
#echo $lastfileNum
#! /bin/bash  
#filename: test.sh  
  
  
# arg1=start, arg2=end, format: %s.%N  
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
  
  
    echo "$time ms"  
}  
  
  
  
  
echo "This is only a test to get a ms level time duration..."  
start=$(date +%s.%N)  
#ls >& /dev/null    # hey, be quite, do not output to console....  
sleep 2
end=$(date +%s.%N)  
  
  
getTiming $start $end 
