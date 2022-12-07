# FROM ubuntu:20.04
FROM ubuntu:14.04
# FROM ubuntu:12.04

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim wget curl zlib1g-dev strace gdb

# multi2sim dependencies
RUN apt-get install -y \
  zlib1g-dev lib32gcc1 gcc-multilib g++-multilib libgtk-3-dev \
  libtool autotools-dev autoconf automake

# install multi2sim
# this needs gcc >= 4.8 to support C++11
# RUN wget http://www.multi2sim.org/files/multi2sim-4.1.tar.gz && \
RUN git clone https://github.com/Multi2Sim/multi2sim.git /app
WORKDIR /app
RUN cd /app && \
  git checkout multi2sim-kepler && \
  libtoolize && \
  aclocal && \
  autoconf && \
  automake --add-missing && \
  ./configure && make -j && make install && ldconfig

# For CUDA 5.0: install older version of gcc (<4.7)
# RUN apt-get install -y gcc-4.6 g++-4.6 && \
#   update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.6 4 && \
#   update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-4.6 4 && \
#   update-alternatives --config gcc && \
#   update-alternatives --config g++
# RUN apt-get install -y lib32gcc1 gcc-4.6-multilib lib32g++ g++-4.6-multilib 

# cuda installation can fail silently, logs can be found in either
# /tmp/cuda_install_*.log (CUDA 5)
# /var/log/nvidia-installer.log (else)

# install CUDA 7.0 (CUDA 8+ does not support 32bit compilation)
# COPY ./docker/assets/cuda_8.0.61_375.26_linux.run /install_cuda.run
# COPY ./docker/assets/cuda_7.0.28_linux.run /install_cuda.run
# COPY ./docker/assets/cuda_5.0.35_linux_64_ubuntu11.10-1.run /install_cuda.run
COPY ./docker/assets/cuda_6.0.37_linux_64.run /install_cuda.run
# RUN chmod +x /install_cuda.run
RUN chmod +x /install_cuda.run && \
  /install_cuda.run --silent --toolkit --override && \
  if [ -f /var/log/nvidia-installer.log ]; then cat /var/log/nvidia-installer.log; fi && \
  /usr/local/cuda/bin/nvcc --version

# --override 
# COPY ./docker/assets/cudatoolkit_4.2.9_linux_32_ubuntu11.04.run /install_cuda.run
# COPY ./docker/assets/cuda_5.0.35_linux_64_ubuntu11.10-1.run /install_cuda.run
# RUN chmod +x /install_cuda.run && \
#   /install_cuda.run --nox11 && \
#   /usr/local/cuda/bin/nvcc --version

ENV PATH /usr/local/cuda/bin:$PATH

# install packages for 32 bit compilation
# RUN apt-get install -y lib32gcc1 gcc-multilib lib32g++ g++-multilib 
# gcc-4.7

COPY ./samples /samples
WORKDIR /samples/vectoradd
# nvcc -m32 -O3 vectoradd.cu -o vectoradd

# try if the cuda sdk works at least
WORKDIR /sdk
RUN git clone https://github.com/Multi2Sim/m2s-bench-cudasdk-6.5.git /sdk
