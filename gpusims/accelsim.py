import os
import csv
from gpusims.bench import BenchmarkConfig
from pathlib import Path
import gpusims.utils as utils
from pprint import pprint  # noqa: F401


class AccelSimPTXBenchmarkConfig(BenchmarkConfig):
    @staticmethod
    def run_input(path, inp, force=False, timeout_mins=5, **kwargs):
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

        _, stdout, stderr, duration = utils.run_cmd(
            "bash " + str(tmp_run_file.absolute()),
            cwd=path,
            timeout_sec=timeout_mins * 60,
            shell=True,
        )
        print("stdout (last 15 lines):")
        print("\n".join(stdout.splitlines()[-15:]))
        print("stderr:")
        print(stderr)

        with open(str((results_dir / "sim_wall_time.csv").absolute()), "w") as f:
            output_writer = csv.writer(f)
            output_writer.writerow(["exec_time_sec"])
            output_writer.writerow([duration])

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
            timeout_sec=timeout_mins * 60,
        )
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)

        tmp_run_file.unlink()

    def load_dataframe(self, inp):
        results_dir = self.input_path(inp) / "results"
        assert results_dir.is_dir(), "{} is not a dir".format(results_dir)
        return build_accelsim_ptx_df(
            results_dir / "stats.csv",
            sim_dur_csv=results_dir / "sim_wall_time.csv",
        )


def build_accelsim_ptx_df(csv_file, sim_dur_csv=None):
    import pandas as pd

    df = pd.read_csv(csv_file)
    df = df.pivot(index=["kernel", "kernel_id"], columns=["stat"])["value"]
    df = df.reset_index()
    if sim_dur_csv is not None:
        df["sim_wall_time"] = pd.read_csv(sim_dur_csv)["exec_time_sec"]

    return df
