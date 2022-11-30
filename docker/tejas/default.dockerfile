# FROM nvidia/cuda:11.8.0-devel-ubuntu22.04
# FROM --platform=linux/amd64 ocelot
FROM romnn/ocelot
# :23c195a97490

RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y \
  vim sudo python3-pip wget curl less \
  freeglut3-dev libxi-dev libxmu-dev

# wget https://www.cse.iitd.ac.in/tejas/gputejas/home_files/gputejas_installation_kit.tar.gz

# build gputejas
RUN mkdir -p /tmp/tejas && mkdir -p /app
WORKDIR /tmp/tejas
COPY ./docker/tejas/gputejas_installation_kit.tar.gz /tmp/tejas
RUN tar -xzvf gputejas_installation_kit.tar.gz && \
  mv ./gputejas/* /app/

WORKDIR /app
RUN rm -rf /tmp/tejas
RUN ls -lia

# compile tracegen
# RUN g++-4.8 -std=c++0x Tracegen.cpp -c -I .
# RUN g++-4.4 -o tracegen $bench_path/*.o Tracegen.o -locelot -ltinfo 

# ./tracegen $args $threadNum

# install java
RUN apt-get install -y ant openjdk-7-jdk
ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64/
RUN export JAVA_HOME

RUN cd gputejas && ant clean && ant && ant make-jar
