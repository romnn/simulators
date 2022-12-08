FROM romnn/native-base

# python for running the scripts
RUN apt-get install -y python3-pip && pip3 install pyyaml invoke pathlib

# add the benchmarks
COPY ./benchmarks /benchmarks
WORKDIR /benchmarks

# compile the benchmarks
# ENV SIM_ROOT /simulator/gpu-simulator/gpgpu-sim
# RUN source $SIM_ROOT/setup_environment && \
RUN cd /benchmarks && \
  make clean && \
  make -j native
