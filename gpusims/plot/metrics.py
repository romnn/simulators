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
    def compute(self):
        data = []
        if self.m2s_df is not None:
            m2s_value = self.m2s_df["Total.Cycles"] / self.m2s_df["Config.Device.NumSM"]
            data.append(("Multi2Sim", m2s_value.mean()))

        if self.macsim_df is not None:
            macsim_value = self.macsim_df["CYC_COUNT_CORE_TOTAL"]
            data.append(("MacSim", macsim_value.mean()))

        if self.tejas_df is not None:
            # without the multiplication, this does not make sense
            tejas_value = self.tejas_df["total_cycle_count"] * 8
            data.append(("GPUTejas", tejas_value.mean()))

        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            if accel_df is not None:
                accel_value = accel_df["gpu_tot_sim_cycle"]
                data.append((name, accel_value.mean()))

        if self.hw_df is not None:
            # hw_cycles = self.hw_df["elapsed_cycles_sm"] / self.data.config.spec["sm_count"]
            hw_value = self.hw_df["Duration"] * self.data.config.spec["clock_speed"]
            data.append(("Hardware", hw_value.mean()))

        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class L2ReadHit(Metric):
    def compute(self):
        data = []
        # if self.m2s_df is not None:
        #    m2s_cycles = self.m2s_df["Total/Cycles"] / len(cycle_columns)
        #    data.append(("Multi2Sim", m2s_cycles.mean()))
        # if self.tejas_df is not None:
        #    tejas_cycles = self.tejas_df["total_cycle_count"]
        #    data.append(("GPUTejas", tejas_cycles.mean()))
        # if self.accel_df is not None:
        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            # "float(sim[\"\s+L2_cache_stats_breakdown\[GLOBAL_ACC_R\]\[HIT\]\s*=\s*(.*)\"])"
            if accel_df is not None:
                accel_value = accel_df["l2_cache_read_total"]
                data.append((name, accel_value.mean()))

        if self.hw_df is not None:
            # "np.average(hw[\"l2_tex_read_transactions\"])*np.average(hw[\"l2_tex_read_hit_rate\"])/100",
            print(self.hw_df["l2_tex_read_transactions"])
            print(self.hw_df["l2_tex_read_hit_rate"].mean())
            hw_value = (
                self.hw_df["l2_tex_read_transactions"].mean()
                * self.hw_df["l2_tex_read_hit_rate"].mean()
                / 100.0
            )
            data.append(("Hardware", hw_value.mean()))

        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class InstructionCount(Metric):
    def compute(self):
        data = []
        if self.m2s_df is not None:
            m2s_value = self.m2s_df["Total.Instructions"].mean()
            data.append(("Multi2Sim", m2s_value))

        if self.macsim_df is not None:
            macsim_value = self.macsim_df["INST_COUNT_TOT"].mean()
            data.append(("MacSim", macsim_value))

        if self.tejas_df is not None:
            # without the multiplication, this does not make sense
            tejas_value = (
                self.tejas_df["total_inst_count"].mean() * self.data.config.spec["sm_count"]
            )
            data.append(("GPUTejas", tejas_value))

        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            if accel_df is not None:
                accel_value = accel_df["gpgpu_n_tot_w_icount"].mean()
                data.append((name, accel_value.mean()))

        if self.hw_df is not None:
            hw_value = self.hw_df["inst_issued"].mean()
            data.append(("Hardware", hw_value))
        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].round(3)
        return df


class IPC(Metric):
    def compute(self):
        data = []
        if self.m2s_df is not None:
            m2s_value = (
                self.m2s_df["Total.Instructions"].mean()
                / self.m2s_df["Device.Cycles"].mean()
            )
            data.append(("Multi2Sim", m2s_value))

        if self.macsim_df is not None:
            macsim_value = (
                self.macsim_df["INST_COUNT_TOT"].mean()
                / self.macsim_df["CYC_COUNT_CORE_TOTAL"].mean()
            )
            data.append(("MacSim", macsim_value))

        if self.tejas_df is not None:
            # their total_ipc does not make sense?
            # tejas_value = self.tejas_df["total_ipc"].mean()
            # * self.data.config.spec["sm_count"]
            tejas_instr = (
                self.tejas_df["total_inst_count"].mean() * self.data.config.spec["sm_count"]
            )
            tejas_value = tejas_instr / self.tejas_df["total_cycle_count"].mean()
            data.append(("GPUTejas", tejas_value))

        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            # "np.average(hw[\"inst_issued\"]) /
            #   float(sim[\"gpu_tot_sim_cycle\s*=\s*(.*)\"])"
            if accel_df is not None and self.hw_df is not None:
                accel_value = (
                    self.hw_df["inst_issued"].mean()
                    / accel_df["gpu_tot_sim_cycle"].mean()
                )
                data.append((name, accel_value.mean()))

        if self.hw_df is not None:
            # "np.average(hw[\"inst_issued\"]) /
            #   (np.average(hw[\"elapsed_cycles_sm\"])/80)"
            hw_cycles = (
                self.hw_df["elapsed_cycles_sm"].mean() / self.data.config.spec["sm_count"]
            )
            hw_value = self.hw_df["inst_issued"].mean() / hw_cycles
            data.append(("Hardware", hw_value))
        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].round(3)
        return df


class DRAMReads(Metric):
    def compute(self):
        data = []
        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            # "float(sim[\"total dram reads\s*=\s*(.*)\"])"
            if accel_df is not None:
                accel_value = accel_df["total_dram_reads"].mean()
                data.append((name, accel_value.mean()))

        if self.hw_df is not None:
            # np.average(hw[\"dram_read_transactions\"])
            hw_value = self.hw_df["dram_read_transactions"].mean()
            data.append(("Hardware", hw_value))
        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df


class DRAMWrites(Metric):
    def compute(self):
        data = []
        for name, accel_df in [
            ("AccelSim PTX", self.accelsim_ptx_df),
            ("AccelSim SASS", self.accelsim_sass_df),
        ]:
            # "float(sim[\"total dram writes\s*=\s*(.*)\"])"
            if accel_df is not None:
                accel_value = accel_df["total_dram_writes"].mean()
                data.append((name, accel_value.mean()))

        if self.hw_df is not None:
            # np.average(hw[\"dram_write_transactions\"])
            hw_value = self.hw_df["dram_write_transactions"].mean()
            data.append(("Hardware", hw_value))
        df = pd.DataFrame.from_records(
            data, columns=["Simulator", "Value"], index=["Simulator"]
        )
        df["Value"] = df["Value"].astype(int)
        return df
