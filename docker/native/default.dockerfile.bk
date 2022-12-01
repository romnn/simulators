# FROM nvidia/cuda:11.8.0-devel-ubuntu20.04
# FROM nvidia/cuda:10.2-devel-ubuntu18.04
FROM nvidia/cuda:10.1-devel-ubuntu18.04

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim tree wget curl

# install nsight compute for hardware profiling using performance counters
# ref: https://developer.nvidia.com/blog/using-nsight-compute-in-containers/
# search for versions: apt-cache search nsight-compute
RUN apt-get install -y nsight-compute-2022.3.0
# RUN export PATH="/opt/nvidia/nsight-compute/2022.3.0/:$PATH"
ENV PATH="$PATH:/opt/nvidia/nsight-compute/2022.3.0"

# install accelsim dependencies
RUN apt-get install -y \
  wget build-essential xutils-dev bison zlib1g-dev flex \
  libglu1-mesa-dev libssl-dev libxml2-dev libboost-all-dev \
  git g++ vim python-setuptools python3-dev python3-pip
RUN pip3 install pyyaml plotly psutil

# clone accelsim
RUN git clone https://github.com/accel-sim/accel-sim-framework.git /work
WORKDIR /work
RUN git checkout release
RUN ls -lia .

# build accelsim
SHELL ["/bin/bash", "-c"]
ENV CUDA_INSTALL_PATH=/usr/local/cuda
RUN cd ./gpu-simulator && source ./setup_environment.sh && make -j
RUN ls -lia .

# # Get the pre-run trace files
# RUN rm -rf ./hw_run/rodinia_2.0-ft
# RUN wget https://engineering.purdue.edu/tgrogers/accel-sim/traces/tesla-v100/latest/rodinia_2.0-ft.tgz
# RUN mkdir -p ./hw_run
# RUN tar -xzvf rodinia_2.0-ft.tgz -C ./hw_run
# RUN rm rodinia_2.0-ft.tgz

# get sample apps
RUN git clone https://github.com/accel-sim/gpu-app-collection /apps
WORKDIR /apps
RUN apt-get install -y \
  libxmu-dev libxmu-headers freeglut3-dev libglu1-mesa-dev \
  ocl-icd-opencl-dev

# todo: make building the apps a different stage as we really only untar and buid here, thats much better for caching
# copy data to avoid downloading it again
COPY ./docker/accelsim/all.gpgpu-sim-app-data.tgz /apps/data_dirs/
RUN tar xzvf /apps/data_dirs/all.gpgpu-sim-app-data.tgz -C /apps

# wrong location?
COPY ./docker/native/gpucomputingsdk_4.2.9_linux.run /apps/
ENV NVIDIA_COMPUTE_SDK_LOCATION=/apps/4.2
RUN chmod u+x /apps/gpucomputingsdk_4.2.9_linux.run && \
  /apps/gpucomputingsdk_4.2.9_linux.run -- \
  --prefix="$NVIDIA_COMPUTE_SDK_LOCATION" --cudaprefix="$CUDA_INSTALL_PATH"
RUN make -j -i -C "$NVIDIA_COMPUTE_SDK_LOCATION"

# RUN ln -sf /work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/libcudart.so /work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/libcudart.so.10.2
# RUN echo /work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/ > /etc/ld.so.conf.d/gpgpu-sim.conf
# export LD_RUN_PATH=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/ && \

# the mode controls whether we build the benchmarks using 
# sim: gpgpusim's libcudart for simulation
# hw: the native libcudart for hardware profiling
ARG mode=sim
ENV MODE=$mode

# build rodinia_2.0-ft benchmarks
# make data should not do anything as we already have the data
RUN source ./src/setup_environment && \
  if [ $MODE == "sim" ]; then source /work/gpu-simulator/gpgpu-sim/setup_environment; fi && \
  if [ $MODE == "sim" ]; then export LD_RUN_PATH=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10010/release/; fi && \
  make -j -C src rodinia_2.0-ft && \
  make -j -C src data

# ldconfig && \
# RUN ln -sf /work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/libcudart.so /work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/libcudart.so.10.2
# ln -sf /work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/libcudart.so /work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/libcudart.so.10.2 && \

# RUN export CUDA_INSTALL_PATH
# cannnot find -lOpenCL
# cannot find -lXmu
# cannot find -lglut

# add our sample applications
# LD_PRELOAD=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/libcudart.so 
COPY ./samples /samples
WORKDIR /samples
# && ldconfig
# export LD_RUN_PATH=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/ && \

# CUOBJDUMP_SIM_FILE=on LD_PRELOAD=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10010/release/libcudart.so ./vectoradd

# hack: always skip the cache for the last command
ADD "https://www.random.org/cgi-bin/randbyte?nbytes=10&format=h" skipcache
RUN if [ $MODE == "sim" ]; then source /work/gpu-simulator/gpgpu-sim/setup_environment; fi && \
  if [ $MODE == "sim" ]; then export LD_RUN_PATH=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10010/release/; fi && \
  cd ./vectoradd && make clean && make -j

RUN ldd /samples/vectoradd/vectoradd

# source /work/gpu-simulator/setup_environment.sh && \
# LD_PRELOAD=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/libcudart.so ./vectoradd

# Run the tests on the trace
# RUN source ./gpu-simulator/gpgpu-sim/setup_environment && \

# RUN source ./gpu-simulator/setup_environment.sh && \
#   ./util/job_launching/run_simulations.py \
#   -C QV100-SASS -B rodinia_2.0-ft -T ./hw_run/rodinia_2.0-ft/9.1 -N myTest

# # Wait for them to finish
# RUN source ./gpu-simulator/setup_environment.sh && \
#   ./util/job_launching/monitor_func_test.py -v -N myTest

# build the accelsim tracer
# RUN ./util/tracer_nvbit/install_nvbit.sh
# RUN make -j -C ./util/tracer_nvbit/

# clone benchmarks
# RUN git clone https://github.com/gpgpu-sim/gpgpu-sim_simulations.git /benchmarks
# WORKDIR /benchmarks
# RUN apt-get install -y libglew-dev scons freeglut3-dev
# RUN cd ./benchmarks/src && source ./setup_environment dev && make -j
