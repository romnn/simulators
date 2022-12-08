FROM nvidia/cuda:10.1-devel-ubuntu18.04

SHELL ["/bin/bash", "-c"]
LABEL MAINTAINER="roman <contact@romnn.com>"

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim tree wget curl pigz

# install nsight compute for hardware profiling using performance counters
# ref: https://developer.nvidia.com/blog/using-nsight-compute-in-containers/
# search for versions: apt-cache search nsight-compute
# RUN apt-get install -y nsight-compute-2022.3.0
COPY ./nsight-compute-2022.3.0_2022.3.0.22-1_amd64.deb /deps/
RUN apt-get install -y /deps/nsight-compute-2022.3.0_2022.3.0.22-1_amd64.deb
ENV PATH="$PATH:/opt/nvidia/nsight-compute/2022.3.0"

# install accelsim dependencies
# RUN apt-get install -y \
#   wget build-essential xutils-dev bison zlib1g-dev flex \
#   libglu1-mesa-dev libssl-dev libxml2-dev libboost-all-dev \
#   git g++ vim python-setuptools python3-dev python3-pip
# RUN pip3 install pyyaml plotly psutil numpy

# # clone accelsim
# RUN git clone https://github.com/accel-sim/accel-sim-framework.git /work && \
#   cd /work && git checkout release

# get sample apps
# RUN git clone https://github.com/accel-sim/gpu-app-collection /apps
# RUN apt-get install -y \
#   libxmu-dev libxmu-headers freeglut3-dev libglu1-mesa-dev \
#   ocl-icd-opencl-dev

# copy data required by the make data command to avoid downloading it
# COPY ./docker/accelsim/all.gpgpu-sim-app-data.tgz /apps/data_dirs/
# RUN tar -I pigz -xf /apps/data_dirs/all.gpgpu-sim-app-data.tgz -C /apps

# copy and install the gpucomputingsdk to avoid downloading it
# COPY ./docker/native/gpucomputingsdk_4.2.9_linux.run /apps/
# ENV CUDA_INSTALL_PATH=/usr/local/cuda
# ENV NVIDIA_COMPUTE_SDK_LOCATION=/apps/4.2
# RUN chmod u+x /apps/gpucomputingsdk_4.2.9_linux.run && \
#   /apps/gpucomputingsdk_4.2.9_linux.run -- \
#   --prefix="$NVIDIA_COMPUTE_SDK_LOCATION" \
#   --cudaprefix="$CUDA_INSTALL_PATH"
# RUN make -j -i -C "$NVIDIA_COMPUTE_SDK_LOCATION"

# note: building the benchmarks is left to avoid linking 
# the wrong libcudart when simulation should be used
