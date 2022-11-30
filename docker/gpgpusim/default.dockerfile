# 22.04 does not work: https://github.com/gpgpu-sim/gpgpu-sim_distribution/issues/221
# FROM nvidia/cuda:11.8.0-devel-ubuntu22.04
FROM nvidia/cuda:11.8.0-devel-ubuntu20.04
# FROM nvidia_cuda_11.8.0-devel-ubuntu20.04

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim

# install gpgpusm dependencies
RUN apt-get install -y build-essential xutils-dev bison zlib1g-dev flex libglu1-mesa-dev

# clone gpgpusim
RUN git clone https://github.com/gpgpu-sim/gpgpu-sim_distribution.git /work
WORKDIR /work
RUN git checkout dev
RUN ls -lia .

# build gpgpusim
SHELL ["/bin/bash", "-c"]
ENV CUDA_INSTALL_PATH=/usr/local/cuda
RUN source ./setup_environment dev && make -j

# clone benchmarks
RUN git clone https://github.com/gpgpu-sim/gpgpu-sim_simulations.git /benchmarks
WORKDIR /benchmarks
RUN apt-get install -y libglew-dev scons freeglut3-dev
# RUN cd ./benchmarks/src && source ./setup_environment dev && make -j
