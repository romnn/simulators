set -e
export KERNEL_INFO_PATH="/benchrun/macsim/vectoradd/sm6_gtx1080/input-1000/occupancy.txt"
export COMPUTE_VERSION="2.0"
export TRACE_PATH="/benchrun/macsim/vectoradd/sm6_gtx1080/input-1000/results/traces"
/benchrun/macsim/vectoradd/sm6_gtx1080/input-1000/vectorAdd 1000
