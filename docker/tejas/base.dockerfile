FROM romnn/ocelot

SHELL ["/bin/bash", "-c"]

RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y \
  vim sudo python3-pip wget curl less pigz tree \
  freeglut3-dev libxi-dev libxmu-dev

# get gputejas
RUN mkdir -p /simulator
COPY ./cache /cache
RUN ls -lia /cache
RUN if [[ ! -f "/cache/gputejas_installation_kit.tar.gz" ]]; then \
  echo "downloading" && \
  wget --no-check-certificate \
  -O /cache/gputejas_installation_kit.tar.gz \
  https://www.cse.iitd.ac.in/tejas/gputejas/home_files/gputejas_installation_kit.tar.gz; fi

RUN tar -I pigz -xf /cache/gputejas_installation_kit.tar.gz -C /cache && \
  mv /cache/gputejas/* /simulator/ && \
  rm -rf /cache

# install java
RUN apt-get install -y ant openjdk-7-jdk
ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64/

# build gputejas
RUN cd /simulator/gputejas && \
  ant clean && \
  ant && \
  ant make-jar
ENV TEJAS_ROOT /simulator/gputejas
