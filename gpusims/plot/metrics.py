import abc
import pandas as pd
import gpusims

USE_DURATION = False


class Metric(abc.ABC):
    name = "Unknown"
    unit = None

    def __init__(self, data):
        self.data = data

    @abc.abstractmethod
    def compute(self):
        pass

    @property
    def config(self):
        return dict()

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
            data.append(("Multi2Sim", self.compute_m2s(self.m2s_df)))
        if self.macsim_df is not None:
            data.append(("MacSim", self.compute_macsim(self.macsim_df)))
        if self.tejas_df is not None:
            data.append(("GPUTejas", self.compute_tejas(self.tejas_df)))
        if self.accelsim_ptx_df is not None:
            data.append(
                ("AccelSim PTX", self.compute_accelsim_ptx(self.accelsim_ptx_df))
            )
        if self.accelsim_sass_df is not None:
            data.append(
                ("AccelSim SASS", self.compute_accelsim_sass(self.accelsim_sass_df))
            )
        if self.hw_df is not None:
            data.append(("Hardware", self.compute_native(self.hw_df)))

        df = pd.DataFrame.from_records(
            data,
            columns=["Simulator", "Value"],
        )
        df["Value"] = df["Value"].astype(float)
        return df


class Cycles(BaseMetric):
    name = "Cycles"
    log = True

    def __init__(self, data, use_duration=USE_DURATION):
        self.use_duration = use_duration
        super().__init__(data)

    def compute_m2s(self, df):
        per_core_cycles = df["Total.Cycles"].sum() / df["Config.Device.NumSM"].mean()
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
        if self.use_duration:
            # clock speed is mhz, so *1e6
            # duration is us, so *1e-6
            # unit conversions cancel each other out
            hw_value = (
                self.hw_df["Duration"].sum() * self.data.config.spec["clock_speed"]
            )
        else:
            # sm_efficiency: The percentage of time at least one warp
            # is active on a specific multiprocessor
            # mean_sm_efficiency = self.hw_df["sm_efficiency"].mean() / 100.0
            sm_count = self.data.config.spec["sm_count"]
            # num_active_sm = self.data.config.spec["sm_count"] * mean_sm_efficiency
            # print("num active sms", num_active_sm)
            hw_value = self.hw_df["elapsed_cycles_sm"].sum() / sm_count
            # hw_value *= mean_sm_efficiency
        return hw_value

    # def compute(self):
    #     data = []
    #     if self.m2s_df is not None:
    #         per_core_cycles = (
    #             self.m2s_df["Total.Cycles"].sum()
    #             / self.m2s_df["Config.Device.NumSM"].mean()
    #         )
    #         m2s_value = self.m2s_df["Device.Cycles"].sum()
    #         assert per_core_cycles == m2s_value
    #         data.append(("Multi2Sim", m2s_value))

    #     if self.macsim_df is not None:
    #         # CYC_COUNT_TOT seems to be way too large
    #         # CYCLE_GPU is always zero
    #         macsim_value = self.macsim_df["CYC_COUNT_CORE_TOTAL"][0]
    #         data.append(("MacSim", macsim_value))

    #     if self.tejas_df is not None:
    #         tejas_value = self.tejas_df["total_cycle_count"].sum()
    #         data.append(("GPUTejas", tejas_value))

    #     for name, accel_df in [
    #         ("AccelSim PTX", self.accelsim_ptx_df),
    #         ("AccelSim SASS", self.accelsim_sass_df),
    #     ]:
    #         if accel_df is not None:
    #             accel_value = accel_df["gpu_tot_sim_cycle"].sum()
    #             data.append((name, accel_value))

    #     if self.hw_df is not None:
    #         if self.use_duration:
    #             # clock speed is mhz, so *1e6
    #             # duration is us, so *1e-6
    #             # unit conversions cancel each other out
    #             hw_value = (
    #                 self.hw_df["Duration"].sum() * self.data.config.spec["clock_speed"]
    #             )
    #         else:
    #             # sm_efficiency: The percentage of time at least one warp
    #             # is active on a specific multiprocessor
    #             mean_sm_efficiency = self.hw_df["sm_efficiency"].mean() / 100.0
    #             sm_count = self.data.config.spec["sm_count"]
    #             # num_active_sm = self.data.config.spec["sm_count"] * mean_sm_efficiency
    #             # print("num active sms", num_active_sm)
    #             hw_value = self.hw_df["elapsed_cycles_sm"].sum() / sm_count
    #             # hw_value *= mean_sm_efficiency

    #         data.append(("Hardware", hw_value))

    #     df = pd.DataFrame.from_records(
    #         data,
    #         columns=["Simulator", "Value"],
    #     )
    #     df["Value"] = df["Value"].astype(int)
    #     return df


class ExecutionTime(Metric):
    name = "Execution Time"
    unit = "s"
    log = True

    # @property
    # def config(self):
    #     return dict(
    #         log=True,
    #     )
    # def compute_m2s(self, df):
    #     return df["sim_wall_time"].sum()

    def compute(self):
        # data = []
        m2s_sim = 0
        if self.m2s_df is not None:
            m2s_sim = self.m2s_df["sim_wall_time"].sum()

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
            # convert us to seconds
            hw_value = self.hw_df["Duration"].sum() * 1e-6

        df = pd.DataFrame.from_records(
            data=[
                ("Multi2Sim", "Sim", m2s_sim),
                ("Multi2Sim", "Trace", 0),
                ("MacSim", "Sim", macsim_sim),
                ("MacSim", "Trace", macsim_trace),
                ("GPUTejas", "Sim", tejas_sim),
                ("GPUTejas", "Trace", tejas_trace),
                ("AccelSim PTX", "Sim", accel_ptx_sim),
                ("AccelSim PTX", "Trace", 0),
                ("AccelSim SASS", "Sim", accel_sass_sim),
                ("AccelSim SASS", "Trace", accel_sass_trace),
                ("Hardware", "Sim", hw_value),
                ("Hardware", "Trace", 0),
            ],
            columns=["Simulator", "Kind", "Value"],
        )
        df["Value"] = df["Value"].round(6)
        return df


class L2ReadHit(BaseMetric):
    name = "Total L2 Read Hits"

    # m2s has no l2 read hits
    # tejas has no l2 read hits

    def compute_macsim(self, df):
        return df["L2_HIT_GPU"][0]

    def compute_accelsim_ptx(self, df):
        return df["l2_cache_read_hit"].sum()

    def compute_accelsim_sass(self, df):
        return df["l2_cache_read_hit"].sum()

    def compute_native(self, df):
        return (
            df["l2_tex_read_transactions"].sum()
            * df["l2_tex_read_hit_rate"].sum()
            / 100.0
        )

    # @property
    # def config(self):
    #     return dict()

    # def compute(self):
    #     data = []

    #     # m2s has no l2 read hits
    #     # tejas has no l2 read hits
    #     if self.macsim_df is not None:
    #         macsim_value = self.macsim_df["L2_HIT_GPU"].sum()
    #         data.append(("MacSim", macsim_value))

    #     for name, accel_df in [
    #         ("AccelSim PTX", self.accelsim_ptx_df),
    #         ("AccelSim SASS", self.accelsim_sass_df),
    #     ]:
    #         # "float(sim[
    #         #   \"\s+L2_cache_stats_breakdown\[GLOBAL_ACC_R\]\[HIT\]
    #         #   \s*=\s*(.*)\"])"
    #         if accel_df is not None:
    #             accel_value = accel_df["l2_cache_read_hit"].sum()
    #             data.append((name, accel_value))

    #     if self.hw_df is not None:
    #         # "np.average(hw[\"l2_tex_read_transactions\"])
    #         #   *np.average(hw[\"l2_tex_read_hit_rate\"])/100",
    #         # print(self.hw_df["l2_tex_read_transactions"])
    #         # print(self.hw_df["l2_tex_read_hit_rate"].sum())
    #         hw_value = (
    #             self.hw_df["l2_tex_read_transactions"].sum()
    #             * self.hw_df["l2_tex_read_hit_rate"].sum()
    #             / 100.0
    #         )
    #         data.append(("Hardware", hw_value))

    #     df = pd.DataFrame.from_records(
    #         data,
    #         columns=["Simulator", "Value"],
    #     )
    #     df["Value"] = df["Value"].astype(int)
    #     return df


class InstructionCount(BaseMetric):
    name = "Total Instruction Count"

    def compute_m2s(self, df):
        return df["Total.Instructions"].sum()

    def compute_macsim(self, df):
        return df["INST_COUNT_TOT"][0]

    def compute_tejas(self, df):
        return df["total_inst_count"].sum() * self.data.config.spec["sm_count"]

    def compute_accelsim_ptx(self, df):
        return df["gpgpu_n_tot_w_icount"].sum()

    def compute_accelsim_sass(self, df):
        return df["gpgpu_n_tot_w_icount"].sum()

    def compute_native(self, df):
        return df["inst_issued"].sum()

    # @property
    # def config(self):
    #     return dict()

    # def compute(self):
    #     data = []
    #     if self.m2s_df is not None:
    #         m2s_value = self.m2s_df["Total.Instructions"].sum()
    #         data.append(("Multi2Sim", m2s_value))

    #     if self.macsim_df is not None:
    #         macsim_value = self.macsim_df["INST_COUNT_TOT"][0]
    #         data.append(("MacSim", macsim_value))

    #     if self.tejas_df is not None:
    #         # without the multiplication, this does not make sense
    #         tejas_value = (
    #             self.tejas_df["total_inst_count"].sum()
    #             * self.data.config.spec["sm_count"]
    #         )
    #         data.append(("GPUTejas", tejas_value))

    #     for name, accel_df in [
    #         ("AccelSim PTX", self.accelsim_ptx_df),
    #         ("AccelSim SASS", self.accelsim_sass_df),
    #     ]:
    #         if accel_df is not None:
    #             accel_value = accel_df["gpgpu_n_tot_w_icount"].sum()
    #             data.append((name, accel_value))

    #     if self.hw_df is not None:
    #         hw_value = self.hw_df["inst_issued"].sum()
    #         data.append(("Hardware", hw_value))
    #     df = pd.DataFrame.from_records(
    #         data,
    #         columns=["Simulator", "Value"],
    #     )
    #     df["Value"] = df["Value"].astype(int)
    #     return df


class IPC(BaseMetric):
    name = "Total IPC"

    def __init__(self, data, use_duration=USE_DURATION):
        self.use_duration = use_duration
        super().__init__(data)

    def compute_m2s(self, df):
        # those metrics seem to be always 0, so we compute manually
        # Device.instructionsPerCycle
        # Total.InstructionsPerCycle
        return df["Total.Instructions"].sum() / df["Device.Cycles"].sum()

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
        return self.hw_df["inst_issued"].sum() / df["gpu_tot_sim_cycle"].sum()

    def compute_accelsim_sass(self, df):
        return self.hw_df["inst_issued"].sum() / df["gpu_tot_sim_cycle"].sum()

    def compute_native(self, df):
        # todo: factor hw cycles out
        if self.use_duration:
            hw_cycles = (
                self.hw_df["Duration"].sum() * self.data.config.spec["clock_speed"]
            )
        else:
            hw_cycles = (
                self.hw_df["elapsed_cycles_sm"].sum()
                / self.data.config.spec["sm_count"]
            )
        # there is also inst_executed
        hw_value = self.hw_df["inst_issued"].sum() / hw_cycles

        # edit: there is ipc and also issued_ipc
        hw_value = self.hw_df["ipc"].sum()
        return hw_value

    # @property
    # def config(self):
    #     return dict()

    # def compute(self):
    #     data = []
    #     if self.m2s_df is not None:
    #         # those metrics seem to be always 0, so we compute manually
    #         # Device.instructionsPerCycle
    #         # Total.InstructionsPerCycle
    #         m2s_value = (
    #             self.m2s_df["Total.Instructions"].sum()
    #             / self.m2s_df["Device.Cycles"].sum()
    #         )
    #         data.append(("Multi2Sim", m2s_value))

    #     if self.macsim_df is not None:
    #         macsim_value = (
    #             self.macsim_df["INST_COUNT_TOT"][0]
    #             / self.macsim_df["CYC_COUNT_CORE_TOTAL"][0]
    #         )
    #         data.append(("MacSim", macsim_value))

    #     if self.tejas_df is not None:
    #         # their total_ipc does not make sense?
    #         tejas_value = self.tejas_df["total_ipc"].sum()
    #         # * self.data.config.spec["sm_count"]
    #         # tejas_instr = (
    #         #     self.tejas_df["total_inst_count"].sum()
    #         #     * self.data.config.spec["sm_count"]
    #         # )
    #         # tejas_value = tejas_instr / self.tejas_df["total_cycle_count"].sum()
    #         data.append(("GPUTejas", tejas_value))

    #     for name, accel_df in [
    #         ("AccelSim PTX", self.accelsim_ptx_df),
    #         ("AccelSim SASS", self.accelsim_sass_df),
    #     ]:
    #         # "np.average(hw[\"inst_issued\"]) /
    #         #   float(sim[\"gpu_tot_sim_cycle\s*=\s*(.*)\"])"
    #         if accel_df is not None and self.hw_df is not None:
    #             accel_value = (
    #                 self.hw_df["inst_issued"].sum()
    #                 / accel_df["gpu_tot_sim_cycle"].sum()
    #             )
    #             data.append((name, accel_value))

    #     if self.hw_df is not None:
    #         # "np.average(hw[\"inst_issued\"]) /
    #         #   (np.average(hw[\"elapsed_cycles_sm\"])/80)"
    #         if self.use_duration:
    #             hw_cycles = (
    #                 self.hw_df["Duration"].sum() * self.data.config.spec["clock_speed"]
    #             )
    #         else:
    #             hw_cycles = (
    #                 self.hw_df["elapsed_cycles_sm"].sum()
    #                 / self.data.config.spec["sm_count"]
    #             )
    #         # there is also inst_executed
    #         hw_value = self.hw_df["inst_issued"].sum() / hw_cycles

    #         # edit: there is ipc and also issued_ipc
    #         hw_value = self.hw_df["ipc"].sum()
    #         data.append(("Hardware", hw_value))
    #     df = pd.DataFrame.from_records(
    #         data,
    #         columns=["Simulator", "Value"],
    #         # index=["Simulator"]
    #     )
    #     df["Value"] = df["Value"].round(3)
    #     return df


# class DRAMReads(Metric):
#     name = "Total DRAM Reads"
#     log = True

#     @property
#     def config(self):
#         return dict()

#     def compute(self):
#         data = []

#         # m2s has no dram reads
#         # macsim has no dram reads

#         if self.tejas_df is not None:
#             # seems to always be 0 but we still report it
#             tejas_value = self.tejas_df["dram_total_reads"].sum()
#             data.append(("GPUTejas", tejas_value))

#         for name, accel_df in [
#             ("AccelSim PTX", self.accelsim_ptx_df),
#             ("AccelSim SASS", self.accelsim_sass_df),
#         ]:
#             # "float(sim[\"total dram reads\s*=\s*(.*)\"])"
#             if accel_df is not None:
#                 accel_value = accel_df["total_dram_reads"].sum()
#                 data.append((name, accel_value))

#         if self.hw_df is not None:
#             # np.average(hw[\"dram_read_transactions\"])
#             hw_value = self.hw_df["dram_read_transactions"].sum()
#             data.append(("Hardware", hw_value))

#         df = pd.DataFrame.from_records(
#             data,
#             columns=["Simulator", "Value"],
#         )
#         df["Value"] = df["Value"].astype(int)
#         return df


class DRAMAccesses(BaseMetric):
    name = "Total DRAM Accesses (Read/Write)"
    log = True

    # @property
    # def config(self):
    #     return dict()

    # def compute(self):
    #     data = []

    #     if self.m2s_df is not None:
    #         # RegistersWrites
    #         # RegistersReads
    #         m2s_value = self.m2s_df["Total.LDS Instructions"].sum()
    #         data.append(("Multi2Sim", m2s_value))

    #     if self.macsim_df is not None:
    #         macsim_value = self.macsim_df["TOTAL_DRAM"][0]
    #         data.append(("MacSim", macsim_value))

    #     df = pd.DataFrame.from_records(
    #         data,
    #         columns=["Simulator", "Value"],
    #     )
    #     df["Value"] = df["Value"].astype(int)
    #     return df


# class L2Reads(Metric):
#     name = "Total L2 Reads"
#     # log = True

#     @property
#     def config(self):
#         return dict()

#     def compute(self):
#         data = []

#         # m2s has no l2 reads
#         # macsim has no l2 reads
#         # tejas has no l2 reads

#         for name, accel_df in [
#             ("AccelSim PTX", self.accelsim_ptx_df),
#             ("AccelSim SASS", self.accelsim_sass_df),
#         ]:
#             # float(sim[\"\s+L2_cache_stats_breakdown\[
#             #   GLOBAL_ACC_R\]\[TOTAL_ACCESS\]\s*=\s*(.*)\"])
#             if accel_df is not None:
#                 accel_value = accel_df["l2_cache_read_total"].sum()
#                 data.append((name, accel_value))

#         if self.hw_df is not None:
#             # np.average(hw[\"l2_tex_read_transactions\"])
#             hw_value = self.hw_df["l2_tex_read_transactions"].sum()
#             data.append(("Hardware", hw_value))

#         df = pd.DataFrame.from_records(
#             data,
#             columns=["Simulator", "Value"],
#         )
#         df["Value"] = df["Value"].astype(int)
#         return df


# class SimpleMetric(Metric):
#     ACCELSIM_KEY = None
#     HW_KEY = None
#     TEJAS_KEY = None
#     MACSIM_KEY = None
#     MULTI2SIM_KEY = None

#     def compute(self):
#         data = []

#         if self.MULTI2SIM_KEY is not None and self.m2s_df is not None:
#             data.append(("Multi2Sim", self.m2s_df[self.MULTI2SIM_KEY].sum()))

#         if self.MACSIM_KEY is not None and self.macsim_df is not None:
#             data.append(("MacSim", self.macsim_df[self.MACSIM_KEY][0]))

#         if self.TEJAS_KEY is not None and self.tejas_df is not None:
#             data.append(("GPUTejas", self.tejas_df[self.TEJAS_KEY].sum()))

#         for name, accel_df in [
#             ("AccelSim PTX", self.accelsim_ptx_df),
#             ("AccelSim SASS", self.accelsim_sass_df),
#         ]:
#             if self.ACCELSIM_KEY is not None and accel_df is not None:
#                 data.append((name, accel_df[self.ACCELSIM_KEY].sum()))

#         if self.HW_KEY is not None and self.hw_df is not None:
#             data.append(("Hardware", self.hw_df[self.HW_KEY].sum()))

#         df = pd.DataFrame.from_records(
#             data,
#             columns=["Simulator", "Value"],
#         )
#         df["Value"] = df["Value"].astype(float)
#         return df


class L2Writes(BaseMetric):
    name = "Total L2 Writes"
    # log = True

    # m2s has no l2 writes
    # macsim has no l2 writes
    # tejas has no l2 writes
    def compute_accelsim_ptx(self, df):
        return df["l2_cache_write_total"].sum()

    def compute_native(self, df):
        return df["l2_tex_write_transactions"].sum()


class L2Reads(BaseMetric):
    name = "Total L2 Reads"
    # log = True

    # m2s has no l2 reads
    # macsim has no l2 reads
    # tejas has no l2 reads
    def compute_accelsim_ptx(self, df):
        return df["l2_cache_read_total"].sum()

    def compute_accelsim_sass(self, df):
        return df["l2_cache_read_total"].sum()

    def compute_native(self, df):
        return df["l2_tex_read_transactions"].sum()


class DRAMReads(BaseMetric):
    name = "Total DRAM Reads"
    log = True

    # m2s has no dram writes
    # macsim has no dram writes
    def compute_tejas(self, df):
        return df["dram_total_reads"].sum()

    def compute_accelsim_ptx(self, df):
        return df["total_dram_reads"].sum()

    def compute_accelsim_sass(self, df):
        return df["total_dram_reads"].sum()

    def compute_native(self, df):
        return df["dram_read_transactions"].sum()


class DRAMWrites(BaseMetric):
    name = "Total DRAM Writes"
    log = True

    # m2s has no dram writes
    # macsim has no dram writes
    def compute_tejas(self, df):
        return df["dram_total_writes"].sum()

    def compute_accelsim_ptx(self, df):
        return df["total_dram_writes"].sum()

    def compute_accelsim_sass(self, df):
        return df["total_dram_writes"].sum()

    def compute_native(self, df):
        return df["dram_write_transactions"].sum()

    # @property
    # def config(self):
    #     return dict()

    # def compute(self):
    #     data = []

    #     # m2s has no dram writes
    #     # macsim has no dram writes

    #     if self.tejas_df is not None:
    #         # seems to always be 0 but we still report it
    #         tejas_value = self.tejas_df["dram_total_writes"].sum()
    #         data.append(("GPUTejas", tejas_value))

    #     for name, accel_df in [
    #         ("AccelSim PTX", self.accelsim_ptx_df),
    #         ("AccelSim SASS", self.accelsim_sass_df),
    #     ]:
    #         # "float(sim[\"total dram writes\s*=\s*(.*)\"])"
    #         if accel_df is not None:
    #             accel_value = accel_df["total_dram_writes"].sum()
    #             data.append((name, accel_value))

    #     if self.hw_df is not None:
    #         # np.average(hw[\"dram_write_transactions\"])
    #         hw_value = self.hw_df["dram_write_transactions"].sum()
    #         data.append(("Hardware", hw_value))

    #     df = pd.DataFrame.from_records(
    #         data,
    #         columns=["Simulator", "Value"],
    #     )
    #     df["Value"] = df["Value"].astype(int)
    #     return df

    # @property
    # def config(self):
    #     return dict()

    # def compute(self):
    #     data = []

    #     for name, accel_df in [
    #         ("AccelSim PTX", self.accelsim_ptx_df),
    #         ("AccelSim SASS", self.accelsim_sass_df),
    #     ]:
    #         # float(sim[\"\s+L2_cache_stats_breakdown\[
    #         #   GLOBAL_ACC_R\]\[TOTAL_ACCESS\]\s*=\s*(.*)\"])
    #         if accel_df is not None:
    #             accel_value = accel_df["l2_cache_write_total"].sum()
    #             data.append((name, accel_value))

    #     if self.hw_df is not None:
    #         # np.average(hw[\"l2_tex_write_transactions\"])
    #         hw_value = self.hw_df["l2_tex_write_transactions"].sum()
    #         data.append(("Hardware", hw_value))

    #     df = pd.DataFrame.from_records(
    #         data,
    #         columns=["Simulator", "Value"],
    #     )
    #     df["Value"] = df["Value"].astype(int)
    #     return df


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
    # log = True

    # m2s has no l2 accesses
    # tejas has no l2 accesses
    def compute_accelsim_ptx(self, df):
        return df["l2_cache_write_total"].sum() + df["l2_cache_read_total"].sum()

    def compute_macsim(self, df):
        return df["L2_HIT_GPU"][0] + df["L2_MISS_GPU"][0]

    def compute_native(self, df):
        return (
            df["l2_tex_write_transactions"].sum() + df["l2_tex_read_transactions"].sum()
        )


class L2Hits(BaseMetric):
    name = "Total L2 Hits"
    # log = True

    # m2s has no l2 hits
    # tejas has no l2 hits
    def compute_accelsim_ptx(self, df):
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
    # log = True

    # m2s has no l2 misses
    # tejas has no l2 misses
    def compute_accelsim_ptx(self, df):
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
