import os
from gpusims.bench import BenchmarkConfig
import gpusims.utils as utils


class Multi2SimBenchmarkConfig(BenchmarkConfig):
    @staticmethod
    def run_input(path, inp, force=False, **kwargs):
        print("multi2sim run:", inp)

        executable = path / inp.executable
        assert executable.is_file()
        utils.chmod_x(executable)

        results_dir = path / "results"
        os.makedirs(str(results_dir.absolute()), exist_ok=True)
        log_file = results_dir / "log.txt"
        stats_file = results_dir / "stats.txt"

        cmd = [
            "m2s",
            "--kpl-report",
            str(stats_file.absolute()),
            "--kpl-config",
            str((path / "m2s.config.ini").absolute()),
            "--kpl-sim",
            "detailed",
            str(executable.absolute()),
            inp.args,
        ]
        cmd = " ".join(cmd)
        _, stdout, stderr = utils.run_cmd(
            cmd,
            cwd=path,
            timeout_sec=5 * 60,
        )
        print("stdout:")
        print(stdout)

        print("stderr:")
        print(stderr)

        with open(str(log_file.absolute()), "w") as f:
            f.write(stderr)

        # parse the stats file
        csv_file = stats_file.with_suffix(".csv")
        utils.run_cmd(
            [
                "m2s-parse",
                "--input",
                str(stats_file.absolute()),
                "--output",
                str(csv_file.absolute()),
            ],
            cwd=path,
            timeout_sec=1 * 60,
        )
