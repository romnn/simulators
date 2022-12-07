FROM romnn/ocelot

RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y \
  vim sudo python3-pip wget curl less pigz tree \
  freeglut3-dev libxi-dev libxmu-dev

# get gputejas
# wget https://www.cse.iitd.ac.in/tejas/gputejas/home_files/gputejas_installation_kit.tar.gz
RUN mkdir -p /tmp/tejas && mkdir -p /simulator
COPY ./gputejas_installation_kit.tar.gz /tmp/tejas
RUN tar -I pigz -xf /tmp/tejas/gputejas_installation_kit.tar.gz -C /tmp/tejas && \
  mv /tmp/tejas/gputejas/* /simulator/ && \
  rm -rf /tmp/tejas

# install java
RUN apt-get install -y ant openjdk-7-jdk
ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64/

# build gputejas
RUN cd /simulator/gputejas && \
  ant clean && \
  ant && \
  ant make-jar
ENV TEJAS_ROOT /simulator/gputejas
