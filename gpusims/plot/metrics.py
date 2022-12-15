import abc
import pandas as pd
import gpusims


class Metric(abc.ABC):
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
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class ExecutionTime(Metric):
    @property
    def config(self):
        return dict(
            log=True,
            unit="s",
        )

    def compute(self):
        data = []
        if self.m2s_df is not None:
            # todo
            # data.append(("Multi2Sim", m2s_value))
            pass

        if self.macsim_df is not None:
            macsim_value = self.macsim_df["EXE_TIME"][0]
            data.append(("MacSim", macsim_value))

        if self.tejas_df is not None:
            tejas_value = self.tejas_df["sim_time_secs"].sum()
            data.append(("GPUTejas", tejas_value))

        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            if accel_df is not None:
                accel_value = accel_df["gpgpu_simulation_time_sec"].sum()
                data.append((name, accel_value))

        if self.hw_df is not None:
            # convert us to seconds
            hw_value = self.hw_df["Duration"].sum() * 1e-6
            # print(self.hw_df["Duration"].sum())
            # print("hw exec time:", hw_value)
            data.append(("Hardware", hw_value))

        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].round(6)
        return df


class L2ReadHit(Metric):
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
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class InstructionCount(Metric):
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
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class IPC(Metric):
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
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].round(3)
        return df


class DRAMReads(Metric):
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
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class DRAMWrites(Metric):
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
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df
