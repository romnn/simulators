import os
from gpusims.bench import BenchmarkConfig


class AccelSimBenchmarkConfig(BenchmarkConfig):
    # def sim_config(self):
    #     os.environ["SIM_ROOT"]

    def check(self):
        if os.environ.get("GPGPUSIM_SETUP_ENVIRONMENT_WAS_RUN") != "1":
            raise AssertionError("run setup_environment first")

    def run_input(self, inp, force=False):
        # sim_root = os.environ["SIM_ROOT"]
        # print(sim_root)
        # run setup_environment before
        # sp.check_output
        # self.sim_config()
        # self.check()
        print("accelsim run:", inp)
