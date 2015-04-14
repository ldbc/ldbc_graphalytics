#!/bin/sh

# Ensure the configuration files exist
if [ ! -f "$config/hadoop.properties" ]; then
	echo "Missing mandatory configuration file: $config/hadoop.properties" >&2
	exit 1
fi

if [ ! -f "$config/graphlab.properties" ]; then
	echo "Missing mandatory configuration file: $config/graphlab.properties" >&2
	exit 1
fi

target=$(grep -E "^graphlab.target[	 ]*[:=]" $config/graphlab.properties | sed 's/graphlab.target[\t ]*[:=][\t ]*\([^\t ]*\).*/\1/g' | head -n 1)
case "$target" in
	local)
		echo "Using target=local"
	;;
	hadoop)
		echo "Using target=hadoop"
		# Get the first specification of hadoop.home
		hadoophome=$(grep -E "^hadoop.home[	 ]*[:=]" $config/hadoop.properties | sed 's/hadoop.home[\t ]*[:=][\t ]*\([^\t ]*\).*/\1/g' | head -n 1)
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
	;;
	"")
		echo "Using default target=local"
	;;
	*)
		echo "Unknown target specified: $target" >&2
		echo "Must be one of: {hadoop,local}" >&2
		exit 1
	;;
esac

