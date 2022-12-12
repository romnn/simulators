## Config matching

#### Native hardware

###### GTX 1080
https://www.techpowerup.com/gpu-specs/geforce-gtx-1080.c2839

#### MacSim <=> GPGPUsim
Occupancy should be computed using `gpgpu_occupancy_sm_number`.

```txt
num_sim_cores 16 = gpgpu_n_clusters (* -gpgpu_n_cores_per_cluster)
num_sim_small_cores 16 = gpgpu_n_clusters (* -gpgpu_n_cores_per_cluster)
max_threads_per_core 80 = gpgpu_shader_core_pipeline [0]

clock_gpu 1.6 = int(gpgpu_clock_domains<Core Clock>/ 1000)
# i guess this is the memory controller
clock_mc  2.1 = int(gpgpu_clock_domains<DRAM Clock> / 1000)
# what are clock_llc 1.6 and clock_noc 1.6 (last level cache? and what?)
clock_llc  1.6 = int(gpgpu_clock_domains<L2 Clock> / 1000)
clock_noc  1.6 = int(gpgpu_clock_domains<Interconnect Clock> / 1000)

gpgpu_const_cache:l1 N:128:64:2,L:R:f:N:L,A:2:64,4
<nsets>:<bsize>:<assoc>,<rep>:<wr>:<alloc>:<wr_alloc>,<mshr>:<N>:<merge>,<mq>
# 4 KB Inst.
<nsets>:<bsize>:<assoc>,<rep>:<wr>:<alloc>:<wr_alloc>,<mshr>:<N>:<merge>,<mq>
-gpgpu_cache:il1 N:8:128:4,L:R:f:N:L,S:2:48,4
4 KB =? 8 * 128 * 4 correct

# 12 KB Const
<nsets>:<bsize>:<assoc>,<rep>:<wr>:<alloc>:<wr_alloc>,<mshr>:<N>:<merge>,<mq>
-gpgpu_const_cache:l1 N:128:64:2,L:R:f:N:L,S:2:64,4
12 KB = 128*64*2 = 16KB (sort of correct)

-gpgpu_cache:dl2 S:64:128:16,L:B:m:L:L,A:256:64,16:0,32
3MB = 64*128*16 * gpgpu_n_mem * gpgpu_n_sub_partition_per_mchannel

-gpgpu_tex_cache:l1 N:16:128:24,L:R:m:N:L,T:128:4,128:2
48 KB = 16*128*24 correct

# Memory
const_cache_size 8192 = gpgpu_const_cache:l1<nsets>*<bsize>*<assoc>
texture_cache_size 8192 = gpgpu_tex_cache:l1<nsets>*<bsize>*<assoc>
# note: this is per SM 768KB*1000 / 512shading units = 1500
shared_mem_size 16384 = -gpgpu_shmem_size_PrefShared or -gpgpu_shmem_sizeDefault
shared_mem_banks 32 = gpgpu_shmem_num_banks
shared_mem_cycles 2 = gpgpu_smem_latency
shared_mem_ports 1 = gpgpu_mem_unit_ports

# l1 cache
l1_small_line_size 128 = gpgpu_const_cache:l1<bsize>
l1_small_num_set 64 = gpgpu_const_cache:l1<nsets>
l1_small_assoc 6 = gpgpu_const_cache:l1<assoc>

# l2 cache
num_llc 6
llc_num_set 128 = gpgpu_cache:dl2<nsets>
llc_line_size 128 = gpgpu_cache:dl2<bsize>
llc_assoc 8 = gpgpu_cache:dl2<assoc>
llc_num_bank 4 = gpgpu_cache:dl2<N> but how to parse that??
llc_latency 10 

# DRAM
dram_num_mc 6
# for NVIDIA TITAN X, bus width is 384bits (12 DRAM chips x 32 bits)
# 12 memory paritions, 4 bytes (1 DRAM chip) per memory partition
# the atom size of GDDR5X (the smallest read request) is 32 bytes
dram_bus_width 8 = gpgpu_dram_buswidth * gpgpu_n_sub_partition_per_mchannel ? # default = 4 bytes (8 bytes per cycle at DDR)
dram_column 11
dram_activate 25 = gpgpu_dram_timing_opt<RAS>
dram_precharge 10 = gpgpu_dram_timing_opt<RP>
dram_num_banks 16 = gpgpu_dram_timing_opt<nbk>
dram_num_channel 1 = gpgpu_dram_timing_opt<nbkgrp> ? not sure
dram_rowbuffer_size 2048
dram_scheduling_policy FRFCFS

num_warp_scheduler 2 = -gpgpu_num_sched_per_core
```

#### Multi2Sim <=> GPGPUsim

```bash
--kpl-config <file> --kpl-sim detailed
```

Kepler reference [config](https://github.com/accel-sim/gpgpu-sim_distribution/blob/dev/configs/tested-cfgs/SM3_KEPLER_TITAN/gpgpusim.config).

```ini
# parameters for the GPU.
[ Device ]
# Frequency for the Kepler GPU in MHz.
Frequency = <value> (Default = 745) -gpgpu_clock_domains[0]

# Number of streaming multiprocessor in the GPU.
NumSMs = <num> (Default = 15) -gpgpu_n_clusters (* -gpgpu_n_cores_per_cluster)

# parameters for the Streaming multiprocessors.
[ SM ]
# Number of warp pools per SM.
NumWarpPools = <num> (Default = 4) -gpgpu_num_sched_per_core 2

# The maximum number of blocks that can be scheduled to a warp pool at a time.
MaxBlocksPerWarpPool = <num> (Default = 4) -TODO

# The maximum number of warps that can be scheduled to a warp pool at a time.
MaxWarpsPerWarpPool = <num> (Default = 16) -gpgpu_shader_cta

# Number of registers per SM. These are divided evenly between all warp pools.
NumRegisters = <num> (Default = 65536) -gpgpu_shader_registers

# Parameters for fetch and dispatch.
[ FrontEnd ]
# Latency of instruction memory in number of cycles.
FetchLatency = <cycles> (Default = 5)

# Maximum number of instructions fetched per cycle.
# gpgpu_inst_fetch_throughput
# 1 # the number of fetched intruction per warp each cycle
FetchWidth = <num> (Default = 8)

# Size of the buffer holding fetched instructions.
FetchBufferSize = <num> (Default = 10)

# Latency of the decode stage in number of cycles.
DispatchLatency = <cycles> (Default = 1)

# Number of instructions that can be issued per cycle.
DispatchWidth = <num> (Default = 5) -gpgpu_max_insn_issue_per_warp 2 * (gpgpu_num_sched_per_core)?

# Maximum number of instructions that can be issued of each type (SIMD, scalar, etc.) in a single cycle.
MaxInstIssuedPerType = <num> (Default = 1)


# ID_OC_SP,ID_OC_DP,ID_OC_INT,ID_OC_SFU,ID_OC_MEM,
# OC_EX_SP, OC_EX_DP,   OC_EX_INT,  OC_EX_SFU,  OC_EX_MEM,  EX_WB
# this is kepler
# 6,        4,        0,          2,          1,          12

# parameters for the Single Precision Units.
[ SPU ]
# Number of lanes per SPU. This must divide the warp size (32) evenly.
NumSPULanes = <num> (Default = 32)

# Maximum number of instructions processed per cycle.
Width = <num> (Default = 1)

# Size of the buffer holding dispatched instructions.
DispatchBufferSize = <num> (Default = 1)

# Latency of register file access in number of cycles for reads.
ReadLatency = <cycles> (Default = 1)

# Size of the buffer holding register read instructions.
ReadBufferSize = <num> (Default = 1)

# Latency of execution in number of cycles.
ExecutionLatency = <cycles> (Default = 1) NOT! OC_EX_SP

# Size of the buffer holding executing instructions.
ExecutionBufferSize = <num> (Default = 1)

# Latency of register file writes in number of cycles.
WriteLatency = <cycles> (Default = 1)

# Size of the buffer holding register write instructions.
WriteBufferSize = <num> (Default = 1)

# parameters for the Double Precision Units.
[ DPU ]
# Number of lanes per DPU. This must divide the warp size (32) evenly.
NumSPULanes = <num> (Default = 32)

# Maximum number of instructions processed per cycle.
Width = <num> (Default = 1)

# Size of the buffer holding dispatched instructions.
DispatchBufferSize = <num> (Default = 1)

# Latency of register file access in number of cycles for reads.
ReadLatency = <cycles> (Default = 1)

# Size of the buffer holding register read instructions.
ReadBufferSize = <num> (Default = 1)

# Latency of execution in number of cycles.
ExecutionLatency = <cycles> (Default = 1)

# Size of the buffer holding executing instructions.
ExecutionBufferSize = <num> (Default = 1)

# Latency of register file writes in number of cycles.
WriteLatency = <cycles> (Default = 1)

# Size of the buffer holding register write instructions.
WriteBufferSize = <num> (Default = 1)

# parameters for the Special Function Units.
[ SFU ]
# Number of lanes per SFU. This must divide the warp size (32) evenly.
NumSPULanes = <num> (Default = 32)

# Maximum number of instructions processed per cycle.
Width = <num> (Default = 1)

# Size of the buffer holding dispatched instructions.
DispatchBufferSize = <num> (Default = 1)

# Latency of register file access in number of cycles for reads.
ReadLatency = <cycles> (Default = 1)

# Size of the buffer holding register read instructions.
ReadBufferSize = <num> (Default = 1)

# Latency of execution in number of cycles.
ExecutionLatency = <cycles> (Default = 1)

# Size of the buffer holding executing instructions.
ExecutionBufferSize = <num> (Default = 1)

# Latency of register file writes in number of cycles.
WriteLatency = <cycles> (Default = 1)

# Size of the buffer holding register write instructions.
WriteBufferSize = <num> (Default = 1)

# parameters for the Integer Math Units.
[ IMU ]
# Number of lanes per IMU. This must divide the warp size (32) evenly.
NumSPULanes = <num> (Default = 32)

# Maximum number of instructions processed per cycle.
Width = <num> (Default = 1)

# Size of the buffer holding dispatched instructions.
DispatchBufferSize = <num> (Default = 1)

# Latency of register file access in number of cycles for reads.
ReadLatency = <cycles> (Default = 1)

# Size of the buffer holding register read instructions.
ReadBufferSize = <num> (Default = 1)

# Latency of execution in number of cycles.
ExecutionLatency = <cycles> (Default = 1)

# Size of the buffer holding executing instructions.
ExecutionBufferSize = <num> (Default = 1)

# Latency of register file writes in number of cycles.
WriteLatency = <cycles> (Default = 1)

# Size of the buffer holding register write instructions.
WriteBufferSize = <num> (Default = 1)

# parameters for the Branch Units.
[ BRU ]
# Number of lanes per BRU. This must divide the warp size (32) evenly.
NumSPULanes = <num> (Default = 32)

# Maximum number of instructions processed per cycle.
Width = <num> (Default = 1)

# Size of the buffer holding dispatched instructions.
DispatchBufferSize = <num> (Default = 1)

# Latency of register file access in number of cycles for reads.
ReadLatency = <cycles> (Default = 1)

# Size of the buffer holding register read instructions.
ReadBufferSize = <num> (Default = 1)

# Latency of execution in number of cycles.
ExecutionLatency = <cycles> (Default = 1)

# Size of the buffer holding executing instructions.
ExecutionBufferSize = <num> (Default = 1)

# Latency of register file writes in number of cycles.
WriteLatency = <cycles> (Default = 1)

# Size of the buffer holding register write instructions.
WriteBufferSize = <num> (Default = 1)

# parameters for the Single Precision Units.
[ LSU ]
# Maximum number of instructions processed per cycle.
Width = <num> (Default = 1)

# Size of the buffer holding dispatched instructions.
DispatchBufferSize = <num> (Default = 1)

# Latency of register file access in number of cycles for reads.
ReadLatency = <cycles> (Default = 1)

# Size of the buffer holding register read instructions.
ReadBufferSize = <num> (Default = 1)

# Size of in flight memory accesses.
MaxInFlightMemAcccesses = <num> (Defalut = 32)

# Latency of register file writes in number of cycles.
WriteLatency = <cycles> (Default = 1)

# Size of the buffer holding register write instructions.
WriteBufferSize = <num> (Default = 1)
```

#### GPUTejas <=> GPGPUsim
```bash
Configuration.Simulation.GPUType
Configuration.Simulation.ThreadsPerCTA = gpgpu_shader_core_pipeline [0]

System.NoOfTPC = 8 gpgpu_n_clusters
System.TPC.NoOfSM = 1 gpgpu_n_cores_per_cluster
System.TPC.SM.Frequency = gpgpu_clock_domains[0]
System.TPC.SM.NoOfWarpSchedulers = gpgpu_num_sched_per_core
System.TPC.SM.NoOfSP = 8 ? gpgpu_num_sp_units
System.TPC.SM.WarpSize= gpgpu_shader_cta
System.TPC.SM.SP.NoOfThreadsSupported = 32 gpgpu_shader_cta, have that but what does it mean?

System.TPC.SM.RegisterFile.Bank.BankSize = gpgpu_num_reg_banks
System.TPC.SM.RegisterFile.Dispatch.DispatchSize = gpgpu_reg_file_port_throughput ??

System.MainMemory.MainMemoryLatency = dram_latency
System.MainMemory.MainMemoryFrequency = gpgpu_clock_domains<DRAM Clock>
System.MainMemory.MainMemoryAccessPorts = gpgpu_dram_buswidth
System.MainMemoryController.rankLatency = dram_latency ?
System.MainMemoryController.rankOperatingFrequency = gpgpu_clock_domains<DRAM Clock>

-gpgpu_dram_timing_opt nbk=16:CCD=2:RRD=6:RCD=12:RAS=28:RP=12:RC=40: CL=12:WL=4:CDLR=5:WR=12:nbkgrp=1:CCDL=0:RTPL=0

System.MainMemoryController.numBanks = gpgpu_dram_timing_opt<nbk>
System.MainMemoryController.tCCD = gpgpu_dram_timing_opt<CCD>
System.MainMemoryController.tCL = gpgpu_dram_timing_opt<CL>
System.MainMemoryController.tRP = gpgpu_dram_timing_opt<RP>
System.MainMemoryController.tRC = gpgpu_dram_timing_opt<RC>
System.MainMemoryController.tRCD = gpgpu_dram_timing_opt<RCD>
System.MainMemoryController.tRAS = gpgpu_dram_timing_opt<RAS>
System.MainMemoryController.tWR = gpgpu_dram_timing_opt<WR>

gpgpu_n_mem
gpgpu_n_sub_partition_per_mchannel
System.CacheBusLatency

Configuration.Library.iCache = 
Configuration.Library.sharedCache = 
Assoc = <assoc>
BlockSize = <bsize>


-gpgpu_tex_cache:l1  N:16:128:24,L:R:m:N:L,F:128:4,128:2 # per-shader L1 texture cache  (READ-ONLY) config  {<nsets>:<bsize>:<assoc>,<rep>:<wr>:<alloc>:<wr_alloc>,<mshr>:<N>:<merge>,<mq>:<rf>}
-gpgpu_const_cache:l1 N:128:64:2,L:R:f:N:L,A:2:64,4 # per-shader L1 constant memory cache  (READ-ONLY) config  {<nsets>:<bsize>:<assoc>,<rep>:<wr>:<alloc>:<wr_alloc>,<mshr>:<N>:<merge>,<mq>} 
-gpgpu_cache:il1     N:8:128:4,L:R:f:N:L,A:2:48,4 # shader L1 instruction cache config  {<nsets>:<bsize>:<assoc>,<rep>:<wr>:<alloc>:<wr_alloc>,<mshr>:<N>:<merge>,<mq>} 
-gpgpu_cache:dl1     N:64:128:6,L:L:m:N:H,A:128:8,8 # per-shader L1 data cache config  {<nsets>:<bsize>:<assoc>,<rep>:<wr>:<alloc>:<wr_alloc>,<mshr>:<N>:<merge>,<mq> | none}

<BlockSize>64</BlockSize>
         <Associativity>8</Associativity>
         <Size>32</Size>
         <Latency>2</Latency>
         <MSHRSize>32</MSHRSize>
         <AccessPorts>1</AccessPorts>

<nsets>:<bsize>:<assoc>,<rep>:<wr>:<alloc>:<wr_alloc>:<set_index_fn>,<mshr>:<N>:<merge>,<mq>:**<fifo_entry>
-gpgpu_cache:dl1PrefShared  S:4:128:32,L:L:s:N:L,A:256:8,16:0,3

these numbers seem off, unless for ptx_opcode_initiation_*
Configuration.OperationLatency = 
load>100</load> = dram_latency ?
intALU>1</intALU> = ptx_opcode_latency_int[0] ?
<intMUL>2</intMUL> = ptx_opcode_latency_int[2]
<intDIV>4</intDIV> = ptx_opcode_latency_int[4]
<floatALU>1</floatALU> = ptx_opcode_latency_fp[0] ? 
<floatMUL>4</floatMUL> = ptx_opcode_latency_fp[2]
<floatDIV>8</floatDIV> = ptx_opcode_latency_fp[4]
```


