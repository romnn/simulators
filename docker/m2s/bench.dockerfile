FROM romnn/m2s-base

# python for running the scripts
RUN apt-get install -y python3-pip && pip3 install pyyaml invoke pathlib

# add the tools
COPY ./target/x86_64-unknown-linux-musl/release/*-parse /usr/bin/

COPY ./benchmarks /benchmarks
WORKDIR /benchmarks

# compile the benchmarks
RUN cd /benchmarks && \
  make clean && \
  make -j m2s
