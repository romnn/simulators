set -e
export KERNEL_INFO_PATH="/benchrun/macsim/cuda6-transpose/sm86_rtx3070/input-repeat1-dimx64-dimy64/occupancy.txt"
export COMPUTE_VERSION="2.0"
export TRACE_PATH="/benchrun/macsim/cuda6-transpose/sm86_rtx3070/input-repeat1-dimx64-dimy64/results/traces"
/benchrun/macsim/cuda6-transpose/sm86_rtx3070/input-repeat1-dimx64-dimy64/transpose -repeat=1 -dimX=64 -dimY=64
