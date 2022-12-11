import os
from gpusims.bench import BenchmarkConfig
from pathlib import Path
import gpusims.utils as utils
from pprint import pprint  # noqa: F401


class AccelSimPTXBenchmarkConfig(BenchmarkConfig):
    @staticmethod
    def run_input(path, inp, force=False, **kwargs):
        print("accelsim PTX run:", inp)
        sim_root = Path(os.environ["SIM_ROOT"])
        setup_env = sim_root / "setup_environment"
        assert setup_env.is_file()
        utils.chmod_x(setup_env)

        executable = path / inp.executable
        assert executable.is_file()
        utils.chmod_x(executable)

        results_dir = path / "results"
        os.makedirs(str(results_dir.absolute()), exist_ok=True)
        log_file = results_dir / "log.txt"

        tmp_run_sh = "set -e\n"
        tmp_run_sh += "source {}\n".format(str(setup_env.absolute()))
        tmp_run_sh += "{} {}\n".format(str(executable.absolute()), inp.args)
        print("\nrunning:\n")
        print(tmp_run_sh)
        print("")

        tmp_run_file = path / "run.tmp.sh"
        with open(str(tmp_run_file.absolute()), "w") as f:
            f.write(tmp_run_sh)

        _, stdout, _ = utils.run_cmd(
            "bash " + str(tmp_run_file.absolute()),
            cwd=path,
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
            cwd=path,
            timeout_sec=1 * 60,
        )

        tmp_run_file.unlink()
