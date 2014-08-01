#!/bin/bash

# this is crappy solution SETS PATHS
#export JAVA_HOME="/usr/lib/jvm/java-1.6.0-openjdk.x86_64"  
export SCRIPTS="$HOME/yarn_scripts"				# SET ACCORDING TO YOUR PREFERENCES
export TEMPLATE="$SCRIPTS/core-site.xml.template"
export YARN_TEMPLATE="$SCRIPTS/yarn-site.xml.template"


export HADOOP_HOME="$HOME/hadoop-2.0.3-alpha"  	# SET ACCORDING TO YOUR PREFERENCES
export HADOOP_TEMP_DIR="/local/yongguo/yarn.tmp.$USER"	# TU DELFT && VU
export HADOOP_CONF="$HADOOP_HOME/etc/hadoop"
export HADOOP_CONF_CORE="$HADOOP_CONF/core-site.xml"
export HADOOP_CONF_YARN="$HADOOP_CONF/yarn-site.xml"
export HADOOP_MASTERS="$HADOOP_CONF/masters"
export HADOOP_SLAVES="$HADOOP_CONF/slaves"

