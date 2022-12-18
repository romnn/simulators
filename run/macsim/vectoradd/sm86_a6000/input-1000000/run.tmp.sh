set -e
export KERNEL_INFO_PATH="/benchrun/macsim/vectoradd/sm86_a6000/input-1000000/occupancy.txt"
export COMPUTE_VERSION="2.0"
export TRACE_PATH="/benchrun/macsim/vectoradd/sm86_a6000/input-1000000/results/traces"
/benchrun/macsim/vectoradd/sm86_a6000/input-1000000/vectorAdd 1000000
