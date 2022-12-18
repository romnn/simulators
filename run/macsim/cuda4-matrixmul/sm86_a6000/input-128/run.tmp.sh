set -e
export KERNEL_INFO_PATH="/benchrun/macsim/cuda4-matrixmul/sm86_a6000/input-128/occupancy.txt"
export COMPUTE_VERSION="2.0"
export TRACE_PATH="/benchrun/macsim/cuda4-matrixmul/sm86_a6000/input-128/results/traces"
/benchrun/macsim/cuda4-matrixmul/sm86_a6000/input-128/matrixMul 128
