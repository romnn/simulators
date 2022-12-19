import yaml
import abc
import os
import shutil
from pathlib import Path
from pprint import pprint  # noqa: F401
import gpusims.utils as utils


class BenchmarkConfig(abc.ABC):
    def __init__(self, run_dir, benchmark, config, path=None):
        self.benchmark = benchmark
        self.config = config
        config_name = utils.slugify(config.key.lower())
        self.path = path or Path(run_dir) / config_name / benchmark.sanitized_name()

    def __repr__(self):
        return "{}({}, {})".format(self.__class__.__name__, self.benchmark, self.config)

    @staticmethod
    @abc.abstractmethod
    def _run(path, inp, **kwargs):
        pass

    @abc.abstractmethod
    def load_dataframe(self, inp):
        pass

    def input_path(self, inp):
        return self.path / inp.sanitized_name()

    def run(self, inp, **kwargs):
        path = self.input_path(inp)
        self.setup(path)
        try:
            kwargs["repetitions"] = int(self.benchmark.extra["repetitions"])
        except (KeyError, ValueError):
            pass
        self._run(path=path, inp=inp, **kwargs)

    def setup(self, path):
        """setup the benchmark in given run dir"""
        print("setup {} in {}".format(self.benchmark.name, path))
        utils.ensure_exists(path)

        # copy files to run dir
        benchmark_files = {
            f: f.relative_to(self.benchmark.path)
            for f in list(self.benchmark.path.rglob("*"))
        }
        # pprint(benchmark_files)

        # copy config to run dir
        config_files = {}
        if self.config.path is not None:
            config_files = {
                f: f.relative_to(self.config.path)
                for f in list(self.config.path.rglob("*"))
            }
            # pprint(config_files)

        for src, rel in utils.merge_dicts(config_files, benchmark_files).items():
            if src.is_file():
                dest = path / rel
                # print("cp {} to {}".format(src, dest))
                os.makedirs(str(dest.parent.absolute()), exist_ok=True)
                shutil.copyfile(str(src.absolute()), str(dest.absolute()))


class Benchmark:
    def __init__(self, name, path, executable, extra=None, inputs=None):
        assert path.is_dir()
        self.name = name
        self.path = path
        self.executable = executable
        self.inputs = inputs
        self.extra = extra

    def sanitized_name(self):
        return utils.slugify(self.name.lower())

    def enabled(self, simulator):
        try:
            return self.extra[simulator]["enabled"]
        except KeyError:
            return True

    def __repr__(self):
        return "{}({})".format(self.__class__.__name__, self.path)


class Input:
    def __init__(self, executable, args=None, extra=None):
        self.executable = executable
        self.args = ""
        self.extra = extra or dict()
        if args is not None:
            if isinstance(args, list):
                self.args = " ".join(args)
            else:
                self.args = str(args)

    def enabled(self, sim):
        extra = self.extra.get(sim)
        if isinstance(extra, dict):
            return extra.get("enabled", True)
        return True

    def sanitized_name(self):
        sanitized = utils.slugify(self.args)
        return "input-" + sanitized

    def __repr__(self):
        return "{}({} {})".format(self.__class__.__name__, self.executable, self.args)


def parse_benchmarks(path):
    benchmarks = {}
    with open(str(path.absolute()), "r") as f:
        try:
            benchmarks_yaml = yaml.load(f)
        except TypeError:
            benchmarks_yaml = yaml.load(f, Loader=yaml.FullLoader)

        # pprint(benchmarks_yaml)
        for name, config in benchmarks_yaml.items():
            bench_path = path.parent / config["path"]
            executable = config["executable"]
            inputs = []
            for inp in config.get("inputs", []):
                inputs.append(Input(executable, args=inp.get("args"), extra=inp))

            benchmarks[name.lower()] = Benchmark(
                name=name,
                path=bench_path,
                executable=executable,
                inputs=inputs,
                extra=config,
            )
    return benchmarks
