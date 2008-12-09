#!/bin/sh

NR_NODES=$1
shift
shift
JAVA=$1
shift
ARGS=$*

echo $JAVA $NR_NODES $ARGS $NR_NODES
$JAVA $ARGS
