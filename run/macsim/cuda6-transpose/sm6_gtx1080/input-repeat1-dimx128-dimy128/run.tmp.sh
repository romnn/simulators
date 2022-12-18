set -e
export KERNEL_INFO_PATH="/benchrun/macsim/cuda6-transpose/sm6_gtx1080/input-repeat1-dimx128-dimy128/occupancy.txt"
export COMPUTE_VERSION="2.0"
export TRACE_PATH="/benchrun/macsim/cuda6-transpose/sm6_gtx1080/input-repeat1-dimx128-dimy128/results/traces"
/benchrun/macsim/cuda6-transpose/sm6_gtx1080/input-repeat1-dimx128-dimy128/transpose -repeat=1 -dimX=128 -dimY=128
