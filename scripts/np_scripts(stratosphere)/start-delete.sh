. ./settings.sh

#delete hadoop.tmp.${USER}
Master=`cat $HADOOP_MASTERS`
ssh $USER@$Master 'rm -rf /local/${USER}/hadoop.tmp.${USER}'
ssh $USER@$Master 'rm -rf /local/${USER}/np.tmp.${USER}'

cp $HADOOP_SLAVES $HADOOP_CONF/slaves2
for (( i=1; i<=$1; i++ ))
do
        Slave=`cat $HADOOP_CONF/slaves2 | head -1` #sed -n '$i,$i p'`
	sed -i '1d' $HADOOP_CONF/slaves2
        echo $Slave
	ssh $USER@$Slave 'rm -rf /local/${USER}/hadoop.tmp.${USER}'
	ssh $USER@$Slave 'rm -rf /local/${USER}/np.tmp.${USER}'
done

