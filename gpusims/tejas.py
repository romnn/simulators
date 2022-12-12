import os
import re
from pathlib import Path
from gpusims.bench import BenchmarkConfig
import multiprocessing
from pprint import pprint  # noqa: F401
import xml.etree.ElementTree as ET
import gpusims.utils as utils


def build_config(config_file, threads):
    tree = ET.parse(str(config_file.absolute()))
    root = tree.getroot()
    root.find("./Simulation/MaxNumJavaThreads").text = str(threads)
    return tree


class TejasBenchmarkConfig(BenchmarkConfig):
    @staticmethod
    def run_input(path, inp, force=False, **kwargs):
        print("tejas run:", inp, inp.args)

        threads = multiprocessing.cpu_count()
        threads = 8
        tejas_root = Path(os.environ["TEJAS_ROOT"])

        default_config_file = path / "tejas_config.xml"
        new_config = build_config(default_config_file, threads)

        new_config_file = path / "config.xml"
        print("building config {} for {} threads".format(str(new_config_file), threads))
        new_config.write(str(new_config_file.absolute()))

        results_dir = path / "results"
        trace_dir = results_dir / str(threads)
        utils.ensure_empty(trace_dir)
        print(trace_dir)

        tracegen = path / "tracegen"
        utils.chmod_x(tracegen)
        # tracegen.chmod(tracegen.stat().st_mode | stat.S_IEXEC)

        cmd = [str(tracegen.absolute()), inp.args, str(threads)]
        cmd = " ".join(cmd)
        utils.run_cmd(cmd, cwd=path, timeout_sec=5 * 60)

        # check number of kernels
        kernels = 0
        with open(str((path / "0.txt").absolute()), "r") as f:
            kernels = len([line for line in f.readlines() if "KERNEL START" in line])
        print("kernels={}".format(kernels))

        for txt_file in list(path.glob("*.txt")):
            if re.match(r"\d+", txt_file.name):
                txt_file.rename(trace_dir / txt_file.name)

        simplifier = tejas_root / "../gputejas/Tracesimplifier.jar"
        assert simplifier.is_file()

        cmd = [
            "java -jar",
            str(simplifier.absolute()),
            str(new_config_file.absolute()),
            "tmp",
            str(trace_dir.parent.absolute()),
            str(kernels),
        ]
        cmd = " ".join(cmd)
        _, stdout, stderr = utils.run_cmd(cmd, cwd=path, timeout_sec=5 * 60)
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)

        kernels = len(list(trace_dir.glob("hashfile_*")))
        print("kernels:", kernels)

        tejas_simulator = tejas_root / "../gputejas/jars/GPUTejas.jar"
        assert tejas_simulator.is_file()

        log_file = results_dir / "stats.txt"
        cmd = [
            "java -jar",
            str(tejas_simulator.absolute()),
            str(new_config_file.absolute()),
            str(log_file.absolute()),
            str(trace_dir.parent.absolute()),
            str(kernels),
        ]
        cmd = " ".join(cmd)
        _, stdout, stderr = utils.run_cmd(cmd, cwd=path, timeout_sec=5 * 60)
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)

        # parse the stats file
        stat_file = log_file.with_suffix(".csv")
        _, stdout, stderr = utils.run_cmd(
            [
                "tejas-parse",
                "--input",
                str(log_file.absolute()),
                "--output",
                str(stat_file.absolute()),
            ],
            cwd=path,
            timeout_sec=1 * 60,
        )
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)