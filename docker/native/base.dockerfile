FROM nvidia/cuda:10.1-devel-ubuntu18.04

SHELL ["/bin/bash", "-c"]

# install basic packages
RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y build-essential git less vim tree wget curl pigz

# install nsight compute for hardware profiling using performance counters
# ref: https://developer.nvidia.com/blog/using-nsight-compute-in-containers/
# search for versions: apt-cache search nsight-compute
# RUN apt-get install -y nsight-compute-2022.3.0
COPY ./nsight-compute-2022.3.0_2022.3.0.22-1_amd64.deb /deps/
RUN apt-get install -y /deps/nsight-compute-2022.3.0_2022.3.0.22-1_amd64.deb
ENV PATH="$PATH:/opt/nvidia/nsight-compute/2022.3.0"
