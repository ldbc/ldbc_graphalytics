#!/bin/bash

# create settings
. ./settings.sh

$HADOOP_HOME/bin/yarn $@
