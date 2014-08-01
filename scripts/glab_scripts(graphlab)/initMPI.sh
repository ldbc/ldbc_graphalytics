#!/bin/bash 

nodesNr=$1
machines=$2

mpdboot -n $nodesNr -f $machines
