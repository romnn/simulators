FROM romnn/macsim-base

# python for running the scripts
RUN apt-get install -y python3-pip && pip3 install pyyaml invoke pathlib
# RUN apt-get install -y libboost-all-dev libglu1-mesa-dev
# RUN apt-get install -y libglu1-mesa-dev

# apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 0C69C6EC64E1C6DA && \
# printf "deb http://ppa.launchpad.net/hparch/gpuocelot/ubuntu trusty main" \
# > /etc/apt/sources.list.d/hparch-gpuocelot-trusty.list && \

# Avoid ERROR: invoke-rc.d: policy-rc.d denied execution of start.
# RUN echo "#!/bin/sh\nexit 0" > /usr/sbin/policy-rc.d && \
#     apt-get update && apt-get install -y \
#     freeglut3-dev \
#     g++-4.8 g++-4.6 g++-4.4 \
#     libboost1.49-all-dev libboost1.49-dev libboost-mpi-python1.49-dev libboost-mpi-python1.49.0 \
#     libglew-dev \
#     libxi-dev \
#     libXmu-dev

# add the tools
COPY ./target/x86_64-unknown-linux-musl/release/*-parse /usr/bin/

# add the benchmarks
COPY ./benchmarks /benchmarks 
WORKDIR /benchmarks

# we need bash shell to allow python to exectute and read cmd outputs with Popen during build 
SHELL ["/bin/bash", "-c"]

# compile the benchmarks
RUN cd /benchmarks && \
  make clean && \
  make -j macsim

# warning: libGL.so.1, needed by //usr/lib/x86_64-linux-gnu/libGLEW.so.1.10
