# FROM ubuntu:20.04
FROM nvidia/cuda:11.8.0-devel-ubuntu22.04

RUN apt-get update && apt-get -y upgrade
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt-get install -y \
  vim sudo python3-pip less ant \
  freeglut3-dev libxi-dev libxmu-dev

COPY ./Pipfile* /app/
WORKDIR /app/

RUN pip install pipenv
RUN pipenv install --dev --system

COPY ./gputejas /app/gputejas
COPY ./samples /app/samples

ENTRYPOINT [ "/bin/bash" ]
