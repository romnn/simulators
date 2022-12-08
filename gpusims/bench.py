import yaml
import abc
import shutil
from pathlib import Path
from pprint import pprint


class BenchmarkConfig(abc.ABC):
    def __init__(self, run_dir, benchmark, config, path=None):
        self.benchmark = benchmark
        self.config = config
        benchmark_name = benchmark.name.lower().replace(" ", "_")
        config_name = config.name.lower().replace(" ", "_")
        self.path = path or Path(run_dir) / benchmark_name / config_name

    def __repr__(self):
        return f"{self.__class__.__name__}({self.benchmark}, {self.config})"

    @abc.abstractmethod
    def run_input(self, inp, force=False):
        pass

    def run(self, force=False):
        for inp in self.benchmark.inputs:
            print("running input:", inp)
            assert inp.executable.is_file()
            self.run_input(inp, force=force)
            # do not create run scripts
            # do that on the fly here
            # input_name = inp.name()
            # run_file = self.path / input_name
            # # run_file = run_file.with_suffix(".sh")
            # # run_file = run_file.with_extension(".sh")
            # # result_file = self.path / "_".join(inp).replace(" ", "_")
            # # input_dir
            # # path = path.with_extension
            # print("run file:", run_file)

    def setup(self):
        """setup the benchmark in given run dir"""
        print(self.path)
        # self.path.mkdir(parents=True, exist_ok=True)
        # copy files to run dir
        benchmark_files = {
            f: f.relative_to(self.benchmark.path)
            for f in list(self.benchmark.path.rglob("*"))
        }
        pprint(benchmark_files)

        # copy config to run dir
        config_files = {
            f: f.relative_to(self.config.path)
            for f in list(self.config.path.rglob("*"))
        }
        pprint(config_files)

        for src, rel in {**config_files, **benchmark_files}.items():
            dest = self.path / rel
            print(f"cp {src} to {dest}")
            # shutil.copyfile(src, dest)


class Benchmark:
    def __init__(self, name, path, executable, inputs=[]):
        assert path.is_dir()
        self.name = name
        self.path = path
        self.executable = executable
        self.inputs = inputs

    def __repr__(self):
        return f"{self.__class__.__name__}({self.path})"


class Input:
    def __init__(self, executable, args=None):
        self.executable = executable
        self.args = ""
        if isinstance(args, list):
            self.args = " ".join(args)

    def name(self):
        input_name = self.args
        input_name = input_name.replace(" ", "_")
        input_name = input_name.replace(".", "_")
        return input_name

    def __repr__(self):
        return f"{self.__class__.__name__}({self.executable.name + self.args})"


def parse_benchmarks(path):
    benchmarks = {}
    with open(path, "r") as f:
        benchmarks_yaml = yaml.load(f, Loader=yaml.FullLoader)
        # pprint(benchmarks_yaml)
        for name, config in benchmarks_yaml.items():
            bench_path = path.parent / config["path"]
            executable = bench_path / config["executable"]
            inputs = []
            for inp in config.get("inputs", []):
                inputs.append(Input(executable, args=inp.get("args")))

            benchmarks[name.lower()] = Benchmark(
                name=name,
                path=bench_path,
                executable=executable,
                inputs=inputs,
            )
    return benchmarks
