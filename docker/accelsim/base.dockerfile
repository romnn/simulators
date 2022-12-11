# FROM nvidia/cuda:11.8.0-devel-ubuntu20.04
# FROM nvidia/cuda:10.2-devel-ubuntu18.04
# for SASS execution: the CUDA version should match the CUDA version of the HOST running the GPU
FROM nvidia/cuda:10.1-devel-ubuntu18.04

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim

# install accelsim dependencies
RUN apt-get install -y \
  wget build-essential xutils-dev bison zlib1g-dev flex \
  libglu1-mesa-dev libssl-dev libxml2-dev libboost-all-dev \
  git g++ vim python-setuptools python3-dev python3-pip
RUN pip3 install pyyaml plotly psutil

# clone accelsim
RUN git clone https://github.com/accel-sim/accel-sim-framework.git /simulator
WORKDIR /simulator
RUN git checkout dev

# build accelsim tracer
SHELL ["/bin/bash", "-c"]
ENV CUDA_INSTALL_PATH=/usr/local/cuda
ENV NVBIT_TRACER_ROOT=/simulator/util/tracer_nvbit/
RUN /simulator/util/tracer_nvbit/install_nvbit.sh && \
  ls -lia /simulator/util/tracer_nvbit/ && \
  make -j -C /simulator/util/tracer_nvbit

# build accelsim
# RUN cd /simulator/gpu-simulator && source ./setup_environment.sh && make -j
RUN source /simulator/gpu-simulator/setup_environment.sh && \
  make -C /simulator/gpu-simulator -j
