set -e
export KERNEL_INFO_PATH="/benchrun/macsim/cuda4-matrixmul/sm6_gtx1080/input-128/occupancy.txt"
export TRACE_PATH="/benchrun/macsim/cuda4-matrixmul/sm6_gtx1080/input-128/results/traces"
export COMPUTE_VERSION="2.0"
/benchrun/macsim/cuda4-matrixmul/sm6_gtx1080/input-128/matrixMul 128
