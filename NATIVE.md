## Native benchmarking

This guide describes how to run native profiling and generate SASS traces

- Clone repo

- Install pipenv virtual env

  ```bash
  pip install pipenv
  pipenv install --dev
  # activate the virtual environment in the current shell.
  pipenv shell
  ```

  If run on a DAS cluster node, install python packages locally and activate the CUDA toolkit:

  ```bash
  module load cuda11.2/toolkit
  pip3 install —user "pipenv==2022.4.8"
  pipenv install --skip-lock —dev
  ```

There are two ways to collect profiling and tracing data using docker or locally. Both options are described in the following sections.
To use the docker approach, docker must be installed on the GPU host and containers must be run with elevated privileges.

**Note**: All following steps assume your current working directory is the root of the git repository and you have activated the `pipenv` environment with `pipenv shell` or otherwise installed the required packages.

#### Option 1: Local

###### Native profiling

- Build `native` benchmarks

  ```bash
  make -C benchmarks clean
  make -C benchmarks native
  ```

- Run `native` benchmarks

  **Note**: Remember to set the correct config you are running with the `-c` flag.

  ```bash
  inv run -s native -c sm86_a4000 --run-dir ./your-run-dir
  ```

  When using the DAS cluster, you can submit slurm jobs using `--slurm` and specify the GPU to use with the `--slurm-node` flag:

  ```bash
  inv run -s native -c sm86_a4000 --slurm --slurm-node A4000 --run-dir ./your-run-dir
  ```

- Review results

  The results can be found in `./your-run-dir` and packaged into a tar archive for example.

###### SASS tracing

- Build `accelsim-sass` benchmarks:

  ```bash
  make -C benchmarks clean
  make -C benchmarks nvbit-tracer-tool
  make -C benchmarks accelsim
  ```

- Run `accelsim-sass` benchmarks

  **Note**: Remember to set the correct config you are running with the `-c` flag.

  ```bash
  inv run -s accelsim-sass -c sm86_a4000 --no-simulate --trace --run-dir ./your-run-dir
  ```

  When using the DAS cluster, you can submit slurm jobs using `--slurm` and specify the GPU to use with the `--slurm-node` flag:

  ```bash
  inv run -s accelsim-sass -c sm86_a4000 --slurm --slurm-node A4000 --no-simulate --trace --run-dir ./your-run-dir
  ```

- Review results

  The results can be found in `./your-run-dir` and packaged into a tar archive for example.


#### Option 2: Using docker

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
  inv bench -s native -c sm86_rtx3070
  inv bench -s accelsim-sass -c sm86_rtx3070 --no-simulate --trace
  ```

- Archive the results

  Finally, package the results into a tar archive. Because the resulting files are created by the user inside the docker container, this will ask for the sudo password to change the permissions of the results directory.

  ```bash
  inv pack
  ```
