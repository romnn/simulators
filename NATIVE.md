## Native benchmarking

Native profiling and SASS benchmarking SASS

- Clone repo

- Install pipenv virtual env

  ```bash
  pip install pipenv
  pipenv install --dev
  # activate the virtual environment in the current shell.
  pipenv shell
  ```

- Install docker

- Build containers

  It might be necessary to match the CUDA version of the docker images to with the local CUDA version before building.
  The local CUDA version can be checked using `nvidia-smi`.
  The relevant base images in `./docker/accelsim/base.dockerfile` and `./docker/native/base.dockerfile` should be changed to `nvidia/cuda:<CUDA_VERSION>-devel-ubuntu18.04`. A list of available `nvidia/cuda` images is [available on Docker Hub](https://hub.docker.com/r/nvidia/cuda/tags).

  ```bash
  inv build -s accelsim-sass -s native --base
  ```

- Install `nvidia-container-toolkit`

  Using `nvidia-container-toolkit` allows access to the host GPU in a docker container and is assumed to be installed when running `inv bench -s native` or `inv bench -s accelsim-sass`.

  If not yet installed, you can install the container toolkit using the [instructions by nvidia](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html#setting-up-nvidia-container-toolkit) or use our convenience script:

  ```bash
  ./install-nvidia-container-toolkit.sh
  sudo apt-get update
  sudo apt-get install -y nvidia-docker2
  sudo systemctl restart docker
  # optional: check if the GPU is accessible
  sudo docker run --rm --gpus all nvidia/cuda:<CUDA_VERSION>-devel-ubuntu18.04 nvidia-smi
  ```

- Run benchmarks

  ```bash
  inv bench -s accelsim-sass -s native -c sm86_rtx3070
  ```

- Archive the results

  Finally, package the results into a tar archive. Because the resulting files are created by the user inside the docker container, this will ask for the sudo password to change the permissions of the results directory.

  ```bash
  inv pack
  ```
