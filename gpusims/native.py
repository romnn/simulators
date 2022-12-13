import re
import os
import csv
from pprint import pprint  # noqa: F401
from gpusims.bench import BenchmarkConfig
import gpusims.utils as utils


def convert_hw_csv(csv_file, output_csv_file):
    with open(str(csv_file.absolute()), "rU") as f:
        reader = csv.reader(f)

        # find the start of the csv dump
        prof_pat = re.compile(r"^==\d*==\s*Profiling result:\s*$")
        prof_abort = re.compile(r"^==PROF== Disconnected\s*$")
        for row in reader:
            line = ", ".join(row)
            # print(line)
            if prof_pat.match(line) is not None:
                # print("found start")
                break
            if prof_abort.match(line) is not None:
                # print("found abort")
                break

        os.makedirs(output_csv_file.parent, exist_ok=True)
        with open(str(output_csv_file.absolute()), "w") as out:
            output_writer = csv.writer(out)

            # write the valid csv rows to new file
            for row in reader:
                output_writer.writerow(row)
        print("parsed stats:", output_csv_file.absolute())


class NativeBenchmarkConfig(BenchmarkConfig):
    @staticmethod
    def run_input(path, inp, repetitions=1, force=False, timeout_mins=5, **kwargs):
        print("native run:", inp)
        print("repetitions:", repetitions)
        print("timeout mins:", timeout_mins)
        # export CUDA_VISIBLE_DEVICES="0"

        results_dir = path / "results"
        os.makedirs(str(results_dir.absolute()), exist_ok=True)

        executable = path / inp.executable
        assert executable.is_file()
        utils.chmod_x(executable)

        for r in range(repetitions):
            log_file = results_dir / "result.txt.{}".format(r)

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
                _, stdout, _ = utils.run_cmd(
                    cmd, cwd=path, timeout_sec=timeout_mins * 60
                )
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

            cycles_log_file = results_dir / "result.cycles.txt.{}".format(r)
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
                _, stdout, _ = utils.run_cmd(
                    cycles_cmd, cwd=path, timeout_sec=timeout_mins * 60
                )
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

            # convert the csv files
            convert_hw_csv(log_file, results_dir / "result.csv.{}".format(r))
            convert_hw_csv(
                cycles_log_file, results_dir / "result.cycles.csv.{}".format(r)
            )
