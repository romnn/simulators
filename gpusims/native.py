import os
from pprint import pprint  # noqa: F401
from gpusims.bench import BenchmarkConfig
import gpusims.utils as utils


class NativeBenchmarkConfig(BenchmarkConfig):
    def run_input(self, inp, force=False):
        # print(self.benchmark.name, self.path)
        print("native run:", inp)
        # export CUDA_VISIBLE_DEVICES="0"

        results_dir = self.path / "results"
        os.makedirs(str(results_dir.absolute()), exist_ok=True)

        log_file = results_dir / "result.txt"

        executable = self.path / inp.executable
        assert executable.is_file()
        utils.chmod_x(executable)

        cmd = [
            "nvprof",
            "--unified-memory-profiling",
            "off",
            "--concurrent-kernels",
            "off",
            "--print-gpu-trace",
            "-u",
            "us",
            "--demangling",
            "off",
            "--csv",
            "--log-file",
            str(log_file.absolute()),
            str(executable.absolute()),
            inp.args,
        ]
        cmd = " ".join(cmd)
        try:
            _, stdout, _ = utils.run_cmd(cmd, cwd=self.path, timeout_sec=5 * 60)
            print("stdout:")
            print(stdout)
            with open(str(log_file.absolute()), "r") as f:
                print("log file:")
                print(f.read())

        except utils.ExecError as e:
            with open(str(log_file.absolute()), "r") as f:
                print("log file:")
                print(f.read())
            raise e

        cycles_log_file = results_dir / "result.cycles.txt"
        cycles_cmd = [
            "nvprof",
            "--unified-memory-profiling",
            "off",
            "--concurrent-kernels",
            "off",
            "--print-gpu-trace",
            "--events",
            "elapsed_cycles_sm",
            "-u",
            "us",
            "--metrics",
            "all",
            "--demangling",
            "off",
            "--csv",
            "--log-file",
            str(cycles_log_file.absolute()),
            str(executable.absolute()),
            inp.args,
        ]
        cycles_cmd = " ".join(cycles_cmd)
        try:
            _, stdout, _ = utils.run_cmd(cycles_cmd, cwd=self.path, timeout_sec=5 * 60)
            print("stdout:")
            print(stdout)
            with open(str(cycles_log_file.absolute()), "r") as f:
                print("log file:")
                print(f.read())

        except utils.ExecError as e:
            with open(str(cycles_log_file.absolute()), "r") as f:
                print("log file:")
                print(f.read())
            raise e
