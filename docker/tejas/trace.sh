#!/usr/bin/env bash

# set -e
# set -euxo pipefail
# set -exo pipefail
set -exo pipefail

# CONFIG_PATH="${CONFIG_PATH:-config.xml}"
# echo "config: $CONFIG_PATH"

ARGS="$@"

if [[ -z "$TEJAS_ROOT" ]]; then
  echo "ERROR: TEJAS_ROOT environment variable is not set"
  exit 1
fi

# clean the files
rm -rf *.txt tmp

# get the number of threads
THREADS=`grep -o '<MaxNumJavaThreads>.*</MaxNumJavaThreads>' config.xml | cut -d'<' -f 2 | cut -d'>' -f 2`
echo "num threads: $THREADS"

# generate the traces
./tracegen $ARGS $THREADS

# get the number of kernels launched
KERNELS=`grep "KERNEL START" 0.txt | wc -l`
echo "num kernels launched: $KERNELS"

# creating a new folder & move the text files	
rm -rf $THREADS
mkdir -p $THREADS
mv *.txt $THREADS

# simplify traces
echo "simplifying the traces"
if !(java -jar "$TEJAS_ROOT/Tracesimplifier.jar" config.xml tmp . $KERNELS) then
  echo "problem with simplifying the traces :("
  exit 1
fi

echo "done"
echo "run the benchmark with ./run.sh"
