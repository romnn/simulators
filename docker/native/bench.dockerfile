FROM romnn/native-base

# python for running the scripts
RUN apt-get install -y python3-pip && \
  pip3 install pyyaml invoke pathlib psutil==5.0.0

# add the tools
COPY --from=romnn/tools /tools/target/x86_64-unknown-linux-musl/release/*-parse /usr/bin/

# add the benchmarks
COPY ./benchmarks /benchmarks
WORKDIR /benchmarks

# compile the benchmarks
RUN cd /benchmarks && \
  make clean && \
  make native
