FROM romnn/tejas-base

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

# python for running the scripts
RUN apt-get install -y python3-pip && pip3 install pyyaml invoke pathlib

# add the python scripts
# COPY ./tasks.py /tasks.py
# COPY ./gpusims /gpusims
# WORKDIR /gpusims

# add the benchmarks
COPY ./benchmarks /benchmarks 
WORKDIR /benchmarks

# compile the benchmarks
RUN cd /benchmarks && \
  make clean && \
  make -j tejas

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
