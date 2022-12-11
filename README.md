## GPGPU Sims

#### Docker

#### TODO
- add macsim (using ocelot base image)
- add more benchmarks for all simulators

- match configs and document the mappings between simulators
- go though all the comments and try to remember the difficulties and things that did not work

- add larger inputs for all simulators once we have more benchmarks and do not care about runtime anymore

- make bar plots between simulators for matrixMul-modified
- make correlation plots between simulators for matrixMul-modified (and two inputs?)
- make correlation plots between simulators for multiple benchmarks (once we have them)


#### Done
- add trace driven accelsim
- copy unused stuff from the dockerfiles into NOTES.md
- rename the benchmarks to reflect the benchmarks versions they are from
- use individual folders for each input
- fix the results permissions after each benchmark run
- repeat the native benchmarks a few times
- integrate the rust parsers into python runners
- add native targets to the makefiles
- add python script that reads benchmark.yml and creates the run dirs with all the config and code
- use pylint and mypy and so on to at least somehow be sure the python code works
- add native dockerfiles

#### Building the rust tools

Using musl instead of glibc, we can run it on super old systems:
```bash
rustup target add x86_64-unknown-linux-musl
cargo build --release --all-targets --target x86_64-unknown-linux-musl

```
#### Building the containers

##### Simple API
```
# building
inv build -s native
inv build -s native --base

# benchmarking
# single simulator
inv bench -s native -c SM6_GTX1080
# all simulators
inv bench -c SM6_GTX1080
```

```bash
docker run -v "$PWD/tasks.py:/tasks.py" -v "$PWD/gpusims:/gpusims" -v "$PWD/run:/benchrun" romnn/tejas-bench inv run -c SM6_GTX1080 -s tejas --run-dir /benchrun

# we manually set the config name for correlation purposes
# however, for -s native the config files will not be copied to the run dir
docker run --cap-add SYS_ADMIN --privileged --gpus all -v "$PWD/tasks.py:/tasks.py" -v "$PWD/gpusims:/gpusims" -v "$PWD/run:/benchrun" romnn/tejas-bench inv run -c SM6_GTX1080 -s native --run-dir /benchrun

# fix permissions
sudo chown -R $(id -u):$(id -g) ./run/
sudo chmod -R 744 ./run/
```

###### Native
```bash
docker build -t romnn/native-base -f docker/native/base.dockerfile docker/native/
docker build . -t romnn/native-bench -f docker/native/bench.dockerfile
```
###### AccelSim
```bash
docker build -t romnn/accelsim-base -f docker/accelsim/base.dockerfile docker/accelsim/
docker build . -t romnn/accelsim-bench -f docker/accelsim/bench.dockerfile
```
###### Tejas
```bash
> docker build . -t romnn/ocelot -f docker/ocelot/original.dockerfile
docker build -t romnn/ocelot -f docker/ocelot/original.dockerfile docker/ocelot/
docker build -t romnn/tejas-base -f docker/tejas/base.dockerfile docker/tejas/
docker build . -t romnn/tejas-bench -f docker/tejas/bench.dockerfile
```
###### Multi2Sim
```bash
docker build -t romnn/m2s-base -f docker/m2s/base.dockerfile docker/m2s/
docker build . -t romnn/m2s-bench -f docker/m2s/bench.dockerfile
```

#### Benchmarks
```bash
source /simulator/gpu-simulator/gpgpu-sim/setup_environment
./matrixMul -wA=32 -hA=32 -wB=32 -hB=32
```


#### Running tools

```bash
cargo run --bin gpgpusim-parse
```

#### TODO (today)

#### Kepler traces
```
m2s --kpl-report ./test-report.txt --kpl-sim detailed ./vectorAdd_m2s
m2s --kpl-help |& tee kpl-help.txt
```

libm2s-cuda.so 
unsupported GNU version! gcc 4.7 and up are not supported!
`file /app/lib/.libs/libm2s-cuda.so.1.0.0` shows that indeed libm2s-cuda is 32bit

// there are some instructions for Multi2Sim kepler!!!!
https://github.com/Multi2Sim/m2s-bench-cudasdk-6.5

```
0xf7f38cb8 in cuda_stream_create () from /usr/local/lib/libm2s-cuda.so.1
(gdb) bt
#0  0xf7f38cb8 in cuda_stream_create () from /usr/local/lib/libm2s-cuda.so.1
#1  0xf7f37c8c in cuda_device_create () from /usr/local/lib/libm2s-cuda.so.1
#2  0xf7f2a7a4 in cuInit () from /usr/local/lib/libm2s-cuda.so.1
#3  0xf7f2f9cc in __cudaRegisterFatBinary () from /usr/local/lib/libm2s-cuda.so.1
#4  0x08049141 in __sti____cudaRegisterAll_44_tmpxft_000001bc_00000000_6_vectorAdd_cpp1_ii_a98933e8() ()
#5  0x08049202 in __libc_csu_init ()
#6  0xf7d8fa6a in __libc_start_main () from /lib32/libc.so.6
#7  0x080488a1 in _start ()
```

- concrete goal now:
  - refactor the rust tools into a library
  - module for each simulator
  - automate running a gpgpusim benchmark inside a docker container
  - get and parse the logs 

- move tool logic to lib and expose the functions as a python package?
- write a test harness for gpgpusim in rust that uses temp dirs
  - the configs should be structs
  - benchmarks should use builder pattern

- correlations work best for a full benchmark?
- make a correlation plot using the parsed stats and python

- write script to profile native app with nsight etc.
- make one simple correlation for ./vectoradd
  - between gpgpusim simulation and native execution

#### Done
- do not allow warnings and fix them
- gpgpu sim parser: use tuples as keys and write them to csv
- find out what scheduling systems are used by DAS5 and DAS6
  - since i cannot remember
  - => they use slurm and there even is slurm-rs which we should use

- update the proposal
- try to get simulation metrics from GPUtejas
  - need to trace an application first
  - the java part should be "easy"
  - but can it output structured statistics? lets hope so...


- write script to parse GPGPUsim statistics
  - ignore all the app data config stuff for now
  - we could have nested hash maps or just set a prefix and pass a reference

#### Current state

- multi2sim: compiling for CUDA 10 ubuntu 18
- gpuocelot: compiling in their old docker container for CUDA 4.2 ubuntu 14
- gpgpusim: compiling and correctly running 
- native: compiling and all benchmarks and exposing GPU
- tejas: should not be too difficult when we can use our ocelot base image?


#### Multi2Sim issues

GCC version:
```bash
g++ --version
g++ (Ubuntu 4.8.4-2ubuntu1~14.04.3) 4.8.4
Copyright (C) 2013 Free Software Foundation, Inc.
This is free software; see the source for copying conditions.  There is NO
warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
```

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

### TEJAS benchmarks 

```bash
./trace.sh <BENCHMARK ARGS> <THREADS>
./run.sh ./sim_run_stats_8
```

#### Native benchmarks

In the root dir, run:
```bash
docker build . -t romnn/native -f docker/native/native.dockerfile
sudo docker run --cap-add SYS_ADMIN --privileged --gpus all -it romnn/native

source /apps/src/setup_environment

# first try
/work/util/hw_stats/run_hw.py -D 0 -B rodinia_2.0-ft -R 1

# device 0, benchmark ridinia, 1 repetition, using the new nsight_profiler, 
# collecting all available metrics
/work/util/hw_stats/run_hw.py -D 0 -B rodinia_2.0-ft -R 1 --nsight_profiler --collect="cycles,other_stats"

# ==> issue:
Note: nv-nsight-cu-cli wrapper is deprecated in favor of ncu and will be removed in a future version.
PARSEC Benchmark Suite
read 1024 points
==PROF== Connected to process 827 (/apps/bin/10.1/release/streamcluster-rodinia-2.0-ft)
==ERROR== Profiling is not supported on device 0. To find out supported GPUs refer --list-chips option.

# best solution:
/work/util/hw_stats/run_hw.py -D 0 -B rodinia_2.0-ft -R 1 --collect="cycles,other_stats"

# look at all the files
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
