# FROM nvidia/cuda:11.8.0-devel-ubuntu20.04
# FROM nvidia/cuda:10.2-devel-ubuntu18.04
FROM nvidia/cuda:10.1-devel-ubuntu18.04

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim

# install accelsim dependencies
RUN apt-get install -y \
  wget build-essential xutils-dev bison zlib1g-dev flex \
  libglu1-mesa-dev libssl-dev libxml2-dev libboost-all-dev \
  git g++ vim python-setuptools python3-dev python3-pip
RUN pip3 install pyyaml plotly psutil

# clone accelsim
RUN git clone https://github.com/accel-sim/accel-sim-framework.git /simulator
WORKDIR /simulator
RUN git checkout release
RUN ls -lia .

# build accelsim tracer
SHELL ["/bin/bash", "-c"]
ENV CUDA_INSTALL_PATH=/usr/local/cuda
RUN /simulator/util/tracer_nvbit/install_nvbit.sh && \
  ls -lia /simulator/util/tracer_nvbit/ && \
  make -j -C /simulator/util/tracer_nvbit

# build accelsim
RUN cd /simulator/gpu-simulator && source ./setup_environment.sh && make -j
RUN ls -lia .

# Get the pre-run trace files
# RUN rm -rf ./hw_run/rodinia_2.0-ft
# RUN wget https://engineering.purdue.edu/tgrogers/accel-sim/traces/tesla-v100/latest/rodinia_2.0-ft.tgz
# RUN mkdir -p ./hw_run
# RUN tar -xzvf rodinia_2.0-ft.tgz -C ./hw_run
# RUN rm rodinia_2.0-ft.tgz

# COPY ./samples /samples
# WORKDIR /samples

# # hack: always skip the cache for the last command
# ADD "https://www.random.org/cgi-bin/randbyte?nbytes=10&format=h" skipcache
# RUN source /work/gpu-simulator/gpgpu-sim/setup_environment && \
#   export LD_RUN_PATH=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10010/release/ && \
#   cd ./vectoradd && make clean && make -j

# RUN ldd /samples/vectoradd/vectoradd


# ###### END


# LD_PRELOAD=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10020/release/libcudart.so ./vectoradd

# Run the tests on the trace
# RUN source ./gpu-simulator/gpgpu-sim/setup_environment && \

# RUN source ./gpu-simulator/setup_environment.sh && \
#   ./util/job_launching/run_simulations.py \
#   -C QV100-SASS -B rodinia_2.0-ft -T ./hw_run/rodinia_2.0-ft/9.1 -N myTest

# # Wait for them to finish
# RUN source ./gpu-simulator/setup_environment.sh && \
#   ./util/job_launching/monitor_func_test.py -v -N myTest

# build the accelsim tracer
# RUN ./util/tracer_nvbit/install_nvbit.sh
# RUN make -j -C ./util/tracer_nvbit/

# clone benchmarks
# RUN git clone https://github.com/gpgpu-sim/gpgpu-sim_simulations.git /benchmarks
# WORKDIR /benchmarks
# RUN apt-get install -y libglew-dev scons freeglut3-dev
# RUN cd ./benchmarks/src && source ./setup_environment dev && make -j
