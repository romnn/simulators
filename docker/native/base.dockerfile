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
# COPY ./nsight-compute-2022.3.0_2022.3.0.22-1_amd64.deb /deps/
COPY ./cache /cache
RUN ls -lia /cache
RUN if [[ -e "/cache/nsight-compute-2022.3.0_2022.3.0.22-1_amd64.deb" ]]; \
  then echo "using cache" && apt-get install -y /cache/nsight-compute-2022.3.0_2022.3.0.22-1_amd64.deb; \
  else echo "downloading" && apt-get install -y nsight-compute-2022.3.0; fi
ENV PATH="$PATH:/opt/nvidia/nsight-compute/2022.3.0"
