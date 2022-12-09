FROM ubuntu:trusty
# FROM ubuntu:20.04

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y \
    g++-4.8 g++-4.6 g++-4.4 \
    libboost-mpi-python1.54-dev \
    libboost1.54-all-dev \
    libboost1.54-tools-dev \
    git wget flex bison scons make vim curl libbison-dev \
    freeglut3-dev \
    libglew-dev libxi-dev libXmu-dev

# bison=1:2.5.dfsg-2.1ubuntu1 \
# flex \
# freeglut3-dev \
# git \
# g++-4.8 g++-4.6 g++-4.4 \
# libbison-dev=1:2.5.dfsg-2.1ubuntu1 \
# libboost1.49-all-dev libboost1.49-dev \
# libboost-mpi-python1.49-dev libboost-mpi-python1.49.0 \
# libglew-dev \
# libxi-dev \
# libXmu-dev \
# make \
# scons \
# vim \ 
# wget && \
# apt-mark hold libbison-dev bison 


# wget http://security.ubuntu.com/ubuntu/pool/multiverse/n/nvidia-graphics-drivers-352/libcuda1-352_352.63-0ubuntu0.14.04.1_amd64.deb && \
# wget http://developer.download.nvidia.com/compute/cuda/4_2/rel/toolkit/cudatoolkit_4.2.9_linux_64_ubuntu11.04.run && \
# dpkg -i libcuda1-352_352.63-0ubuntu0.14.04.1_amd64.deb && \

RUN mkdir /tmp/cuda_toolkit
COPY ./cudatoolkit_4.2.9_linux_64_ubuntu11.04.run /tmp/cuda_toolkit/cudatoolkit_4.2.9_linux_64_ubuntu11.04.run

RUN cd /tmp/cuda_toolkit && \
  chmod +x cudatoolkit_4.2.9_linux_64_ubuntu11.04.run && \
  ./cudatoolkit_4.2.9_linux_64_ubuntu11.04.run --tar mxvf && \
  perl install-linux.pl auto && \
  cd / && rm -rf /tmp/cuda_toolkit && \
  printf "/usr/local/cuda/lib64\n/usr/local/cuda/lib" > /etc/ld.so.conf.d/cuda.conf && \
  ldconfig

ENV PATH /usr/local/cuda/bin:$PATH

# Ocelot needs 4.6 for compilation, but programs that link to ocelot's trace generator work better with 4.4

# error: alternative path /usr/bin/gcc-4.6 doesn't exist
RUN update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.8 10 && \
  update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.6 20 && \
  update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.4 30 && \
  update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-4.8 10 && \
  update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-4.6 20 && \
  update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-4.4 30 && \
  update-alternatives --install /usr/bin/cc cc /usr/bin/gcc 40 && \
  update-alternatives --set cc /usr/bin/gcc && \
  update-alternatives --install /usr/bin/c++ c++ /usr/bin/g++ 40 && \
  update-alternatives --set c++ /usr/bin/g++ && \
  update-alternatives --set gcc "/usr/bin/gcc-4.6" && \
  update-alternatives --set g++ "/usr/bin/g++-4.6"

# fatal error: boost/lexical_cast.hpp: No such file or directory
RUN cd /usr/local/src && \
  git clone --recursive https://github.com/gthparch/gpuocelot.git && \
  cd gpuocelot && ./build.py --thread 16 --install && \
  ldconfig

RUN cd /usr/local/src/trace-generators && \
  scons install && \
  ldconfig

# && \
# update-alternatives --set gcc "/usr/bin/gcc-4.4" && \
# update-alternatives --set g++ "/usr/bin/g++-4.4"

