from gpusims.bench import BenchmarkConfig


class Multi2SimBenchmarkConfig(BenchmarkConfig):
    def run_input(self, inp, force=False):
        print("multi2sim run:", inp)
