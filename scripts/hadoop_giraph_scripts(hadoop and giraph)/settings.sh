#!/bin/bash

# this is crappy solution SETS PATHS
#export JAVA_HOME="/usr/lib/jvm/java-1.6.0-openjdk.x86_64"  
export SCRIPTS="$HOME/hadoop_giraph_scripts"				# SET ACCORDING TO YOUR PREFERENCES
export TEMPLATE="$SCRIPTS/core-site.xml.template"

export HADOOP_HOME="$HOME/exeLibs/hadoop-0.20.203.0"  	# SET ACCORDING TO YOUR PREFERENCES
export HADOOP_TEMP_DIR="/local/yongguo/hadoop.tmp.$USER"	# TU DELFT && VU
export HADOOP_CONF="$HADOOP_HOME/conf"
export HADOOP_CONF_CORE="$HADOOP_CONF/core-site.xml"
export HADOOP_MASTERS="$HADOOP_CONF/masters"
export HADOOP_SLAVES="$HADOOP_CONF/slaves"
