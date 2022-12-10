FROM ubuntu:14.04

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
RUN git clone https://github.com/Multi2Sim/multi2sim.git /simulator
ENV M2S_ROOT /simulator
WORKDIR /simulator
RUN cd /simulator && \
  git checkout multi2sim-kepler && \
  libtoolize && \
  aclocal && \
  autoconf && \
  automake --add-missing && \
  ./configure && make -j && make install && ldconfig

# install CUDA 6.0 (CUDA 8+ does not support 32bit compilation)
# COPY ./docker/assets/cuda_8.0.61_375.26_linux.run /install_cuda.run
# COPY ./docker/assets/cuda_7.0.28_linux.run /install_cuda.run
# COPY ./docker/assets/cuda_5.0.35_linux_64_ubuntu11.10-1.run /install_cuda.run
COPY ./cuda_6.0.37_linux_64.run /install_cuda.run
RUN chmod +x /install_cuda.run && \
  /install_cuda.run --silent --toolkit --override && \
  if [ -f /var/log/cuda-installer.log ]; then cat /var/log/cuda-installer.log; fi && \
  if [ -f /var/log/nvidia-installer.log ]; then cat /var/log/nvidia-installer.log; fi && \
  find /tmp -name "cuda_install_*.log" | xargs cat && \
  /usr/local/cuda/bin/nvcc --version

ENV PATH /usr/local/cuda/bin:$PATH
