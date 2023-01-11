FROM gpgpusims/m2s-base

# python for running the scripts
RUN apt-get install -y python3-pip && \
  pip3 install pyyaml invoke pathlib psutil==5.0.0

# add the tools
COPY --from=gpgpusims/tools /tools/target/x86_64-unknown-linux-musl/release/*-parse /usr/bin/

COPY ./benchmarks /benchmarks
WORKDIR /benchmarks

# compile the benchmarks
RUN cd /benchmarks && \
  make clean && \
  make m2s
