set -e
export TRACE_PATH="/benchrun/macsim/sm6_gtx1080/cuda4-matrixmul/input-128/results/traces"
export COMPUTE_VERSION="2.0"
export KERNEL_INFO_PATH="/benchrun/macsim/sm6_gtx1080/cuda4-matrixmul/input-128/occupancy.txt"
/benchrun/macsim/sm6_gtx1080/cuda4-matrixmul/input-128/matrixMul 128
