#!/usr/bin/env bash

set -exo pipefail

OUTFILE="$1"

if [[ -z "$TEJAS_ROOT" ]]; then
  echo "ERROR: TEJAS_ROOT environment variable is not set"
  exit 1
fi

# get the number of threads
THREADS=$(grep -o '<MaxNumJavaThreads>.*</MaxNumJavaThreads>' config.xml | cut -d'<' -f 2 | cut -d'>' -f 2)
echo "num threads: $THREADS"

# get the number of kernels launched
KERNELS=$(ls $THREADS/hashfile_* | wc -l)
echo "num kernels launched: $KERNELS"

# run the simulator
if !(java -jar "$TEJAS_ROOT/jars/GPUTejas.jar" config.xml $OUTFILE . $KERNELS) then
  echo "problem running the benchmark :("
  exit 1
fi
