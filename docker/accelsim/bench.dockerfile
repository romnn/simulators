FROM romnn/accelsim-base

# copy the benchmarks
COPY ./benchmarks /benchmarks
WORKDIR /benchmarks

# todo: compile the benchmarks
# # hack: always skip the cache for the last command
# ADD "https://www.random.org/cgi-bin/randbyte?nbytes=10&format=h" skipcache
# RUN source /work/gpu-simulator/gpgpu-sim/setup_environment && \
#   export LD_RUN_PATH=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10010/release/ && \
#   cd ./vectoradd && make clean && make -j

# RUN ldd /samples/vectoradd/vectoradd

