import os
from gpusims.bench import BenchmarkConfig
import gpusims.utils as utils


class Multi2SimBenchmarkConfig(BenchmarkConfig):
    def run_input(self, inp, force=False):
        print("multi2sim run:", inp)

        # sim_root = Path(os.environ["SIM_ROOT"])
        # setup_env = sim_root / "setup_environment"
        # assert setup_env.is_file()
        # utils.chmod_x(setup_env)

        executable = self.path / inp.executable
        assert executable.is_file()
        utils.chmod_x(executable)

        results_dir = self.path / "results"
        os.makedirs(str(results_dir.absolute()), exist_ok=True)
        log_file = results_dir / "log.txt"
        stats_file = results_dir / "stats.txt"

        cmd = [
            "m2s",
            "--kpl-report",
            str(stats_file.absolute()),
            "--kpl-sim",
            "detailed",
            str(executable.absolute()),
            inp.args,
        ]
        cmd = " ".join(cmd)
        _, stdout, stderr = utils.run_cmd(
            cmd,
            cwd=self.path,
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
            cwd=self.path,
            timeout_sec=1 * 60,
        )
