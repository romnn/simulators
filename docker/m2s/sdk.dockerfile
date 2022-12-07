# we still need nvcc, but only nvcc
FROM ubuntu:14.04 as NVCC
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim wget curl zlib1g-dev

COPY ./docker/assets/cuda_5.0.35_linux_64_ubuntu11.10-1.run /install_cuda.run
RUN chmod +x /install_cuda.run && \
  /install_cuda.run --silent --toolkit --override && \
  /usr/local/cuda/bin/nvcc --version

FROM ubuntu:14.04

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim wget curl zlib1g-dev

# multi2sim dependencies
RUN apt-get install -y \
  zlib1g-dev lib32gcc1 gcc-multilib libgtk-3-dev \
  libtool autotools-dev autoconf automake

# install multi2sim
# this needs gcc >= 4.8 to support C++11
# RUN wget http://www.multi2sim.org/files/multi2sim-4.1.tar.gz && \
RUN git clone https://github.com/Multi2Sim/multi2sim.git /app
WORKDIR /app
RUN cd /app && \
  libtoolize && \
  aclocal && \
  autoconf && \
  automake --add-missing && \
  ./configure && make -j && make install && ldconfig

# try if the cuda sdk works at least
WORKDIR /sdk
RUN git clone https://github.com/Multi2Sim/m2s-bench-cudasdk-6.5.git /sdk

# copy only binary tools
RUN apt-get install -y gcc-multilib g++-multilib lib32gcc1 lib32g++
RUN mkdir -p /usr/local/cuda/
COPY --from=NVCC /usr/local/cuda/bin /usr/local/cuda/bin
ENV PATH /usr/local/cuda/bin:$PATH
