FROM nvidia/cuda:10.1-devel-ubuntu18.04

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim wget curl strace gdb pigz 

RUN apt-get install -y zlib1g-dev python3-pip
RUN pip3 install --upgrade scons

RUN git clone https://github.com/gthparch/macsim.git /simulator
ENV MACSIM_ROOT /simulator
WORKDIR /simulator
RUN cd /simulator && python3 ./build.py -j 16
