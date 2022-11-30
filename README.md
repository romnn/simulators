
#### Docker

#### Running tools

```bash
cargo run --bin gpgpusim-parse
```

#### Current state

- multi2sim: compiling for CUDA 10 ubuntu 18
- gpuocelot: compiling in their old docker container for CUDA 4.2 ubuntu 14
- gpgpusim: compiling and correctly running 
- native: compiling and all benchmarks and exposing GPU
- tejas: should not be too difficult when we can use our ocelot base image?


#### Git submodule diff

```bash
git diff --submodule=diff
```

#### TODO

- inspect and run the `run_hw.py` script of accelsim.
  - understand how it works and write our own version of it

- inspect `run_simulations.py` script of accelsim
  - understand how it gets its metrics and where they are saved

Save docker images to disk (note: this actually takes a long time)

```bash
apt-get list -a <package>
```

```bash
docker save myimage:latest | gzip > myimage_latest.tar.gz

# example
docker pull nvidia/cuda:11.8.0-devel-ubuntu20.04
docker save nvidia/cuda:11.8.0-devel-ubuntu20.04 | gzip > ./images/cuda/nvidia_cuda_11.8.0-devel-ubuntu20.04.tar.gz
```

```bash
docker build --load --progress plain . -t accelsim -f docker/accelsim/default.dockerfile
```

#### GPU in docker container

https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html#docker
```bash
./install-nvidia-container-toolkit.sh
sudo apt-get update
sudo apt-get install -y nvidia-docker2
sudo systemctl restart docker
```

#### Update buildx

```bash
mkdir -p mkdir ~/.docker/cli-plugins
wget -O ~/.docker/cli-plugins/docker-buildx https://github.com/docker/buildx/releases/download/v0.9.1/buildx-v0.9.1.linux-amd64
chmod +x ~/.docker/cli-plugins/docker-buildx
docker buildx version
```

#### Disable docker buildkit

```bash
sudo vim /etc/docker/daemon.json
# add the following:
# "features": {
#   "buildkit": false
# }

# removed from ~/docker/config.json
# "aliases": {
#   "builder": "buildx"
# }
sudo systemctl restart docker

```

#### Native benchmarks

In the root dir, run:
```bash
docker build . --build-arg mode=hw -t romnn/native -f docker/native/default.dockerfile
sudo docker run --cap-add SYS_ADMIN --privileged --gpus all -it romnn/native

source /apps/src/setup_environment
/work/util/hw_stats/run_hw.py -D 0 -B rodinia_2.0-ft -R 1
tree /work/hw_run/device-0/
```

#### Simulation benchmarks

```bash
docker build . --build-arg mode=sim -t romnn/native -f docker/native/default.dockerfile
docker run -it romnn/native

source /apps/src/setup_environment
source /work/gpu-simulator/gpgpu-sim/setup_environment
make -j -C /apps/src rodinia_2.0-ft 
/work/util/job_launching/run_simulations.py -B rodinia_2.0-ft -C QV100-PTX  -N rodinia-ptx
/work/util/job_launching/monitor_func_test.py -N rodinia-ptx

/work/util/job_launching/get_stats.py -R -K -k -B rodinia_2.0-ft -C QV100-PTX > correl.stats.csv
./plot-correlation.py -c correl.stats.csv

./util/job_launching/get_stats.py -R -C QV100-SASS,QV100-PTX -B rodinia_2.0-ft | tee per-app-stats.csv
./util/plot-get-stats.py -c per-app-stats.csv
docker cp fdadff90c6c9:/work/sim_run_10.2 ./analyze/
```

#### libcuda.so.10.2 cannot be found at runtime

even though `ldd ./vectoradd` shows the gpgpgusim libcudart when forced to link it using `LD_RUN_PATH` or `LD_PRELOAD`.

The so version file in the repo seems to not be used anywhere but the so file can still only be used for certain versions of CUDA (10.1 but not 10.2) even when we create a ln -s symbolic link just like the ones for the other versions in the Makefile.

However, this is also happens when `setup_environment.sh` has not been called in the shell running the program at runtime!


#### Nsight compute in docker containers

article: https://developer.nvidia.com/blog/using-nsight-compute-in-containers/
```bash

```

#### Failed to prune PTX section list

Interesting detail: `CUOBJDUMP_SIM_FILE` is only needed with `LD_PRELOAD` it seems...
When recompiling with `--cudart shared` and CUDA is actually found, it seems to work.

```bash
CUOBJDUMP_SIM_FILE=on LD_PRELOAD=/work/gpu-simulator/gpgpu-sim/lib/gcc-7.5.0/cuda-10010/release/libcudart.so ./vectoradd
```

#### Can GPGPU-sim output statistics to a file?

```bash
$ head -n 300 analyze/sim_output.txt | grep file

-save_embedded_ptx                      0 # saves ptx files embedded in binary as <n>.ptx
-keep                                   0 # keep intermediate files created by GPGPU-Sim when interfacing with external programs
-gpgpu_ptx_save_converted_ptxplus                    0 # Saved converted ptxplus to a file
-inter_config_file                   mesh # Interconnection network config file
-gpgpu_ptx_inst_debug_to_file                    0 # Dump executed instructions' debug information to file
-gpgpu_ptx_inst_debug_file       inst_debug.txt # Executed instructions' debug output file
-n_regfile_gating_group                    4 # group of lanes that should be read/written together)
-gpgpu_clock_gated_reg_file                    0 # enable clock gated reg file for power calculations
-gpgpu_reg_file_port_throughput                    2 # the number ports of the register file
-accelwattch_xml_file accelwattch_sass_sim.xml # AccelWattch XML file
-hw_perf_file_name            hw_perf.csv # Hardware Performance Statistics file
-hw_perf_bench_name                       # Kernel Name in Hardware Performance Statistics file
-power_trace_enabled                    0 # produce a file for the power trace (1=On, 0=Off)
-steady_power_levels_enabled                    0 # produce a file for the steady power levels (1=On, 0=Off)
-visualizer_outputfile                 NULL # Specifies the output log file for visualizer
-enable_ptx_file_line_stats                    1 # Turn on PTX source line statistic profiling. (1 = On)
-ptx_line_stats_filename gpgpu_inst_stats.txt # Output file for PTX source line statistics.
Extracting PTX file and ptxas options    1: vectoradd.1.sm_30.ptx -arch=sm_30
GPGPU-Sim PTX: __cudaRegisterFatBinary, fat_cubin_handle = 1, filename=default
Extracting specific PTX file named vectoradd.1.sm_30.ptx
GPGPU-Sim PTX: finished parsing EMBEDDED .ptx file vectoradd.1.sm_30.ptx
```
