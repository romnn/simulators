set -e
export KERNEL_INFO_PATH="/benchrun/macsim/cuda6-transpose/sm6_gtx1080/input-repeat3-dimx32-dimy32/occupancy.txt"
export COMPUTE_VERSION="2.0"
export TRACE_PATH="/benchrun/macsim/cuda6-transpose/sm6_gtx1080/input-repeat3-dimx32-dimy32/results/traces"
/benchrun/macsim/cuda6-transpose/sm6_gtx1080/input-repeat3-dimx32-dimy32/transpose -repeat=3 -dimX=32 -dimY=32
