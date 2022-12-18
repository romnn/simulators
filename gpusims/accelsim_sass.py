import os
import csv
from gpusims.bench import BenchmarkConfig
from pathlib import Path
import gpusims.utils as utils
from pprint import pprint  # noqa: F401

ROOT_DIR = Path(__file__).parent.parent


def trace_commands(path, inp, traces_dir):
    # locate nvbit tracing tool
    if os.environ.get("NVBIT_TRACER_ROOT") is not None:
        nvbit_tracer_root = Path(os.environ["NVBIT_TRACER_ROOT"])
    else:
        nvbit_tracer_root = (
            ROOT_DIR / "benchmarks/accel-sim-framework/util/tracer_nvbit/"
        )
        print(
            "NVBIT_TRACER_ROOT environment variable is not set, trying",
            str(nvbit_tracer_root.absolute()),
        )
        if not nvbit_tracer_root.is_dir():
            raise AssertionError(
                "NVBIT_TRACER_ROOT not set and default installation at {} does not exist.".format(
                    str(nvbit_tracer_root.absolute())
                )
            )

    nvbit_tracer_tool = nvbit_tracer_root / "tracer_tool"
    assert nvbit_tracer_tool.is_dir()

    executable = path / inp.executable
    assert executable.is_file()
    utils.chmod_x(executable)

    options = {
        "kernel_number": 0,
        "terminate_upon_limit": 0,
    }

    env = {
        # USER_DEFINED_FOLDERS must be set for TRACES_FOLDER variable to be read
        "USER_DEFINED_FOLDERS": "1",
        "TRACES_FOLDER": str(traces_dir.absolute()),
        "CUDA_INJECTION64_PATH": str((nvbit_tracer_tool / "tracer_tool.so").absolute()),
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

    # create the tracing commands
    # 1. generate the .trace and kernelslist trace files
    # 2. post-processing for the traces and generate .traceg and kernelslist.g files
    # 3. delete the intermediate .trace and kernelslist files
    trace_cmds = []
    for k, v in env.items():
        trace_cmds += ['export {}="{}"\n'.format(k, v)]
    trace_cmds += ["{} {}\n".format(str(executable.absolute()), inp.args)]
    trace_cmds += [
        "{} {}\n".format(
            str(post_traces_processing.absolute()),
            str((traces_dir / "kernelslist").absolute()),
        )
    ]
    if True:
        trace_cmds += ["rm -f {}\n".format(str((traces_dir / "*.trace").absolute()))]
        trace_cmds += [
            "rm -f {}\n".format(str((traces_dir / "kernelslist").absolute()))
        ]
    return trace_cmds


class AccelSimSASSBenchmarkConfig(BenchmarkConfig):
    @staticmethod
    def _run(path, inp, timeout_mins=5, parse_only=False, trace_only=False, **kwargs):
        print("accelsim SASS run")

        pprint(
            dict(
                path=path,
                inp=inp,
                timeout_mins=timeout_mins,
                parse_only=parse_only,
                trace_only=trace_only,
                kwargs=kwargs,
            )
        )

        results_dir = path / "results"
        traces_dir = results_dir / "traces"
        os.makedirs(str(traces_dir.absolute()), exist_ok=True)

        if not parse_only:
            trace_cmds = trace_commands(
                path=path,
                inp=inp,
                traces_dir=traces_dir,
            )
            tmp_trace_sh = "\n".join(["set -e"] + trace_cmds)

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
                save_to=results_dir / "trace.tmp.sh",
            )
            print("stdout (last 15 lines):")
            print("\n".join(stdout.splitlines()[-15:]))
            print("stderr (last 15 lines):")
            print("\n".join(stderr.splitlines()[-15:]))

            with open(str((results_dir / "trace_wall_time.csv").absolute()), "w") as f:
                output_writer = csv.writer(f)
                output_writer.writerow(["exec_time_sec"])
                output_writer.writerow([duration])

            tmp_trace_file.unlink()

        if trace_only:
            return

        if not parse_only:
            sim_root = Path(os.environ["SIM_ROOT"])
            setup_env = sim_root / "setup_environment"
            assert setup_env.is_file()
            utils.chmod_x(setup_env)

            accelsim = sim_root.parent / "bin/release/accel-sim.out"
            log_file = results_dir / "log.txt"

            tmp_run_sh = "set -e\n"
            tmp_run_sh += "source {}\n".format(str(setup_env.absolute()))
            cmd = [
                str(accelsim.absolute()),
                "-trace",
                str((traces_dir / "kernelslist.g").absolute()),
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
                save_to=results_dir / "run.tmp.sh",
            )
            print("stdout (last 15 lines):")
            print("\n".join(stdout.splitlines()[-15:]))
            print("stderr (last 15 lines):")
            print("\n".join(stderr.splitlines()[-15:]))

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
            save_to=results_dir / "gpgpusim-parse",
        )
        print("stdout (last 15 lines):")
        print("\n".join(stdout.splitlines()[-15:]))
        print("stderr (last 15 lines):")
        print("\n".join(stderr.splitlines()[-15:]))

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
        df["trace_wall_time"] = pd.read_csv(trace_dur_csv)["exec_time_sec"]
    if sim_dur_csv is not None:
        df["sim_wall_time"] = pd.read_csv(sim_dur_csv)["exec_time_sec"]

    return df
