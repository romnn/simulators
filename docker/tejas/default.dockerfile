FROM romnn/ocelot

RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y \
  vim sudo python3-pip wget curl less pigz tree \
  freeglut3-dev libxi-dev libxmu-dev

# wget https://www.cse.iitd.ac.in/tejas/gputejas/home_files/gputejas_installation_kit.tar.gz

# get gputejas
RUN mkdir -p /tmp/tejas && mkdir -p /app
COPY ./docker/tejas/gputejas_installation_kit.tar.gz /tmp/tejas
RUN tar -I pigz -xf /tmp/tejas/gputejas_installation_kit.tar.gz -C /tmp/tejas && \
  mv /tmp/tejas/gputejas/* /app/ && \
  rm -rf /tmp/tejas


# install java
RUN apt-get install -y ant openjdk-7-jdk
ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64/
# RUN export JAVA_HOME

# build gputejas
RUN cd /app/gputejas && \
  ant clean && \
  ant && \
  ant make-jar

# add sample app
COPY ./samples /samples
WORKDIR /samples/vectoradd_tejas

# generate a trace
# WORKDIR /trace
# ENV CONFIG_PATH=/app/gputejas/src/simulator/config/config.xml
RUN mkdir ./run && \
  cp /app/gputejas/src/simulator/config/config.xml ./ && \
  cp /app/Tracegen.cpp ./ && \
  ls -lia ./

# cp /app/configure.ocelot ./ && \
# WORKDIR /samples/vectoradd
# RUN export threadNum=$(grep -o '<MaxNumJavaThreads>.*</MaxNumJavaThreads>' ./config.xml | cut -d'<' -f 2 | cut -d'>' -f 2)
# RUN rm -rf $threadNum 2>/dev/null
ENV INC="-I/app/so_files_64bit -I/app -I/usr/local/cuda/lib64 -I/usr/lib/x86_64-linux-gnu/"
ENV LIBS="-L/app/so_files_64bit -L/app -L/usr/local/cuda/lib64 -L/usr/lib/x86_64-linux-gnu/"
RUN make clean && make vectoradd.o && \
  g++-4.8 -std=c++0x Tracegen.cpp -c -I /app/ && \
  g++-4.4 -o tracegen vectoradd.o Tracegen.o \
  $LIBS -locelot -ltinfo -Wl,-rpath-link=/app/so_files_64bit 

# jq '.executive.reconvergenceMechanism = "ipdom"' configure.ocelot

# launch completed successfully
# terminate called after throwing an instance of 'executive::RuntimeException'
# what():  barrier deadlock:
