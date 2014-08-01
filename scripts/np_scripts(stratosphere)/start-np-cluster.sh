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

set -e

# check for number of nodes
if [ $# -eq 0 ] 
  then
    echo "Number of computational nodes required"
    exit
fi

# load proper module
module load prun

# request nodes
preserve -np $1 -t 72:30:00
sleep 5

# get hosts array
nodesNr=`preserve -llist | grep $USER | awk '{print NF}'`
echo $nodesNr
let "nodesNr -= 8"
index=9
for (( i=0; i<$nodesNr; i++ ))
do   
  hosts[$i]=`preserve -llist | grep $USER | awk -v col=$index '{print $col}'`
  let "index += 1"
done

# check if sufficient amount of nodes is available
if [ "${hosts[0]}" == "-" ]
  then
    echo "INSUFFICIENT AMOUNT OF NODES, WILL EXIT"
    qdel -u $USER
    exit
fi

# create addresses of nodes
adrPrefix="10.141."
site=${HOSTNAME:2} # cluster site
for (( i=0; i<$nodesNr; i++ ))
do
  if [ "${hosts[$i]:5:1}" = "0" ]
    then
      adrs[$i]=$adrPrefix$site"."${hosts[$i]:6}
    else
      adrs[$i]=$adrPrefix$site"."${hosts[$i]:5}
  fi
done


# create settings
. ./settings.sh

# start to set hadoop
echo "HADOOP START"

# clean masters and slaves (Hadoop required files)
cat /dev/null > $HADOOP_CONF/masters
cat /dev/null > $HADOOP_CONF/slaves

# create masters and slaves (Hadoop required files)
for (( i=0; i<$nodesNr; i++ ))
do
  if [ $i -eq 0 ] # MASTER
    then
      echo "${adrs[$i]}" >$HADOOP_MASTERS
      ssh $USER@"${adrs[$i]}" 'rm -rf  "/local/$USER/hadoop.tmp.$USER"'
      continue
  fi
  # SLAVES
  echo "${adrs[$i]}" >>$HADOOP_SLAVES  
  ssh $USER@"${adrs[$i]}" 'rm -rf  "/local/$USER/hadoop.tmp.$USER"'
done
Master=`cat $HADOOP_MASTERS`

# overwrite hadoop confs (not clean solution but I like the idea of "one conf to rule them all")
cp $TEMPLATE $HADOOP_CONF_CORE
# fill TEMPLATE dynamic "vars" (Master.Adr and Hadoop.TMP)
_HOSTNAME=$(echo "${Master}"|sed -e 's/\(\/\|\\\|&\)/\\&/g')
_HADOOP_TEMP_DIR=$(echo "${HADOOP_TEMP_DIR}"|sed -e 's/\(\/\|\\\|&\)/\\&/g')
sed -i "s/%%MASTER%%/$_HOSTNAME/g" $HADOOP_CONF_CORE
sed -i "s/%%HADOOP_TEMP_DIR%%/$_HADOOP_TEMP_DIR/g" $HADOOP_CONF_CORE

cp $HADOOP_CONF_CORE $HADOOP_CONF/mapred-site.xml
cp $HADOOP_CONF_CORE $HADOOP_CONF/hdfs-site.xml

#let NFS propagate data
sleep 5

# Connect to Master -> WARNING can use headNode HADOOP_HOME/bin/start-all // to samo dla stop (plus qdel -u $USER) i client
ssh $USER@$Master 'bash -s' < initHadoop.sh $HADOOP_HOME $HADOOP_CONF

echo "@@@ Wait for 10s to allow all nodes to fully start (sometimes there is a little delay)."
sleep 10

echo "HADOOP END"


# start to set stratosphere
echo "STRATOSPHERE START"

# clean slaves file
cat /dev/null > $NP_CONF/slaves

# create slaves file
for (( i=0; i<$nodesNr; i++ ))
do
  if [ $i -eq 0 ] # MASTER
    then
      ssh $USER@"${adrs[$i]}" 'bash -s' < np-mktempdir.sh $NP_TEMP_DIR
      continue
  fi
  # SLAVES
  ssh $USER@"${adrs[$i]}" 'bash -s' < np-mktempdir.sh $NP_TEMP_DIR
  echo "${adrs[$i]}" >>$NP_SLAVES
done


# create nephele-user.xml file and pact-user.xml file
cp $SCRIPTS/nephele-user.xml.template $NP_CONF/nephele-user.xml
cp $SCRIPTS/pact-user.xml.template $NP_CONF/pact-user.xml

_HOSTNAME=$(echo "${Master}"|sed -e 's/\(\/\|\\\|&\)/\\&/g')
_NP_TEMP_DIR=$(echo "${NP_TEMP_DIR}"|sed -e 's/\(\/\|\\\|&\)/\\&/g')
_NP_HOME=$(echo "${NP_HOME}"|sed -e 's/\(\/\|\\\|&\)/\\&/g')

sed -i "s/%%MASTER%%/$_HOSTNAME/g" $NP_CONF/nephele-user.xml
sed -i "s/%%NP_TEMP_DIR%%/$_NP_TEMP_DIR/g" $NP_CONF/nephele-user.xml


sed -i "s/%%NP_TEMP_DIR%%/$_NP_TEMP_DIR/g" $NP_CONF/pact-user.xml
sed -i "s/%%NP_HOME%%/$_NP_HOME/g" $NP_CONF/pact-user.xml


ssh $USER@$Master 'bash -s' < initStratosphere.sh $NP_HOME
echo "@@@ Wait for 10s to allow all nodes to fully start stratosphere"
sleep 10

echo "STRATOSPHERE END"
