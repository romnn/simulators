set -e
export TRACE_PATH="/benchrun/macsim/cuda4-matrixmul/sm86_a4000/input-32/results/traces"
export KERNEL_INFO_PATH="/benchrun/macsim/cuda4-matrixmul/sm86_a4000/input-32/occupancy.txt"
export COMPUTE_VERSION="2.0"
/benchrun/macsim/cuda4-matrixmul/sm86_a4000/input-32/matrixMul 32
