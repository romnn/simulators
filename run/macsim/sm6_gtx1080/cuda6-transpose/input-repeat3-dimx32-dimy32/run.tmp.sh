set -e
export TRACE_PATH="/benchrun/macsim/sm6_gtx1080/cuda6-transpose/input-repeat3-dimx32-dimy32/results/traces"
export COMPUTE_VERSION="2.0"
export KERNEL_INFO_PATH="/benchrun/macsim/sm6_gtx1080/cuda6-transpose/input-repeat3-dimx32-dimy32/occupancy.txt"
/benchrun/macsim/sm6_gtx1080/cuda6-transpose/input-repeat3-dimx32-dimy32/transpose -repeat=3 -dimX=32 -dimY=32
