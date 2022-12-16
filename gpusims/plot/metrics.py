import abc
import pandas as pd
import gpusims


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


class Cycles(Metric):
    name = "Cycles"

    @property
    def config(self):
        return dict(
            log=True,
        )

    def compute(self):
        data = []
        if self.m2s_df is not None:
            m2s_value = (
                self.m2s_df["Total.Cycles"].sum()
                / self.m2s_df["Config.Device.NumSM"].mean()
            )
            data.append(("Multi2Sim", m2s_value))

        if self.macsim_df is not None:
            macsim_value = self.macsim_df["CYC_COUNT_CORE_TOTAL"][0]
            data.append(("MacSim", macsim_value))

        if self.tejas_df is not None:
            # without the multiplication, this does not make sense
            tejas_value = self.tejas_df["total_cycle_count"].sum()
            # * 8
            data.append(("GPUTejas", tejas_value))

        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            if accel_df is not None:
                accel_value = accel_df["gpu_tot_sim_cycle"].sum()
                data.append((name, accel_value))

        if self.hw_df is not None:
            hw_value = (
                self.hw_df["elapsed_cycles_sm"].sum()
                / self.data.config.spec["sm_count"]
            )
            # hw_value = self.hw_df["elapsed_cycles_sm"].sum()
            # clock speed is mhz, so *1e6
            # duration is us, so *1e-6
            # unit conversions cancel each other out
            # hw_duration = self.hw_df["Duration"].sum()
            # hw_value = hw_duration * self.data.config.spec["clock_speed"]
            data.append(("Hardware", hw_value))

        df = pd.DataFrame.from_records(
            data,
            columns=["Simulator", "Value"],
            # index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class ExecutionTime(Metric):
    name = "Execution Time"
    unit = "s"

    @property
    def config(self):
        return dict(
            log=True,
        )

    def compute(self):
        # data = []
        m2s_sim = 0
        if self.m2s_df is not None:
            m2s_sim = self.m2s_df["sim_wall_time"].sum()
            # m2s_trace = self.m2s_df["trace_wall_time"].sum()
        # data.append(("Multi2Sim", "Sim", m2s_sim))
        # data.append(("Multi2Sim", "Trace", m2s_trace))

        macsim_sim, macsim_trace = 0, 0
        if self.macsim_df is not None:
            macsim_sim = self.macsim_df["EXE_TIME"][0]
            macsim_trace = self.macsim_df["trace_wall_time"].sum()
        # data.append(("MacSim", "Sim", macsim_sim))
        # data.append(("MacSim", "Trace", macsim_trace))

        tejas_sim, tejas_trace = 0, 0
        if self.tejas_df is not None:
            tejas_sim = self.tejas_df["sim_time_secs"].sum()
            tejas_trace = self.tejas_df["trace_wall_time"].sum()
        # data.append(("GPUTejas", "Sim", tejas_sim))
        # data.append(("GPUTejas", "Trace", tejas_trace))

        accel_ptx_sim = 0
        if self.accelsim_ptx_df is not None:
            accel_ptx_sim = self.accelsim_ptx_df["gpgpu_simulation_time_sec"].sum()
        # data.append(("AccelSim PTX", "Sim", accel_sim))
        # data.append(("AccelSim PTX", "Trace", 0))

        accel_sass_sim, accel_sass_trace = 0, 0
        if self.accelsim_sass_df is not None:
            accel_sass_sim = self.accelsim_sass_df["gpgpu_simulation_time_sec"].sum()
            accel_sass_trace = self.accelsim_sass_df["trace_wall_time"].sum()
        # data.append(("AccelSim SASS", "Sim", accel_sim))
        # data.append(("AccelSim SASS", "Trace", accel_trace))

        hw_value = 0
        if self.hw_df is not None:
            # convert us to seconds
            hw_value = self.hw_df["Duration"].sum() * 1e-6
            # print(self.hw_df["Duration"].sum())
            # print("hw exec time:", hw_value)
        # data.append(("Hardware", "Sim", hw_value))
        # data.append(("Hardware", "Trace", 0))

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
            # index=["Simulator", "Kind"]
            # index=["Kind"]
        )
        df["Value"] = df["Value"].round(6)
        return df


class L2ReadHit(Metric):
    name = "Total L2 Read Hits"

    @property
    def config(self):
        return dict()

    def compute(self):
        data = []
        # if self.m2s_df is not None:
        #    m2s_cycles = self.m2s_df["Total/Cycles"] / len(cycle_columns)
        #    data.append(("Multi2Sim", m2s_cycles.sum()))
        # if self.tejas_df is not None:
        #    tejas_cycles = self.tejas_df["total_cycle_count"]
        #    data.append(("GPUTejas", tejas_cycles.sum()))
        # if self.accel_df is not None:
        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            # "float(sim[
            #   \"\s+L2_cache_stats_breakdown\[GLOBAL_ACC_R\]\[HIT\]
            #   \s*=\s*(.*)\"])"
            if accel_df is not None:
                accel_value = accel_df["l2_cache_read_total"].sum()
                data.append((name, accel_value))

        if self.hw_df is not None:
            # "np.average(hw[\"l2_tex_read_transactions\"])
            #   *np.average(hw[\"l2_tex_read_hit_rate\"])/100",
            # print(self.hw_df["l2_tex_read_transactions"])
            # print(self.hw_df["l2_tex_read_hit_rate"].sum())
            hw_value = (
                self.hw_df["l2_tex_read_transactions"].sum()
                * self.hw_df["l2_tex_read_hit_rate"].sum()
                / 100.0
            )
            data.append(("Hardware", hw_value))

        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"],
            # index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class InstructionCount(Metric):
    name = "Total Instruction Count"

    @property
    def config(self):
        return dict()

    def compute(self):
        data = []
        if self.m2s_df is not None:
            m2s_value = self.m2s_df["Total.Instructions"].sum()
            data.append(("Multi2Sim", m2s_value))

        if self.macsim_df is not None:
            macsim_value = self.macsim_df["INST_COUNT_TOT"][0]
            data.append(("MacSim", macsim_value))

        if self.tejas_df is not None:
            # without the multiplication, this does not make sense
            tejas_value = (
                self.tejas_df["total_inst_count"].sum()
                * self.data.config.spec["sm_count"]
            )
            data.append(("GPUTejas", tejas_value))

        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            if accel_df is not None:
                accel_value = accel_df["gpgpu_n_tot_w_icount"].sum()
                data.append((name, accel_value))

        if self.hw_df is not None:
            hw_value = self.hw_df["inst_issued"].sum()
            data.append(("Hardware", hw_value))
        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"],
            # index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class IPC(Metric):
    name = "Total IPC"

    @property
    def config(self):
        return dict()

    def compute(self):
        data = []
        if self.m2s_df is not None:
            m2s_value = (
                self.m2s_df["Total.Instructions"].sum()
                / self.m2s_df["Device.Cycles"].sum()
            )
            data.append(("Multi2Sim", m2s_value))

        if self.macsim_df is not None:
            macsim_value = (
                self.macsim_df["INST_COUNT_TOT"][0]
                / self.macsim_df["CYC_COUNT_CORE_TOTAL"][0]
            )
            data.append(("MacSim", macsim_value))

        if self.tejas_df is not None:
            # their total_ipc does not make sense?
            tejas_value = self.tejas_df["total_ipc"].sum()
            # * self.data.config.spec["sm_count"]
            # tejas_instr = (
            #     self.tejas_df["total_inst_count"].sum()
            #     * self.data.config.spec["sm_count"]
            # )
            # tejas_value = tejas_instr / self.tejas_df["total_cycle_count"].sum()
            data.append(("GPUTejas", tejas_value))

        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            # "np.average(hw[\"inst_issued\"]) /
            #   float(sim[\"gpu_tot_sim_cycle\s*=\s*(.*)\"])"
            if accel_df is not None and self.hw_df is not None:
                accel_value = (
                    self.hw_df["inst_issued"].sum()
                    / accel_df["gpu_tot_sim_cycle"].sum()
                )
                data.append((name, accel_value))

        if self.hw_df is not None:
            # "np.average(hw[\"inst_issued\"]) /
            #   (np.average(hw[\"elapsed_cycles_sm\"])/80)"
            hw_cycles = (
                self.hw_df["elapsed_cycles_sm"].sum()
                / self.data.config.spec["sm_count"]
            )
            hw_value = self.hw_df["inst_issued"].sum() / hw_cycles
            data.append(("Hardware", hw_value))
        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"],
            # index=["Simulator"]
        )
        df["Value"] = df["Value"].round(3)
        return df


class DRAMReads(Metric):
    name = "Total DRAM Reads"

    @property
    def config(self):
        return dict()

    def compute(self):
        data = []

        if self.m2s_df is not None:
            # warning: there are no dram reads
            # is this the same?
            m2s_value = self.m2s_df["Total.LDS Instructions"].sum()
            data.append(("Multi2Sim", m2s_value))

        if self.macsim_df is not None:
            macsim_value = self.macsim_df["TOTAL_DRAM"][0]
            data.append(("MacSim", macsim_value))

        if self.tejas_df is not None:
            # seems to always be 0 but we still report it
            tejas_value = self.tejas_df["dram_total_reads"].sum()
            data.append(("GPUTejas", tejas_value))

        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            # "float(sim[\"total dram reads\s*=\s*(.*)\"])"
            if accel_df is not None:
                accel_value = accel_df["total_dram_reads"].sum()
                data.append((name, accel_value))

        if self.hw_df is not None:
            # np.average(hw[\"dram_read_transactions\"])
            hw_value = self.hw_df["dram_read_transactions"].sum()
            data.append(("Hardware", hw_value))
        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"],
            # index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class DRAMWrites(Metric):
    name = "Total DRAM Writes"

    @property
    def config(self):
        return dict()

    def compute(self):
        data = []

        if self.tejas_df is not None:
            # seems to always be 0 but we still report it
            tejas_value = self.tejas_df["dram_total_writes"].sum()
            data.append(("GPUTejas", tejas_value))

        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            # "float(sim[\"total dram writes\s*=\s*(.*)\"])"
            if accel_df is not None:
                accel_value = accel_df["total_dram_writes"].sum()
                data.append((name, accel_value))

        if self.hw_df is not None:
            # np.average(hw[\"dram_write_transactions\"])
            hw_value = self.hw_df["dram_write_transactions"].sum()
            data.append(("Hardware", hw_value))
        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"],
            # index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df
