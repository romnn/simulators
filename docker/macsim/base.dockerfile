# FROM ubuntu:18.04
# FROM ubuntu:18.04
# FROM nvidia/cuda:10.1-devel-ubuntu18.04
FROM romnn/ocelot

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim wget curl strace gdb pigz 

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

# copy in the static simulator binary
COPY --from=romnn/macsim-test /simulator/.opt_build/macsim /usr/bin/

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
