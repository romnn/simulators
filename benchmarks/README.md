#### Benchmarks

You can find the configuration options for the simulator [here](https://github.com/CoffeeBeforeArch/gpgpu-sim_distribution/blob/dev/src/gpgpu-sim/gpu-sim.cc).

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
