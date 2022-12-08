from gpusims.bench import BenchmarkConfig


class TejasBenchmarkConfig(BenchmarkConfig):
    def run_input(self, inp, force=False):
        print("tejas run:", inp)
