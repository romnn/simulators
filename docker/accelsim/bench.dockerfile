FROM romnn/accelsim-base

# add the benchmarks
COPY ./benchmarks /benchmarks
WORKDIR /benchmarks

# compile the benchmarks
ENV SIM_ROOT /simulator/gpu-simulator/gpgpu-sim
RUN source $SIM_ROOT/setup_environment && \
  cd /benchmarks && \
  make clean && \
  make -j accelsim

# ALL_LDFLAGS="--cudart shared" 
# RUN source $SIM_ROOT/setup_environment && \
  # export LD_RUN_PATH=$SIM_ROOT/lib/gcc-$CC_VERSION/cuda-$CUDA_VERSION_NUMBER/release/ && \
  # cd /benchmarks/matrixMul && make clean && make -j ALL_LDFLAGS="--cudart shared" && \
  # cd /benchmarks/matrixMul-modified && make clean && make -j ALL_LDFLAGS="--cudart shared"


# cd ./vectoradd && make clean && make -j
# export LD_RUN_PATH=/simulator/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10010/release/ && \
# RUN ldd /samples/vectoradd/vectoradd

