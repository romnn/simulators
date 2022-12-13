#### Benchmarks

#### Configs

we inject configurations for each simulator
we try to match config to gpgpusim
we provide a script to template configurations to match the gpgpusim config as close as possible
we use GPGPUSim's [tested-configs](https://github.com/accel-sim/gpgpu-sim_distribution/tree/dev/configs/tested-cfgs) as our baselines.

```bash
inv configure \
  --simulator tejas \
  --base ./configs/SM6_GTX1080/gpgpusim.config \
  --template ./configs/tejas_default_config.xml \
  --out ./configs/SM6_GTX1080/tejas_config.xml
```

You can find configuration options for GPGPUSim in [gpu-sim.cc](https://github.com/CoffeeBeforeArch/gpgpu-sim_distribution/blob/dev/src/gpgpu-sim/gpu-sim.cc), or just read through `log.txt` files, as all the configuration options with a description are printed.

-gpgpu_l2_rop_latency
-gpgpu_gmem_skip_L1D
-accelwattch_xml_file

# ADD "https://www.random.org/cgi-bin/randbyte?nbytes=10&format=h" skipcache
##### Host
```bash
make HOST_COMPILER=g++-7
```

##### Accelsim
```
make -B ALL_LDFLAGS="--cudart shared"
# issue: Attribute number 20 unimplemented (the driver api is not supported)
#   cudaDevAttrComputeMode = 20
#   this is called in helper_cuda.h gpuGetMaxGlopsDeviceId()
#   remedy: lets just use device 0 always
# issue: symbol cuProfilerStart not found (the driver api does not support it)
# => remedy: we need an older CUDA matrixMul implementation? or change it ourself
```

```
[CUDA Bandwidth Test] - Starting...
Usage:  bandwidthTest [OPTION]...
Test the bandwidth for device to host, host to device, and device to device transfers

Example:  measure the bandwidth of device to host pinned memory copies in the range 1024 Bytes to 102400 Bytes in 1024 Byte increments
./bandwidthTest --memory=pinned --mode=range --start=1024 --end=102400 --increment=1024 --dtoh

Options:
--help  Display this help menu
--csv   Print results as a CSV
--device=[deviceno]     Specify the device device to be used
  all - compute cumulative bandwidth on all the devices
  0,1,2,...,n - Specify any particular device to be used
--memory=[MEMMODE]      Specify which memory mode to use
  pageable - pageable memory
  pinned   - non-pageable system memory
--mode=[MODE]   Specify the mode to use
  quick - performs a quick measurement
  range - measures a user-specified range of values
  shmoo - performs an intense shmoo of a large range of values
--htod  Measure host to device transfers
--dtoh  Measure device to host transfers
--dtod  Measure device to device transfers
--wc    Allocate pinned memory as write-combined
--cputiming     Force CPU-based timing always
Range mode options
--start=[SIZE]  Starting transfer size in bytes
--end=[SIZE]    Ending transfer size in bytes
--increment=[SIZE]      Increment size in bytes
Result = PASS
```
