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
