import os
from gpusims.bench import BenchmarkConfig
from pathlib import Path
import gpusims.utils as utils
from pprint import pprint  # noqa: F401


class AccelSimBenchmarkConfig(BenchmarkConfig):
    def run_input(self, inp, force=False):
        print("accelsim run:", inp)
        sim_root = Path(os.environ["SIM_ROOT"])
        setup_env = sim_root / "setup_environment"
        assert setup_env.is_file()
        utils.chmod_x(setup_env)

        executable = self.path / inp.executable
        assert executable.is_file()
        utils.chmod_x(executable)

        results_dir = self.path / "results"
        os.makedirs(str(results_dir.absolute()), exist_ok=True)
        log_file = results_dir / "log.txt"

        tmp_run = ""
        tmp_run += "source {}\n".format(str(setup_env.absolute()))
        tmp_run += "{} {}\n".format(str(executable.absolute()), inp.args)
        print(tmp_run)

        tmp_run_file = self.path / "run.tmp.sh"
        with open(str(tmp_run_file.absolute()), "w") as f:
            f.write(tmp_run)

        _, stdout, _ = utils.run_cmd(
            "bash " + str(tmp_run_file.absolute()),
            cwd=self.path,
            timeout_sec=5 * 60,
            shell=True,
        )
        print("stdout:")
        print(stdout[-100:])

        with open(str(log_file.absolute()), "w") as f:
            f.write(stdout)

        # parse the log file
        stat_file = results_dir / "stats.csv"
        utils.run_cmd(
            [
                "gpgpusim-parse",
                "--input",
                str(log_file.absolute()),
                "--output",
                str(stat_file.absolute()),
            ],
            cwd=self.path,
            timeout_sec=1 * 60,
            # shell=True,
        )

        tmp_run_file.unlink()
