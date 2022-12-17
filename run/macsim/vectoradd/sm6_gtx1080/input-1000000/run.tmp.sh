set -e
export KERNEL_INFO_PATH="/benchrun/macsim/vectoradd/sm6_gtx1080/input-1000000/occupancy.txt"
export TRACE_PATH="/benchrun/macsim/vectoradd/sm6_gtx1080/input-1000000/results/traces"
export COMPUTE_VERSION="2.0"
/benchrun/macsim/vectoradd/sm6_gtx1080/input-1000000/vectorAdd 1000000
