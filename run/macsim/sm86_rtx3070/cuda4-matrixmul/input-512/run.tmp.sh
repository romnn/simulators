set -e
export TRACE_PATH="/benchrun/macsim/cuda4-matrixmul/sm86_rtx3070/input-512/results/traces"
export KERNEL_INFO_PATH="/benchrun/macsim/cuda4-matrixmul/sm86_rtx3070/input-512/occupancy.txt"
export COMPUTE_VERSION="2.0"
/benchrun/macsim/cuda4-matrixmul/sm86_rtx3070/input-512/matrixMul 512
