## Notes

##### Ocelot
original.dockerfile
```dockerfile
# wget http://security.ubuntu.com/ubuntu/pool/multiverse/n/nvidia-graphics-drivers-352/libcuda1-352_352.63-0ubuntu0.14.04.1_amd64.deb && \
# dpkg -i libcuda1-352_352.63-0ubuntu0.14.04.1_amd64.deb && \
# wget http://developer.download.nvidia.com/compute/cuda/4_2/rel/toolkit/cudatoolkit_4.2.9_linux_64_ubuntu11.04.run && \
```

##### MacSim
base.dockerfile
```dockerfile
# macsim dependencies
# python3-pip
# RUN apt-get install -y zlib1g-dev 
# python source build deps
# RUN apt-get install -y libssl-dev libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm libncurses5-dev xz-utils tk-dev libxml2-dev libxmlsec1-dev

# WORKDIR /install
# RUN wget https://www.python.org/ftp/python/3.6.3/Python-3.6.3.tgz && \
#   tar -I pigz -xf Python-3.6.3.tgz && \
#   cd Python-3.6.3 && \
#   ./configure --without-tests && \
#   make install -j

# RUN pip3 install --upgrade scons
# RUN pip3 install --upgrade setuptools wheel
# RUN pip3 install SCons==3.0.0
# RUN pip3 install --egg SCons==3.0.0

# RUN git clone https://github.com/gthparch/macsim.git /simulator
# ENV MACSIM_ROOT /simulator
# WORKDIR /simulator
# RUN cd /simulator && python3 ./build.py -j 16

# COPY --from=romnn/ocelot /usr/local/include/* /usr/local/include/
# COPY --from=romnn/ocelot /usr/local/bin/* /usr/local/bin/
# COPY --from=romnn/ocelot /usr/local/lib/* /usr/local/lib/
# COPY --from=romnn/ocelot /usr/lib/libboost_* /usr/lib/
# # COPY --from=romnn/ocelot /usr/lib/x86_64-linux-gnu/libGLEW* /usr/lib/x86_64-linux-gnu/
# # COPY --from=romnn/ocelot /usr/lib/x86_64-linux-gnu/libGLU* /usr/lib/x86_64-linux-gnu/
# COPY --from=romnn/ocelot /usr/lib/x86_64-linux-gnu/libGL* /usr/lib/x86_64-linux-gnu/


# /usr/local/lib
# libocelot.so       libocelotTrace.a   libocelotTrace.so

# --user
# /usr/bin/env python3 /usr/bin/scons
# git checkout multi2sim-kepler && \
# libtoolize && \
# aclocal && \
# autoconf && \
# automake --add-missing && \
# ./configure && make -j && make install && ldconfig
```

bench.dockerfile
```dockerfile
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

# warning: libGL.so.1, needed by //usr/lib/x86_64-linux-gnu/libGLEW.so.1.10
```

##### Multi2Sim

base.dockerfile
```dockerfile
# For CUDA 5.0: install older version of gcc (<4.7)
# RUN apt-get install -y gcc-4.6 g++-4.6 && \
#   update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.6 4 && \
#   update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-4.6 4 && \
#   update-alternatives --config gcc && \
#   update-alternatives --config g++
# RUN apt-get install -y lib32gcc1 gcc-4.6-multilib lib32g++ g++-4.6-multilib 

# COPY ./docker/assets/cudatoolkit_4.2.9_linux_32_ubuntu11.04.run /install_cuda.run
# COPY ./docker/assets/cuda_5.0.35_linux_64_ubuntu11.10-1.run /install_cuda.run
# RUN chmod +x /install_cuda.run && \
#   /install_cuda.run --nox11 && \
#   /usr/local/cuda/bin/nvcc --version

# install packages for 32 bit compilation
# RUN apt-get install -y lib32gcc1 gcc-multilib lib32g++ g++-multilib 
# gcc-4.7
```

bench.dockerfile
```dockerfile
# testing a modern CUDA version just for the includes
# using a newer cuda include results in all sorts of nasty errors when compiling with an older nvcc
# COPY ./docker/assets/cuda_9.0.176_384.81_linux.run /install_cuda.run
# RUN chmod +x /install_cuda.run && \
#   /install_cuda.run --silent --toolkit && \
#   if [ -f /var/log/cuda-installer.log ]; then cat /var/log/cuda-installer.log; fi && \
#   if [ -f /var/log/nvidia-installer.log ]; then cat /var/log/nvidia-installer.log; fi && \
#   find /tmp -name "cuda_install_*.log" | xargs cat && \
#   /usr/local/cuda-9.0/bin/nvcc --version
# # leave CUDA 6 the default cuda version
# RUN ln -vfns /usr/local/cuda-6.0 /usr/local/cuda

# try if the cuda sdk works at least
# WORKDIR /sdk
# RUN git clone https://github.com/Multi2Sim/m2s-bench-cudasdk-6.5.git /sdk
```

##### Native

base.dockerfile
```dockerfile
# install accelsim dependencies
# RUN apt-get install -y \
#   wget build-essential xutils-dev bison zlib1g-dev flex \
#   libglu1-mesa-dev libssl-dev libxml2-dev libboost-all-dev \
#   git g++ vim python-setuptools python3-dev python3-pip
# RUN pip3 install pyyaml plotly psutil numpy

# # clone accelsim
# RUN git clone https://github.com/accel-sim/accel-sim-framework.git /work && \
#   cd /work && git checkout release

# get sample apps
# RUN git clone https://github.com/accel-sim/gpu-app-collection /apps
# RUN apt-get install -y \
#   libxmu-dev libxmu-headers freeglut3-dev libglu1-mesa-dev \
#   ocl-icd-opencl-dev

# copy data required by the make data command to avoid downloading it
# COPY ./docker/accelsim/all.gpgpu-sim-app-data.tgz /apps/data_dirs/
# RUN tar -I pigz -xf /apps/data_dirs/all.gpgpu-sim-app-data.tgz -C /apps

# copy and install the gpucomputingsdk to avoid downloading it
# COPY ./docker/native/gpucomputingsdk_4.2.9_linux.run /apps/
# ENV CUDA_INSTALL_PATH=/usr/local/cuda
# ENV NVIDIA_COMPUTE_SDK_LOCATION=/apps/4.2
# RUN chmod u+x /apps/gpucomputingsdk_4.2.9_linux.run && \
#   /apps/gpucomputingsdk_4.2.9_linux.run -- \
#   --prefix="$NVIDIA_COMPUTE_SDK_LOCATION" \
#   --cudaprefix="$CUDA_INSTALL_PATH"
# RUN make -j -i -C "$NVIDIA_COMPUTE_SDK_LOCATION"

# note: building the benchmarks is left to avoid linking 
# the wrong libcudart when simulation should be used
```

##### AccelSim
```
# for SASS execution: the CUDA version should match the CUDA version of the HOST running the GPU
FROM nvidia/cuda:10.1-devel-ubuntu18.04
```

bench.ptx.dockerfile
```dockerfile
# ALL_LDFLAGS="--cudart shared" 
# RUN source $SIM_ROOT/setup_environment && \
  # export LD_RUN_PATH=$SIM_ROOT/lib/gcc-$CC_VERSION/cuda-$CUDA_VERSION_NUMBER/release/ && \
  # cd /benchmarks/matrixMul && make clean && make -j ALL_LDFLAGS="--cudart shared" && \
  # cd /benchmarks/matrixMul-modified && make clean && make -j ALL_LDFLAGS="--cudart shared"


# cd ./vectoradd && make clean && make -j
# export LD_RUN_PATH=/simulator/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10010/release/ && \
# RUN ldd /samples/vectoradd/vectoradd
```

base.dockerfile
```dockerfile
# && \
# /simulator/gpu-simulator/bin/release/accel-sim.out

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
```

##### Tejas

base.dockerfile
```dockerfile
# wget https://www.cse.iitd.ac.in/tejas/gputejas/home_files/gputejas_installation_kit.tar.gz
# COPY ./gputejas_installation_kit.tar.gz /tmp/tejas
```

bench.dockerfile
```dockerfile
# install python dependencies
# RUN apt-get install python3-pip && pip3 install pipenv
# RUN apt-get install -y software-properties-common && \
#   add-apt-repository -y ppa:deadsnakes/ppa && \
#   apt-get update && \
#   apt-get install -y python3.9 && \
#   which python3.9 && \
#   python3.9 -m ensurepip --default-pip --user && \
#   ln -sf /usr/bin/python3.9 /usr/bin/python3 && \
#   pip3 install pipenv
# RUN python -m ensurepip --default-pip --user && \ pip install pipenv
# WORKDIR /install
# RUN wget https://www.python.org/ftp/python/3.6.3/Python-3.6.3.tgz && \
#   tar -I pigz -xf Python-3.6.3.tgz && \
#   cd Python-3.6.3 && \
#   ./configure --without-tests && \
#   make install -j

# ./configure --enable-optimizations && \
# tar -xvf Python-3.6.3.tgz

# apt-get install -y python3.9-pip && \
# COPY ./Pipfile /
# COPY ./Pipfile* /
# COPY ./Pipfile.lock /
# RUN pip3 install pipenv && pipenv install --dev --system
# RUN pip3 install pipenv && pipenv install --dev --system
# RUN curl https://bootstrap.pypa.io/pip/2.7/get-pip.py --output get-pip.py && python2 get-pip.py
# RUN apt-get install -y python-pip && pip install pyyaml invoke pathlib

# ENTRYPOINT ["/bin/bash"]
# RUN inv run c SM6_GTX1080 -s accelsim --run-dir ./run

# RUN inv run -b matrixmul -c SM6_GTX1080 -s accelsim --run-dir ./run

# WORKDIR /benchmarks/matrixMul
# WORKDIR /benchmarks/matrixMul-modified

# COPY ./docker/tejas/inject-main.py /benchmarks/

# generate a trace
# WORKDIR /trace
# ENV CONFIG_PATH=/simulator/gputejas/src/simulator/config/config.xml
# RUN mkdir ./run && \
#   cp $TEJAS_ROOT/src/simulator/config/config.xml ./ && \
#   ls -lia ./

# cp /simulator/Tracegen.cpp ./ && \
# cp /simulator/configure.ocelot ./ && \
# WORKDIR /samples/vectoradd
# RUN export threadNum=$(grep -o '<MaxNumJavaThreads>.*</MaxNumJavaThreads>' ./config.xml | cut -d'<' -f 2 | cut -d'>' -f 2)
# RUN rm -rf $threadNum 2>/dev/null

# matrixMul.o
# g++-4.4 -o tracegen matrixMul.o Tracegen.o -L/simulator/so_files_64bit -L/simulator 
# -L/usr/local/cuda/lib64 -L/usr/lib/x86_64-linux-gnu/ 
# -locelot -ltinfo -Wl,-rpath-link=/simulator/so_files_64bit

# ENV INC="-I/simulator/so_files_64bit -I/simulator -I/usr/local/cuda/lib64 -I/usr/lib/x86_64-linux-gnu/"
# ENV LIBS="-L/simulator/so_files_64bit -L/simulator -L/usr/local/cuda/lib64 -L/usr/lib/x86_64-linux-gnu/"
# # RUN make clean && make vectoradd.o && \
# RUN make clean && make && \
#   g++-4.8 -std=c++0x /simulator/Tracegen.cpp -c -I /simulator/ && \
#   g++-4.4 -o tracegen vectoradd.o Tracegen.o \
#   $LIBS -locelot -ltinfo -Wl,-rpath-link=/simulator/so_files_64bit 

# jq '.executive.reconvergenceMechanism = "ipdom"' configure.ocelot

# launch completed successfully
# terminate called after throwing an instance of 'executive::RuntimeException'
# what():  barrier deadlock:
```
