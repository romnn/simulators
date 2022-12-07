# FROM nvidia/cuda:10.2-devel-ubuntu18.04
# m2s requires 32bit, which CUDA only supports up to version 8
FROM nvidia/cuda:8.0-devel-ubuntu14.04

# NVIDIA's ubuntu 14.04 packages no longer exist
RUN rm /etc/apt/sources.list.d/cuda.list

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim wget curl zlib1g-dev

# multi2sim dependencies
RUN apt-get install -y \
  zlib1g-dev lib32gcc1 gcc-multilib libgtk-3-dev \
  libtool autotools-dev autoconf automake

# install multi2sim
# RUN wget http://www.multi2sim.org/files/multi2sim-4.1.tar.gz && \
RUN git clone https://github.com/Multi2Sim/multi2sim.git /app
WORKDIR /app
RUN cd /app && \
  libtoolize && \
  aclocal && \
  autoconf && \
  automake --add-missing && \
  ./configure && make -j && make install && ldconfig

# add sample app
COPY ./samples /samples
WORKDIR /samples/vectoradd
# RUN make clean && make -j

COPY ./docker/assets/cuda_8.0.61_375.26_linux.run /cuda_8.0.61_375.26_linux.run

# todo: add the stuff we need for m32 support
# nvcc -ccbin=/usr/bin/gcc -Xcompiler -m32 -m32 -O3 vectoradd.cu -o vectoradd

# https://github.com/Multi2Sim/multi2sim/issues/8
# RUN wget http://www.multi2sim.org/downloads/multi2sim-5.0.tar.gz && \
#  tar -xzf multi2sim-5.0.tar.gz && \
#  cd multi2sim-5.0 && \
#  libtoolize && \
#  aclocal && \
#  autoconf && \
#  automake --add-missing && \
#  ./configure && make -j

# RUN wget http://www.multi2sim.org/downloads/multi2sim-4.1.tar.gz && \
#  tar -xzf multi2sim-4.1.tar.gz && \
#  cd multi2sim-4.1 && \
#  ./configure && make -j
