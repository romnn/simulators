FROM romnn/native-base

WORKDIR /work

# build accelsim
ENV CUDA_INSTALL_PATH=/usr/local/cuda
RUN cd ./gpu-simulator && source ./setup_environment.sh && make -j
RUN ls -lia .

# build rodinia_2.0-ft benchmarks
# make data should not do anything as we already have the data
RUN source ./src/setup_environment && \
  if [ $MODE == "sim" ]; then source /work/gpu-simulator/gpgpu-sim/setup_environment; fi && \
  if [ $MODE == "sim" ]; then export LD_RUN_PATH=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10010/release/; fi && \
  make -j -C src rodinia_2.0-ft && \
  make -j -C src data

COPY ./samples /samples
WORKDIR /samples

# hack: always skip the cache for the last command
ADD "https://www.random.org/cgi-bin/randbyte?nbytes=10&format=h" skipcache
RUN if [ $MODE == "sim" ]; then source /work/gpu-simulator/gpgpu-sim/setup_environment; fi && \
  if [ $MODE == "sim" ]; then export LD_RUN_PATH=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10010/release/; fi && \
  cd ./vectoradd && make clean && make -j

RUN ldd /samples/vectoradd/vectoradd
