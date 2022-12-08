FROM romnn/m2s-base

# python for running the scripts
RUN apt-get install -y python3-pip && pip3 install pyyaml invoke pathlib

# add the tools
COPY ./target/x86_64-unknown-linux-musl/release/*-parse /usr/bin/

COPY ./benchmarks /benchmarks
WORKDIR /benchmarks

# try if the cuda sdk works at least
# WORKDIR /sdk
# RUN git clone https://github.com/Multi2Sim/m2s-bench-cudasdk-6.5.git /sdk

# compile the benchmarks
RUN cd /benchmarks && \
  make clean && \
  make -j m2s
