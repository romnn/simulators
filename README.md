## GPGPU simulators

Building and benchmarking architectural simulators for NVIDIA GPU's.

### Running benchmarks

In the following, we describe the procedure of running benchmarks. Due to different requirements for each simulator, we use docker containers to isolate the build and runtime environment of the simulators.

We recommend using the docker setup as it is more easily reproducible and tested in CI.
We also recommend using the provided `inv` tasks to build and benchmark containers as described in the following sections.

#### Option 1: Using docker containers (recommended)

- Prerequisites

  In the following, we assume a working docker environment. Disabling `buildkit` is not required but _may_ improve caching.

- Install and activate the provided python virtual environment.

  ```bash
  pip install pipenv
  pipenv install --dev
  # activate the virtual environment in the current shell.
  pipenv shell
  ```

  This will create a virtual environment with the required python packages and activate it in the current shell.
  To deactivate the environment, you can use `exit`.
  We recommend using this `pipenv` approach, however, installing the packages listed in the `Pipfile` using `pip install` is also possible.

- Build the docker containers

  On first run, both the `base` and `bench` containers for each simulator can be built using:

  ```bash
  inv build --base
  ```

  We use multi-stage docker builds and `inv build --base` will build all required containers in the correct order.
  To only build containers for a specific simulator, the `-s` flag can be used:

  ```bash
  inv build -s accelsim-ptx --base
  ```

  To see what docker commands will be run, you can use `--dry-run`. For more options, see `inv build --help`.

- _Optional_: Install `nvidia-container-toolkit`

  Benchmarking `native` or `accelsim-sass` require access to a native GPU device.
  Using `nvidia-container-toolkit` allows using the host GPU in a docker container and is assumed to be installed when running `inv bench -s native` or `inv bench -s accelsim-sass`.

  If not yet installed, you can install the container toolkit using the [instructions by nvidia](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html#setting-up-nvidia-container-toolkit) or use our convenience script:

  ```bash
  ./install-nvidia-container-toolkit.sh
  sudo apt-get update
  sudo apt-get install -y nvidia-docker2
  sudo systemctl restart docker
  # optional: check if the GPU is accessible
  sudo docker run --rm --gpus all nvidia/cuda:11.6.2-base-ubuntu20.04 nvidia-smi
  ```

- Run benchmarks

  To run the all benchmarks for all simulators and configurations, you can run:

  ```bash
  inv bench
  ```

  **Note**: Benchmarks are not run in parallel and this will take some time.

  To run specific simulators, configurations, and benchmarks, use the `-s`, `-c`, and `-b` flags respectively. Here are a few examples:

  ```bash
  # all benchmarks and configs for accelsim PTX
  inv bench -s accelsim-ptx
  # all configs for babelstream benchmark for tejas
  inv bench -s tejas -b babelstream
  # SM86 RTX 3070 config for babelstream benchmark for tejas
  inv bench -s tejas -b babelstream -c sm86_rtx3070
  ```

  Under the hood, the specified container is started, the `./run` directory is mapped into the container, and the run script is invoked (see `--dry-run`).

- Inspect results
  By default, the `inv bench` task maps the `./run` directory into the contianer to write the results to. To package only the results into a tar archive, we provide a task as well:
  ```bash
  inv pack
  ```

#### Option 2: Manual

**Note**: This approach is not recommended

- Compile the parsing tools

  ```bash
  inv build-tools
  ```

  which is equivalent to:

  ```bash
  rustup target add x86_64-unknown-linux-musl
  cargo install --release --all-targets --target x86_64-unknown-linux-musl
  ```

- Build the simulator

  Each simulator has very different build requirements.
  The best chance is to consult the `base.dockerfile` in `docker/<simulator>` of the simulator and try to follow the build procedure locally.

- Build the benchmarks

  Depending on your local simulator installation from the previous step, make sure to set all required environment variables used in `./benchmarks/<BENCHMARK_NAME>/Makefile` to match your local installation.

  ```bash
  export <ENV_VAR>=<VALUE>
  # build all benchmarks for the simulator
  make -C benchmarks <SIMULATOR>
  # build a specific benchmark
  make -C benchmarks/<BENCHMARK_NAME> <SIMULATOR>
  ```

- Run the benchmarks

  ```bash
  inv run -s <SIMULATOR> --run-dir ./run
  ```

  The `inv run` task is the command that is run inside the docker container in the docker setup and uses the same flags such as `-s`, `-b`, `-c`.
  For a list of all available options, see `inv run --help`

### Generating plots

t.b.a
