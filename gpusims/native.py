from gpusims.bench import BenchmarkConfig


class NativeBenchmarkConfig(BenchmarkConfig):
    def run_input(self, inp, force=False):
        print("native run:", inp)
