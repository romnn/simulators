package main

import (
    // "flag"

    // "gitlab.com/akita/mgpusim/v3/benchmarks/heteromark/fir"
    "gitlab.com/akita/mgpusim/v3/driver"
    "gitlab.com/akita/mgpusim/v3/samples/runner"
)

// openCL kernel example
// https://gitlab.com/akita/mgpusim/-/tree/v3/benchmarks/amdappsdk/simpleconvolution

func main() {
    var (
      gpuDriver *driver.Driver
      context   *driver.Context
      gpuIDs    []int
      // comms     []*mccl.Communicator
    )


    platform := runner.MakeR9NanoBuilder().
			WithNumGPU(4).
			Build()
		gpuDriver = platform.Driver
		gpuDriver.Run()
    defer gpuDriver.Terminate()

		context = gpuDriver.Init()

    gpuNum := 4
		dataSize := 1024
		root := 1
		datas := make([]driver.Ptr, gpuNum)

		for i := 0; i < gpuNum; i++ {
			gpuDriver.SelectGPU(context, i+1)
			data := gpuDriver.AllocateMemory(context, uint64(dataSize*4))
			gpuIDs = append(gpuIDs, i+1)
			datas[i] = data
		}

    // runner := runner.Runner{}
    // runner.Init()

    // benchmark := fir.NewBenchmark(runner.GPUDriver)
    // benchmark.Length = 4096
    // runner.Benchmark = benchmark
    // runner.Run()
}

