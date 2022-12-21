set -e
export TRACE_PATH="/benchrun/macsim/sm6_gtx1080/cuda6-transpose/input-repeat1-dimx128-dimy128/results/traces"
export COMPUTE_VERSION="2.0"
export KERNEL_INFO_PATH="/benchrun/macsim/sm6_gtx1080/cuda6-transpose/input-repeat1-dimx128-dimy128/occupancy.txt"
/benchrun/macsim/sm6_gtx1080/cuda6-transpose/input-repeat1-dimx128-dimy128/transpose -repeat=1 -dimX=128 -dimY=128
