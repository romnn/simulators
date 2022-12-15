import os
import csv
from gpusims.bench import BenchmarkConfig
import gpusims.utils as utils


class Multi2SimBenchmarkConfig(BenchmarkConfig):
    @staticmethod
    def run_input(path, inp, force=False, timeout_mins=5, **kwargs):
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
        _, stdout, stderr, duration = utils.run_cmd(
            cmd,
            cwd=path,
            timeout_sec=timeout_mins * 60,
        )
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)

        with open(str((results_dir / "sim_wall_time.csv").absolute()), "w") as f:
            output_writer = csv.writer(f)
            output_writer.writerow(["exec_time_sec"])
            output_writer.writerow([duration])

        with open(str(log_file.absolute()), "w") as f:
            f.write(stderr)

        # parse the stats file
        csv_file = stats_file.with_suffix(".csv")
        _, stdout, stderr, _ = utils.run_cmd(
            [
                "m2s-parse",
                "--input",
                str(stats_file.absolute()),
                "--output",
                str(csv_file.absolute()),
            ],
            cwd=path,
            timeout_sec=timeout_mins * 60,
        )
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)

    def load_dataframe(self, inp):
        results_dir = self.input_path(inp) / "results"
        assert results_dir.is_dir(), "{} is not a dir".format(results_dir)
        return build_multi2sim_df(
            results_dir / "stats.csv",
            sim_dur_csv=results_dir / "sim_wall_time.csv",
        )


def build_multi2sim_df(csv_file, sim_dur_csv=None):
    import pandas as pd

    df = pd.read_csv(csv_file)
    per_sm_metrics = df[df["Section"].str.match(r"SM \d+")]
    per_sm_total = per_sm_metrics.groupby("Stat")
    per_sm_total = per_sm_total.sum(numeric_only=True).reset_index()
    per_sm_total["Section"] = "Total"
    # print(len(per_sm_total))
    # print(len(per_sm_metrics["Stat"].unique()))

    assert len(per_sm_total) == len(per_sm_metrics["Stat"].unique())

    df = pd.concat([df, per_sm_total])

    df["Stat"] = df["Section"] + "." + df["Stat"]
    del df["Section"]

    df = df.set_index("Stat")
    df = df.T

    # Total instruction count
    # SPU Instructions
    # SFU Instructions
    # LDS Instructions
    # IMU Instructions
    # DPU Instructions
    # BRU Instructions
    units = ["SPU", "SFU", "LDS", "IMU", "DPU", "BRU"]
    df["Total.Instructions"] = df[
        ["Total.{} Instructions".format(unit) for unit in units]
    ].sum(axis=1)

    if sim_dur_csv is not None:
        df["sim_wall_time"] = pd.read_csv(sim_dur_csv)["exec_time_sec"]

    return df
