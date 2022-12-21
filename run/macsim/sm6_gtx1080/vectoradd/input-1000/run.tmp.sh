set -e
export TRACE_PATH="/benchrun/macsim/sm6_gtx1080/vectoradd/input-1000/results/traces"
export COMPUTE_VERSION="2.0"
export KERNEL_INFO_PATH="/benchrun/macsim/sm6_gtx1080/vectoradd/input-1000/occupancy.txt"
/benchrun/macsim/sm6_gtx1080/vectoradd/input-1000/vectorAdd 1000
