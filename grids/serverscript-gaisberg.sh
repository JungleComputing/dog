#!/bin/sh

NR_NODES=$1
shift
shift
JAVA=$1
shift
ARGS=$*

for i in `seq 1 $NR_NODES`; do
    read node
    ssh -x -x $node "cd $PWD && $JAVA $ARGS $NR_NODES" &
done < $HOME/nodes

wait