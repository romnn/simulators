# FROM nvidia/cuda:10.2-devel-ubuntu18.04
# m2s requires 32bit, which CUDA only supports up to version 8
FROM nvidia/cuda:8.0-devel-ubuntu14.04

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim wget curl zlib1g-dev

# multi2sim dependencies
RUN apt-get install -y zlib1g-dev lib32gcc1 gcc-multilib libgtk-3-dev

# install multi2sim
# RUN wget http://www.multi2sim.org/files/multi2sim-4.1.tar.gz && \

RUN git clone https://github.com/Multi2Sim/multi2sim.git /app
WORKDIR /app
RUN cd /app && \
  libtoolize && \
  aclocal && \
  autoconf && \
  automake --add-missing && \
  ./configure && make -j && make install

# add sample app
COPY ./samples /samples
WORKDIR /samples/vectoradd
RUN make clean && make -j

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
