#!/bin/bash 

NP_HOME=$1

echo "@@@ START STRATOSPHERE @@@"
$NP_HOME/bin/./start-cluster.sh
$NP_HOME/bin/./start-pact-web.sh > start-pact-web
