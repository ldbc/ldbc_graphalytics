#!/bin/bash

##########################################################################################
# Main difference between previous hadoop-cluster script:				 #
# - uses preserver NOT qsub								 #
# - number of computational nodes is passed as a parameter not as "final" var in scripts #
#											 #
# REQUIRES OLD settings.sh								 #
#											 #
# NOTE											 #
# script utilizes DAS4 NFS (network FS), each user home dir is propagate across cluster  #
# Thus each operation within script on HOME will be propagated (creating masters, slaves)#
# PAY ATTENTION to path at the end							 #
##########################################################################################

# BUG
# - jak sie wysypie start hadoopa nie zamyka clustera (HAPPENED ONCE)
# VU node09 can not connect properly 10.141.0.09 -> connect fail // generaly *.0.0? -> OK why node9, node8 ???

. ./settings.sh

#delete hadoop.tmp.${USER}
Master=`cat $HADOOP_MASTERS`
ssh $USER@$Master 'rm -rf /local/${USER}/hadoop.tmp.${USER}'
#ssh $USER@$Master 'rm -rf /local/${USER}/np.tmp.${USER}'

cp $HADOOP_SLAVES $HADOOP_CONF/slaves2
for (( i=1; i<=$1; i++ ))
do
        Slave=`cat $HADOOP_CONF/slaves2 | head -1` #sed -n '$i,$i p'`
        sed -i '1d' $HADOOP_CONF/slaves2
        echo $Slave
        ssh $USER@$Slave 'rm -rf /local/${USER}/hadoop.tmp.${USER}'
        ssh $USER@$Slave 'mkdir /local/${USER}/np.tmp.${USER}'
done


# start to set hadoop
echo "HADOOP START"

ssh $USER@$Master 'bash -s' < initHadoop.sh $HADOOP_HOME $HADOOP_CONF

echo "@@@ Wait for 10s to allow all nodes to fully start (sometimes there is a little delay)."
sleep 10

echo "HADOOP END"


# start to set stratosphere
echo "STRATOSPHERE START"


ssh $USER@$Master 'bash -s' < initStratosphere.sh $NP_HOME
echo "@@@ Wait for 10s to allow all nodes to fully start stratosphere"
sleep 10

echo "STRATOSPHERE END"
