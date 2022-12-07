FROM romnn/tejas-base

# add benchmarks
COPY ./benchmarks /benchmarks 
WORKDIR /benchmarks
WORKDIR /benchmarks/matrixMul
WORKDIR /benchmarks/matrixMul-modified

# COPY ./docker/tejas/inject-main.py /benchmarks/

# generate a trace
# WORKDIR /trace
# ENV CONFIG_PATH=/simulator/gputejas/src/simulator/config/config.xml
RUN mkdir ./run && \
  cp $TEJAS_ROOT/src/simulator/config/config.xml ./ && \
  ls -lia ./

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
