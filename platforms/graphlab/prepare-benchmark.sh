#!/bin/sh

# Ensure the configuration file exists
if [ ! -f "$config/graphlab.properties" ]; then
	echo "Missing mandatory configuration file: $config/graphlab.properties" >&2
	exit 1
fi

# Get the first specification of hadoop.home
hadoophome=$(grep -E "^hadoop.home[	 ]*[:=]" $config/graphlab.properties | sed 's/hadoop.home[\t ]*[:=][\t ]*\([^\t ]*\).*/\1/g' | head -n 1)
if [ ! -f "$hadoophome/bin/hadoop" ]; then
	echo "Invalid definition of hadoop.home: $hadoophome" >&2
	echo "Could not find hadoop executable: $hadoophome/bin/hadoop" >&2
	exit 1
fi
echo "Using HADOOP_HOME=$hadoophome"
export HADOOP_HOME=$hadoophome

# Construct the classpath
platform_classpath="$($HADOOP_HOME/bin/hadoop classpath)"
export platform_classpath=$platform_classpath

