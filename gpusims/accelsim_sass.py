import os
import csv
from gpusims.bench import BenchmarkConfig
from pathlib import Path
import gpusims.utils as utils
from pprint import pprint  # noqa: F401


class AccelSimSASSBenchmarkConfig(BenchmarkConfig):
    @staticmethod
    def run_input(path, inp, force=False, timeout_mins=5, **kwargs):
        print("accelsim SASS run:", inp)

        sim_root = Path(os.environ["SIM_ROOT"])
        tracer_root = Path(os.environ["NVBIT_TRACER_ROOT"])

        nvbit_tracer_tool = tracer_root / "tracer_tool"
        assert nvbit_tracer_tool.is_dir()

        setup_env = sim_root / "setup_environment"
        assert setup_env.is_file()
        utils.chmod_x(setup_env)

        executable = path / inp.executable
        assert executable.is_file()
        utils.chmod_x(executable)

        results_dir = path / "results"
        trace_dir = results_dir / "trace"
        os.makedirs(str(trace_dir.absolute()), exist_ok=True)

        options = {
            "kernel_number": 0,
            "terminate_upon_limit": 0,
        }

        env = {
            # USER_DEFINED_FOLDERS must be set for TRACES_FOLDER variable to be read
            "USER_DEFINED_FOLDERS": "1",
            "TRACES_FOLDER": str(trace_dir.absolute()),
            "CUDA_INJECTION64_PATH": str(
                (nvbit_tracer_tool / "tracer_tool.so").absolute()
            ),
            "LD_PRELOAD": str((nvbit_tracer_tool / "tracer_tool.so").absolute()),
        }
        env["DYNAMIC_KERNEL_LIMIT_END"] = "0"
        try:
            env["DYNAMIC_KERNEL_LIMIT_END"] = str(int(options["kernel_number"]))
        except (KeyError, ValueError):
            pass

        try:
            if int(options["terminate_upon_limit"]) > 0:
                env["TERMINATE_UPON_LIMIT"] = "1"
        except (KeyError, ValueError):
            pass

        post_traces_processing = (
            nvbit_tracer_tool / "traces-processing/post-traces-processing"
        )

        # create the tracing script
        # 1. generate the .trace and kernelslist trace files
        # 2. post-processing for the traces and generate .traceg and kernelslist.g files
        # 3. delete the intermediate .trace and kernelslist files
        tmp_trace_sh = "set -e\n"
        for k, v in env.items():
            tmp_trace_sh += 'export {}="{}"\n'.format(k, v)
        tmp_trace_sh += "{} {}\n".format(str(executable.absolute()), inp.args)
        tmp_trace_sh += "{} {}\n".format(
            str(post_traces_processing.absolute()),
            str((trace_dir / "kernelslist").absolute()),
        )
        if True:
            tmp_trace_sh += "rm -f {}\n".format(str((trace_dir / "*.trace").absolute()))
            tmp_trace_sh += "rm -f {}\n".format(
                str((trace_dir / "kernelslist").absolute())
            )

        print("\nrunning:\n")
        print(tmp_trace_sh)
        print("")

        tmp_trace_file = path / "trace.tmp.sh"
        with open(str(tmp_trace_file.absolute()), "w") as f:
            f.write(tmp_trace_sh)

        _, stdout, stderr, duration = utils.run_cmd(
            "bash " + str(tmp_trace_file.absolute()),
            cwd=path,
            timeout_sec=timeout_mins * 60,
            shell=True,
        )
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)

        with open(str((results_dir / "trace_wall_time.csv").absolute()), "w") as f:
            output_writer = csv.writer(f)
            output_writer.writerow(["exec_time_sec"])
            output_writer.writerow([duration])

        tmp_trace_file.unlink()

        accelsim = sim_root.parent / "bin/release/accel-sim.out"
        log_file = results_dir / "log.txt"

        tmp_run_sh = "set -e\n"
        tmp_run_sh += "source {}\n".format(str(setup_env.absolute()))
        cmd = [
            str(accelsim.absolute()),
            "-trace",
            str((trace_dir / "kernelslist.g").absolute()),
            "-config",
            str((path / "gpgpusim.config").absolute()),
        ]
        tmp_run_sh += " ".join(cmd)
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

        tmp_run_file.unlink()

        # parse the log file
        stat_file = results_dir / "stats.csv"
        _, stdout, stderr, _ = utils.run_cmd(
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

    def load_dataframe(self, inp):
        results_dir = self.input_path(inp) / "results"
        assert results_dir.is_dir(), "{} is not a dir".format(results_dir)
        return build_accelsim_sass_df(
            results_dir / "stats.csv",
            trace_dur_csv=results_dir / "trace_wall_time.csv",
            sim_dur_csv=results_dir / "sim_wall_time.csv",
        )


def build_accelsim_sass_df(csv_file, trace_dur_csv=None, sim_dur_csv=None):
    import pandas as pd

    df = pd.read_csv(csv_file)
    df = df.pivot(index=["kernel", "kernel_id"], columns=["stat"])["value"]
    df = df.reset_index()
    if trace_dur_csv is not None:
        # print(pd.read_csv(trace_dur_csv)["exec_time_sec"])
        df["trace_wall_time"] = pd.read_csv(trace_dur_csv)["exec_time_sec"]
        # df = pd.concat(
        #     [
        #         df,
        #         pd.read_csv(trace_dur_csv).rename(
        #             columns={"exec_time_sec": "trace_wall_time"}
        #         ),
        #     ],
        #     axis=1,
        #     # ignore_index=True,
        # )
    if sim_dur_csv is not None:
        df["sim_wall_time"] = pd.read_csv(sim_dur_csv)["exec_time_sec"]

        # df = pd.concat(
        #     [
        #         df,
        #         pd.read_csv(sim_dur_csv).rename(
        #             columns={"exec_time_sec": "sim_wall_time"}
        #         ),
        #     ],
        #     axis=1,
        #     # ignore_index=True,
        # )

    return df
