FROM gpgpusims/accelsim-base

# build accelsim
# ENV CUDA_INSTALL_PATH=/usr/local/cuda
# RUN source /work/gpu-simulator/setup_environment.sh && \
#   make -C /work/gpu-simulator -j

# build rodinia_2.0-ft benchmarks
# make data should not do anything as we already have the data
RUN source /apps/src/setup_environment && \
  make -j -C /apps/src rodinia_2.0-ft && \
  make -j -C /apps/src data

COPY ./samples /samples
WORKDIR /samples

# hack: always skip the cache for the last command
ADD "https://www.random.org/cgi-bin/randbyte?nbytes=10&format=h" skipcache
RUN make -C /samples/vectoradd clean && \
  make -C /samples/vectoradd -j

RUN ldd /samples/vectoradd/vectoradd
