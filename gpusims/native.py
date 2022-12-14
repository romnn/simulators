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

    def load_dataframe(self, inp):
        results_dir = self.input_path(inp) / "results"
        assert results_dir.is_dir(), "{} is not a dir".format(results_dir)
        return build_hw_df(
            cycle_csv_files=list(results_dir.rglob(r"result.cycles.csv.*")),
            kernel_csv_files=list(results_dir.rglob(r"result.csv.*")),
        )


hw_index_cols = ["Stream", "Context", "Device", "Kernel", "Correlation_ID"]


def normalize_device_name(name):
    # Strip off device numbers, e.g. (0), (1)
    # that some profiler versions add to the end of device name
    return re.sub(r" \(\d+\)$", "", name)


def build_hw_kernel_df(csv_files):
    import pandas as pd

    hw_kernel_df = pd.concat(
        [pd.read_csv(csv) for csv in csv_files], ignore_index=False
    )
    # remove the units
    hw_kernel_df = hw_kernel_df[~hw_kernel_df["Correlation_ID"].isnull()]
    # remove memcopies
    hw_kernel_df = hw_kernel_df[
        ~hw_kernel_df["Name"].str.contains(r"\[CUDA memcpy .*\]")
    ]
    # name refers to kernels now
    hw_kernel_df = hw_kernel_df.rename(columns={"Name": "Kernel"})
    # remove columns that are only relevant for memcopies
    # df = df.loc[:,df.notna().any(axis=0)]
    hw_kernel_df = hw_kernel_df.drop(
        columns=["Size", "Throughput", "SrcMemType", "DstMemType"]
    )
    # set the correct dtypes
    hw_kernel_df = hw_kernel_df.astype(
        {
            "Start": "float64",
            "Duration": "float64",
            "Static SMem": "float64",
            "Dynamic SMem": "float64",
            "Device": "string",
            "Kernel": "string",
        }
    )

    hw_kernel_df["Device"] = hw_kernel_df["Device"].apply(normalize_device_name)

    hw_kernel_df = hw_kernel_df.set_index(hw_index_cols)

    # compute min, max, mean, stddev
    grouped = hw_kernel_df.groupby(level=hw_index_cols)
    hw_kernel_df = grouped.mean()
    hw_kernel_df_max = grouped.max()
    hw_kernel_df_max = hw_kernel_df_max.rename(
        columns={c: c + "_max" for c in hw_kernel_df_max.columns}
    )
    hw_kernel_df_min = grouped.min()
    hw_kernel_df_min = hw_kernel_df_min.rename(
        columns={c: c + "_min" for c in hw_kernel_df_min.columns}
    )
    hw_kernel_df_std = grouped.std(ddof=0)
    hw_kernel_df_std = hw_kernel_df_std.rename(
        columns={c: c + "_std" for c in hw_kernel_df_std.columns}
    )
    return hw_kernel_df


def build_hw_cycles_df(csv_files):
    import pandas as pd

    hw_cycle_df = pd.concat([pd.read_csv(csv) for csv in csv_files], ignore_index=False)
    # remove the units
    hw_cycle_df = hw_cycle_df[~hw_cycle_df["Correlation_ID"].isnull()]
    # remove textual utilization metrics (high, low, ...)
    hw_cycle_df = hw_cycle_df[
        hw_cycle_df.columns[~hw_cycle_df.columns.str.contains(r".*_utilization")]
    ]
    hw_cycle_df["Device"] = hw_cycle_df["Device"].apply(normalize_device_name)
    hw_cycle_df = hw_cycle_df.set_index(hw_index_cols)
    # convert to numeric values
    hw_cycle_df = hw_cycle_df.convert_dtypes()
    hw_cycle_df = hw_cycle_df.apply(pd.to_numeric)

    # compute min, max, mean, stddev
    grouped = hw_cycle_df.groupby(level=hw_index_cols)
    hw_cycle_df = grouped.mean()
    hw_cycle_df_max = grouped.max()
    hw_cycle_df_max = hw_cycle_df_max.rename(
        columns={c: c + "_max" for c in hw_cycle_df_max.columns}
    )
    hw_cycle_df_min = grouped.min()
    hw_cycle_df_min = hw_cycle_df_min.rename(
        columns={c: c + "_min" for c in hw_cycle_df_min.columns}
    )
    hw_cycle_df_std = grouped.std(ddof=0)
    hw_cycle_df_std = hw_cycle_df_std.rename(
        columns={c: c + "_std" for c in hw_cycle_df_std.columns}
    )

    hw_cycle_df = pd.concat(
        [hw_cycle_df, hw_cycle_df_max, hw_cycle_df_min, hw_cycle_df_std], axis=1
    )
    return hw_cycle_df


def build_hw_df(kernel_csv_files, cycle_csv_files):
    # pprint(kernel_csv_files)
    # pprint(cycle_csv_files)
    kernel_df = build_hw_kernel_df(kernel_csv_files)
    # print("kernels shape", kernel_df.shape)
    cycle_df = build_hw_cycles_df(cycle_csv_files)
    # print("cycles shape", cycle_df.shape)

    # same number of repetitions
    assert kernel_df.shape[0] == cycle_df.shape[0]

    inner_hw_df = kernel_df.join(cycle_df, how="inner")

    # sort columns
    inner_hw_df = inner_hw_df.sort_index(axis=1)
    # print("inner join shape", inner_hw_df.shape)

    # assert no nan values
    assert inner_hw_df.isna().any().sum() == 0
    return inner_hw_df
