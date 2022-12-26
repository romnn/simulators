import abc
import pandas as pd
import gpusims

USE_DURATION = False


class Metric(abc.ABC):
    name = "Unknown"
    unit = None
    log = True

    def __init__(self, data):
        self.data = data

    @abc.abstractmethod
    def compute(self):
        pass

    @property
    def m2s_df(self):
        return self.data.get(gpusims.MULTI2SIM)

    @property
    def hw_df(self):
        return self.data.get(gpusims.NATIVE)

    @property
    def macsim_df(self):
        return self.data.get(gpusims.MACSIM)

    @property
    def accelsim_ptx_df(self):
        return self.data.get(gpusims.ACCELSIM_PTX)

    @property
    def accelsim_sass_df(self):
        return self.data.get(gpusims.ACCELSIM_SASS)

    @property
    def tejas_df(self):
        return self.data.get(gpusims.TEJAS)


class BaseMetric(Metric):
    def compute_m2s(self, df):
        return None

    def compute_tejas(self, df):
        return None

    def compute_macsim(self, df):
        return None

    def compute_accelsim_ptx(self, df):
        return None

    def compute_accelsim_sass(self, df):
        return None

    def compute_native(self, df):
        return None

    def compute(self):
        data = []
        if self.m2s_df is not None:
            # "Multi2Sim"
            data.append((gpusims.MULTI2SIM, self.compute_m2s(self.m2s_df)))
        if self.macsim_df is not None:
            # "MacSim"
            data.append((gpusims.MACSIM, self.compute_macsim(self.macsim_df)))
        if self.tejas_df is not None:
            # "GPUTejas"
            data.append((gpusims.TEJAS, self.compute_tejas(self.tejas_df)))
        if self.accelsim_ptx_df is not None:
            # "AccelSim PTX"
            data.append(
                (gpusims.ACCELSIM_PTX, self.compute_accelsim_ptx(self.accelsim_ptx_df))
            )
        if self.accelsim_sass_df is not None:
            # "AccelSim SASS"
            data.append(
                (
                    gpusims.ACCELSIM_SASS,
                    self.compute_accelsim_sass(self.accelsim_sass_df),
                )
            )
        if self.hw_df is not None:
            # "Hardware"
            data.append((gpusims.NATIVE, self.compute_native(self.hw_df)))

        df = pd.DataFrame.from_records(
            data,
            columns=["Simulator", "Value"],
        )
        df["Value"] = df["Value"].astype(float)
        return df

    def hw_duration_us(self):
        if "Duration" in self.hw_df:
            # convert us to us (1e-6)
            # duration already us
            return self.hw_df["Duration"].sum()
        elif "gpu__time_duration.sum_nsecond" in self.hw_df:
            # convert ns to us
            return self.hw_df["gpu__time_duration.sum_nsecond"].sum() * 1e-3
        else:
            raise ValueError("hw dataframe missing duration")

    def hw_cycles(self):
        # nsight_col = "sm__cycles_elapsed.sum_cycle"
        nsight_col = "gpc__cycles_elapsed.avg_cycle"
        # nsight_col = "sm__cycles_active.avg_cycle"
        if "elapsed_cycles_sm" in self.hw_df:
            sm_count = self.data.config.spec["sm_count"]
            cycles = self.hw_df["elapsed_cycles_sm"].sum()
            return cycles / sm_count
        elif nsight_col in self.hw_df:
            return self.hw_df[nsight_col].sum()
        else:
            raise ValueError("hw dataframe missing cycles")

    def hw_ipc(self):
        # would need to use active_sm_count so just compute ourselves
        # if "ipc" in self.hw_df:
        #     # ipc is per SM metric
        #     # https://stackoverflow.com/questions/51316553/understanding-the-ipc-metric-from-nvprof-and-gpgpusim
        #     # there is also issued_ipc
        #     sm_count = self.data.config.spec["sm_count"]
        #     return self.hw_df["ipc"].mean() * sm_count

        instructions = self.hw_instructions()
        cycles = self.hw_cycles()
        return instructions / cycles

    def hw_instructions(self):
        if "inst_issued" in self.hw_df:
            # there is also inst_executed
            return self.hw_df["inst_issued"].sum()
        elif "smsp__inst_executed.sum_inst" in self.hw_df:
            # there is also sm__inst_executed.sum_inst
            # sm__sass_thread_inst_executed.sum_inst
            return self.hw_df["smsp__inst_executed.sum_inst"].sum()
        else:
            raise ValueError("hw dataframe missing instructions")

    def num_blocks(self):
        if "Grid X" in self.hw_df:
            return (
                self.hw_df["Grid X"] * self.hw_df["Grid Y"] * self.hw_df["Grid Z"]
            ).sum()
        else:
            return self.hw_df["launch__grid_size"].sum()


class Cycles(BaseMetric):
    name = "Cycles"

    def compute_m2s(self, df):
        total_cycles = df["Total.Cycles"].sum()
        per_core_cycles = total_cycles / df["Config.Device.NumSM"].mean()
        device_cycles = df["Device.Cycles"].sum()
        assert per_core_cycles == device_cycles
        return device_cycles

    def compute_macsim(self, df):
        # CYC_COUNT_TOT seems to be way too large
        # CYCLE_GPU is always zero
        return df["CYC_COUNT_CORE_TOTAL"][0]

    def compute_tejas(self, df):
        return df["total_cycle_count"].sum()

    def compute_accelsim_ptx(self, df):
        return df["gpu_tot_sim_cycle"].sum()

    def compute_accelsim_sass(self, df):
        return df["gpu_tot_sim_cycle"].sum()

    def compute_native(self, df):
        if USE_DURATION:
            # clock speed is mhz, so *1e6
            # duration is us, so *1e-6
            # unit conversions cancel each other out
            hw_duration = self.hw_duration_us()
            hw_value = hw_duration * self.data.config.spec["clock_speed"]
        else:
            # sm_efficiency: The percentage of time at least one warp
            # is active on a specific multiprocessor
            # mean_sm_efficiency = self.hw_df["sm_efficiency"].mean() / 100.0
            # num_active_sm = self.data.config.spec["sm_count"] * mean_sm_efficiency
            # print("num active sms", num_active_sm)
            hw_value = self.hw_cycles()
            # hw_value *= mean_sm_efficiency
        return hw_value


class ExecutionTime(BaseMetric):
    name = "Execution Time"
    unit = "s"

    def compute(self):
        # data = []
        m2s_sim = 0
        if self.m2s_df is not None:
            m2s_sim = self.m2s_df["sim_wall_time"].sum()
            # data.append("Multi2Sim, "Sim"

        macsim_sim, macsim_trace = 0, 0
        if self.macsim_df is not None:
            macsim_sim = self.macsim_df["EXE_TIME"][0]
            macsim_trace = self.macsim_df["trace_wall_time"].sum()

        tejas_sim, tejas_trace = 0, 0
        if self.tejas_df is not None:
            tejas_sim = self.tejas_df["sim_time_secs"].sum()
            tejas_trace = self.tejas_df["trace_wall_time"].sum()

        accel_ptx_sim = 0
        if self.accelsim_ptx_df is not None:
            accel_ptx_sim = self.accelsim_ptx_df["gpgpu_simulation_time_sec"].sum()

        accel_sass_sim, accel_sass_trace = 0, 0
        if self.accelsim_sass_df is not None:
            accel_sass_sim = self.accelsim_sass_df["gpgpu_simulation_time_sec"].sum()
            accel_sass_trace = self.accelsim_sass_df["trace_wall_time"].sum()

        hw_value = 0
        if self.hw_df is not None:
            # convert to seconds
            hw_value = self.hw_duration_us() * 1e-6

        df = pd.DataFrame.from_records(
            data=[
                (gpusims.MULTI2SIM, "Sim", m2s_sim),
                (gpusims.MULTI2SIM, "Trace", 0),
                (gpusims.MACSIM, "Sim", macsim_sim),
                (gpusims.MACSIM, "Trace", macsim_trace),
                (gpusims.TEJAS, "Sim", tejas_sim),
                (gpusims.TEJAS, "Trace", tejas_trace),
                (gpusims.ACCELSIM_PTX, "Sim", accel_ptx_sim),
                (gpusims.ACCELSIM_PTX, "Trace", 0),
                (gpusims.ACCELSIM_SASS, "Sim", accel_sass_sim),
                (gpusims.ACCELSIM_SASS, "Trace", accel_sass_trace),
                (gpusims.NATIVE, "Sim", hw_value),
                (gpusims.NATIVE, "Trace", 0),
            ],
            columns=["Simulator", "Kind", "Value"],
        )
        df["Value"] = df["Value"].round(6)
        return df


class L2ReadHit(BaseMetric):
    name = "Total L2 Read Hits"

    # m2s has no l2 read hits
    # tejas has no l2 read hits
    # macsim has no l2 read hits (only total hits)

    def compute_accelsim_ptx(self, df):
        return df["l2_cache_read_hit"].sum()

    def compute_accelsim_sass(self, df):
        return df["l2_cache_read_hit"].sum()

    def compute_native(self, df):
        if "l2_tex_read_transactions" in df:
            return (
                df["l2_tex_read_transactions"] * df["l2_tex_read_hit_rate"] / 100.0
            ).sum()
        else:
            return df["lts__t_sectors_srcunit_tex_op_read_lookup_hit.sum_sector"].sum()


class L2WriteHit(BaseMetric):
    name = "Total L2 Write Hits"

    # m2s has no l2 write hits
    # tejas has no l2 write hits
    # macsim has no l2 write hits (only total hits)

    def compute_accelsim_ptx(self, df):
        return df["l2_cache_write_hit"].sum()

    def compute_accelsim_sass(self, df):
        return df["l2_cache_write_hit"].sum()

    def compute_native(self, df):
        if "l2_tex_write_transactions" in df:
            return (
                df["l2_tex_write_transactions"] * df["l2_tex_write_hit_rate"] / 100.0
            ).sum()
        else:
            return df["lts__t_sectors_srcunit_tex_op_write_lookup_hit.sum_sector"].sum()


class InstructionCount(BaseMetric):
    name = "Total Instruction Count"

    def compute_m2s(self, df):
        return df["Total.Instructions"].sum()

    def compute_macsim(self, df):
        return df["INST_COUNT_TOT"][0]

    def compute_tejas(self, df):
        # * self.data.config.spec["sm_count"]
        # todo: get num threads
        return df["total_inst_count"].sum() * 16

    def compute_accelsim_ptx(self, df):
        return df["gpgpu_n_tot_w_icount"].sum()

    def compute_accelsim_sass(self, df):
        return df["gpgpu_n_tot_w_icount"].sum()

    def compute_native(self, df):
        return self.hw_instructions()


class IPC(BaseMetric):
    name = "Total IPC"

    def compute_m2s(self, df):
        # those metrics seem to be always 0, so we compute manually
        # Device.instructionsPerCycle
        # Total.InstructionsPerCycle
        instructions = df["Total.Instructions"].sum()
        # cycles = df["Total.Cycles"].sum()
        cycles = df["Device.Cycles"].sum()
        return instructions / cycles

    def compute_macsim(self, df):
        return df["INST_COUNT_TOT"][0] / df["CYC_COUNT_CORE_TOTAL"][0]

    def compute_tejas(self, df):
        # their total_ipc does not make sense?
        tejas_value = df["total_ipc"].sum()
        # * self.data.config.spec["sm_count"]
        # tejas_instr = (
        #     self.tejas_df["total_inst_count"].sum()
        #     * self.data.config.spec["sm_count"]
        # )
        # tejas_value = tejas_instr / self.tejas_df["total_cycle_count"].sum()
        return tejas_value

    def compute_accelsim_ptx(self, df):
        instructions = self.hw_instructions()
        return instructions / df["gpu_tot_sim_cycle"].sum()

    def compute_accelsim_sass(self, df):
        instructions = self.hw_instructions()
        return instructions / df["gpu_tot_sim_cycle"].sum()

    def compute_native(self, df):
        hw_cycles = self.hw_cycles()
        instructions = self.hw_instructions()
        hw_value = instructions / hw_cycles
        hw_value = self.hw_ipc()
        return hw_value


class DRAMAccesses(BaseMetric):
    name = "Total DRAM Accesses (Read/Write)"


class L2Writes(BaseMetric):
    name = "Total L2 Writes"

    # m2s has no l2 writes
    # macsim has no l2 writes
    # tejas has no l2 writes
    def compute_accelsim_ptx(self, df):
        return df["l2_cache_write_total"].sum()

    def compute_accelsim_sass(self, df):
        return df["l2_cache_write_total"].sum()

    def compute_native(self, df):
        if "l2_tex_write_transactions" in df:
            return df["l2_tex_write_transactions"].sum()
        else:
            return df["lts__t_sectors_srcunit_tex_op_write.sum_sector"].sum()


class L2Reads(BaseMetric):
    name = "Total L2 Reads"

    # m2s has no l2 reads
    # macsim has no l2 reads
    # tejas has no l2 reads
    def compute_accelsim_ptx(self, df):
        return df["l2_cache_read_total"].sum()

    def compute_accelsim_sass(self, df):
        return df["l2_cache_read_total"].sum()

    def compute_native(self, df):
        if "l2_tex_read_transactions" in df:
            return df["l2_tex_read_transactions"].sum()
        else:
            return df["lts__t_sectors_srcunit_tex_op_read.sum_sector"].sum()


class DRAMReads(BaseMetric):
    name = "Total DRAM Reads"

    # m2s has no dram writes
    # macsim has no dram writes
    def compute_tejas(self, df):
        return int(df["dram_total_reads"].sum())

    def compute_accelsim_ptx(self, df):
        return int(df["total_dram_reads"].sum())

    def compute_accelsim_sass(self, df):
        return int(df["total_dram_reads"].sum())

    def compute_native(self, df):
        if "dram_read_transactions" in df:
            return int(df["dram_read_transactions"].sum())
        else:
            return int(df["dram__sectors_read.sum_sector"].sum())


class DRAMWrites(BaseMetric):
    name = "Total DRAM Writes"

    # m2s has no dram writes
    # macsim has no dram writes
    def compute_tejas(self, df):
        return int(df["dram_total_writes"].sum())

    def compute_accelsim_ptx(self, df):
        return int(df["total_dram_writes"].sum())

    def compute_accelsim_sass(self, df):
        return int(df["total_dram_writes"].sum())

    def compute_native(self, df):
        if "dram_write_transactions" in df:
            return int(df["dram_write_transactions"].sum())
        else:
            return int(df["dram__sectors_write.sum_sector"].sum())


class ConstantCacheAccesses(BaseMetric):
    name = "Total Constant Cache Accesses"

    # hw has no constant cache misses
    def compute_tejas(self, df):
        return df["total_const_cache_access"].sum()

    def compute_macsim(self, df):
        return df["CONST_CACHE_ACCESS"][0]


class ConstantCacheMisses(BaseMetric):
    name = "Total Constant Cache Misses"

    # hw has no constant cache misses
    def compute_tejas(self, df):
        return df["total_const_cache_misses"].sum()

    def compute_macsim(self, df):
        return df["CONST_CACHE_MISS"][0]


class ConstantCacheHits(BaseMetric):
    name = "Total Constant Cache Hits"

    # hw has no constant cache misses
    def compute_tejas(self, df):
        accesses = df["total_const_cache_access"].sum()
        misses = df["total_const_cache_misses"].sum()
        return max(0, accesses - misses)

    def compute_macsim(self, df):
        return df["CONST_CACHE_HIT"][0]


class InstructionCacheAccesses(BaseMetric):
    name = "Total Instruction Cache Accesses"

    # hw has no instruction cache accesses
    # macsim has no instruction cache accesses
    def compute_tejas(self, df):
        return df["total_instr_cache_access"].sum()


class InstructionCacheMisses(BaseMetric):
    name = "Total Instruction Cache Misses"

    # hw has no instruction cache misses
    def compute_tejas(self, df):
        return df["total_instr_cache_misses"].sum()

    def compute_macsim(self, df):
        return df["ICACHE_MISS_TOTAL"][0]


class InstructionCacheHits(BaseMetric):
    name = "Total Instruction Cache Hits"

    # hw has no instruction cache misses
    def compute_tejas(self, df):
        accesses = df["total_instr_cache_access"].sum()
        misses = df["total_instr_cache_misses"].sum()
        return max(0, accesses - misses)

    def compute_macsim(self, df):
        return df["ICACHE_MISS_TOTAL"][0]


class TextureCacheAccesses(BaseMetric):
    name = "Total Texture Cache Accesses"

    # tejas has no texture cache accesses
    def compute_macsim(self, df):
        return df["TEXTURE_CACHE_ACCESS"][0]


class TextureCacheHits(BaseMetric):
    name = "Total Texture Cache Accesses"

    # tejas has no texture cache hits
    def compute_macsim(self, df):
        return df["TEXTURE_CACHE_HIT"][0]

    def compute_native(self, df):
        hitrate = df["tex_cache_hit_rate"].mean() / 100.0
        accesses = df["tex_cache_transactions"].sum()
        return accesses * hitrate


class TextureCacheMisses(BaseMetric):
    name = "Total Texture Cache Misses"

    # tejas has no texture cache misses
    def compute_macsim(self, df):
        return df["TEXTURE_CACHE_MISS"][0]

    def compute_native(self, df):
        hitrate = df["tex_cache_hit_rate"].mean() / 100.0
        accesses = df["tex_cache_transactions"].sum()
        return accesses * max(0, 1 - hitrate)


"""
# tejas
total_instr_cache_access	0
total_const_cache_access	0
total_shared_cache_access	0
total_instr_cache_misses	8
total_const_cache_misses	68
total_shared_cache_misses
"""

"""
# macsim
TOTAL_WRITES
TOTAL_MEMORY
L1_HIT_GPU
L1_MISS_GPU
L2_HIT_GPU
L2_MISS_GPU
L3_HIT_GPU
L3_MISS_GPU
LLC_HIT_GPU
LLC_MISS_GPU

# INST_COUNT_TOT
# SHARED_MEM_ACCESS
# ICACHE_MISS_TOTAL

# DISPATCHED_INST

# CONST_CACHE_ACCESS
# CONST_CACHE_HIT
# CONST_CACHE_MISS

# TEXTURE_CACHE_HIT
# TEXTURE_CACHE_MISS
# TEXTURE_CACHE_ACCESS
"""


class L2Accesses(BaseMetric):
    name = "Total L2 Accesses"

    # m2s has no l2 accesses
    # tejas has no l2 accesses
    def compute_accelsim_ptx(self, df):
        return df["l2_cache_write_total"].sum() + df["l2_cache_read_total"].sum()

    def compute_accelsim_sass(self, df):
        return df["l2_cache_write_total"].sum() + df["l2_cache_read_total"].sum()

    def compute_macsim(self, df):
        return df["L2_HIT_GPU"][0] + df["L2_MISS_GPU"][0]

    def compute_native(self, df):
        if "l2_tex_read_transactions" in df:
            return (
                df["l2_tex_write_transactions"].sum()
                + df["l2_tex_read_transactions"].sum()
            )
        else:
            return (
                df["lts__t_sectors_srcunit_tex_op_write.sum_sector"].sum()
                + df["lts__t_sectors_srcunit_tex_op_read.sum_sector"].sum()
            )


class L2Hits(BaseMetric):
    name = "Total L2 Hits"

    # m2s has no l2 hits
    # tejas has no l2 hits
    def compute_accelsim_ptx(self, df):
        return df["l2_cache_write_hit"].sum() + df["l2_cache_read_hit"].sum()

    def compute_accelsim_sass(self, df):
        return df["l2_cache_write_hit"].sum() + df["l2_cache_read_hit"].sum()

    def compute_macsim(self, df):
        return df["L2_HIT_GPU"][0]

    def compute_native(self, df):
        read_hits = df["l2_tex_read_transactions"].sum() * (
            df["l2_tex_read_hit_rate"].mean() / 100.0
        )
        write_hits = df["l2_tex_write_transactions"].sum() * (
            df["l2_tex_write_hit_rate"].mean() / 100.0
        )
        return read_hits + write_hits


class L2Misses(BaseMetric):
    name = "Total L2 Misses"

    # m2s has no l2 misses
    # tejas has no l2 misses
    def compute_accelsim_ptx(self, df):
        return df["l2_cache_write_miss"].sum() + df["l2_cache_read_miss"].sum()

    def compute_accelsim_sass(self, df):
        return df["l2_cache_write_miss"].sum() + df["l2_cache_read_miss"].sum()

    def compute_macsim(self, df):
        return df["L2_MISS_GPU"][0]

    def compute_native(self, df):
        read_hit_rate = df["l2_tex_read_hit_rate"].mean() / 100.0
        write_hit_rate = df["l2_tex_write_hit_rate"].mean() / 100.0
        read_miss_rate = max(0, 1 - read_hit_rate)
        write_miss_rate = max(0, 1 - write_hit_rate)
        read_hits = df["l2_tex_read_transactions"].sum() * read_miss_rate
        write_hits = df["l2_tex_write_transactions"].sum() * write_miss_rate
        return read_hits + write_hits


"""
# hw
ldst_executed
ldst_issued
local_hit_rate
local_load_transactions
local_store_transactions
sysmem_read_transactions
sysmem_write_transactions

global_hit_rate
l2_tex_hit_rate
l2_tex_read_hit_rate
l2_tex_write_hit_rate
l2_tex_write_transactions

tex_cache_hit_rate
tex_cache_transactions
"""
