set -e
export KERNEL_INFO_PATH="/benchrun/macsim/cuda6-transpose/sm6_gtx1080/input-repeat1-dimx64-dimy64/occupancy.txt"
export TRACE_PATH="/benchrun/macsim/cuda6-transpose/sm6_gtx1080/input-repeat1-dimx64-dimy64/results/traces"
export COMPUTE_VERSION="2.0"
/benchrun/macsim/cuda6-transpose/sm6_gtx1080/input-repeat1-dimx64-dimy64/transpose -repeat=1 -dimX=64 -dimY=64
