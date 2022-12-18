import re
import os
import csv
from pprint import pprint  # noqa: F401
from gpusims.bench import BenchmarkConfig
from gpusims.cuda import get_devices
import gpusims.utils as utils


def convert_hw_csv(csv_file, output_csv_file):
    with open(str(csv_file.absolute()), "rU") as f:
        reader = csv.reader(f)

        # find the start of the csv dump
        csv_start_nvprof = re.compile(r"^==\d*==\s*Profiling result:\s*$")
        csv_start_nsight = re.compile(r"^==PROF== Disconnected[\S\s]*$")
        for row in reader:
            line = ", ".join(row)
            # print(line)
            if csv_start_nvprof.match(line) is not None:
                break
            if csv_start_nsight.match(line) is not None:
                break

        os.makedirs(output_csv_file.parent, exist_ok=True)
        with open(str(output_csv_file.absolute()), "w") as out:
            output_writer = csv.writer(out)

            # write the valid csv rows to new file
            for row in reader:
                output_writer.writerow(row)
        print("parsed stats:", output_csv_file.absolute())


def profile_nvsight(path, inp, results_dir, r, timeout_mins=5):

    executable = path / inp.executable
    assert executable.is_file()
    utils.chmod_x(executable)

    metrics = [
        "gpc__cycles_elapsed.avg",
        "l1tex__t_sectors_pipe_lsu_mem_global_op_ld_lookup_hit.sum",
        "l1tex__t_sectors_pipe_lsu_mem_global_op_ld.sum",
        "l1tex__t_sectors_pipe_lsu_mem_global_op_st_lookup_hit.sum",
        "l1tex__t_sectors_pipe_lsu_mem_global_op_st.sum",
        "l1tex__t_sectors_pipe_lsu_mem_global_op_ld_lookup_hit.sum",
        "l1tex__t_sectors_pipe_lsu_mem_global_op_ld_lookup_miss.sum",
        "l1tex__t_sectors_pipe_lsu_mem_global_op_st_lookup_miss.sum",
        "lts__t_sector_op_write_hit_rate.pct",
        "lts__t_sectors_srcunit_tex_op_read.sum",
        "lts__t_sectors_srcunit_tex_op_write.sum",
        "lts__t_sectors_srcunit_tex_op_read_lookup_hit.sum",
        "lts__t_sectors_srcunit_tex_op_write_lookup_hit.sum",
        "lts__t_sectors_srcunit_tex_op_read.sum.per_second",
        "lts__t_sectors_srcunit_tex_op_read_lookup_miss.sum",
        "lts__t_sectors_srcunit_tex_op_write_lookup_miss.sum",
        "dram__sectors_read.sum",
        "dram__sectors_write.sum",
        "dram__bytes_read.sum",
        "smsp__inst_executed.sum",
        "smsp__thread_inst_executed_per_inst_executed.ratio",
        "smsp__cycles_active.avg.pct_of_peak_sustained_elapsed",
        "idc__requests.sum,idc__requests_lookup_hit.sum",
        "sm__warps_active.avg.pct_of_peak_sustained_active",
        "sm__pipe_alu_cycles_active.sum",
        "sm__pipe_fma_cycles_active.sum",
        "sm__pipe_fp64_cycles_active.sum",
        "sm__pipe_shared_cycles_active.sum",
        "sm__pipe_tensor_cycles_active.sum",
        "sm__pipe_tensor_op_hmma_cycles_active.sum",
        "sm__cycles_elapsed.sum",
        "sm__cycles_active.sum",
        "sm__cycles_active.avg",
        "sm__cycles_elapsed.avg",
        "sm__sass_thread_inst_executed_op_integer_pred_on.sum",
        "sm__sass_thread_inst_executed_ops_dadd_dmul_dfma_pred_on.sum",
        "sm__sass_thread_inst_executed_ops_fadd_fmul_ffma_pred_on.sum",
        "sm__sass_thread_inst_executed_ops_hadd_hmul_hfma_pred_on.sum",
        "sm__inst_executed.sum",
        "sm__inst_executed_pipe_alu.sum",
        "sm__inst_executed_pipe_fma.sum",
        "sm__inst_executed_pipe_fp16.sum",
        "sm__inst_executed_pipe_fp64.sum",
        "sm__inst_executed_pipe_tensor.sum",
        "sm__inst_executed_pipe_tex.sum",
        "sm__inst_executed_pipe_xu.sum",
        "sm__inst_executed_pipe_lsu.sum",
        "sm__sass_thread_inst_executed_op_fp16_pred_on.sum",
        "sm__sass_thread_inst_executed_op_fp32_pred_on.sum",
        "sm__sass_thread_inst_executed_op_fp64_pred_on.sum",
        "sm__sass_thread_inst_executed_op_dmul_pred_on.sum",
        "sm__sass_thread_inst_executed_op_dfma_pred_on.sum",
        "sm__sass_thread_inst_executed.sum",
        "sm__sass_inst_executed_op_shared_st.sum",
        "sm__sass_inst_executed_op_shared_ld.sum",
        "sm__sass_inst_executed_op_memory_128b.sum",
        "sm__sass_inst_executed_op_memory_64b.sum",
        "sm__sass_inst_executed_op_memory_32b.sum",
        "sm__sass_inst_executed_op_memory_16b.sum",
        "sm__sass_inst_executed_op_memory_8b.sum",
    ]
    cmd = [
        "nv-nsight-cu-cli",
        "--metrics",
        ",".join(metrics),
        "--csv",
        "--page",
        "raw",
        "--target-processes",
        "all",
        str(executable.absolute()),
        inp.args,
    ]
    cmd = " ".join(cmd)

    log_file = results_dir / "{}.result.nsight.txt".format(r)
    _, stdout, stderr, _ = utils.run_cmd(
        cmd,
        cwd=path,
        timeout_sec=timeout_mins * 60,
        save_to=results_dir / "nsight",
    )
    print("stdout:")
    print(stdout)
    print("stderr:")
    print(stderr)

    with open(str(log_file.absolute()), "w") as f:
        f.write(stdout)


def profile_nvprof(path, inp, results_dir, r, timeout_mins=5):
    log_file = results_dir / "{}.result.nvprof.txt".format(r)

    executable = path / inp.executable
    assert executable.is_file()
    utils.chmod_x(executable)

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
        _, stdout, stderr, _ = utils.run_cmd(
            cmd,
            cwd=path,
            timeout_sec=timeout_mins * 60,
            save_to=results_dir / "nvprof-kernels",
        )
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)

        with open(str(log_file.absolute()), "r") as f:
            print("log file:")
            print(f.read())

    except utils.ExecError as e:
        with open(str(log_file.absolute()), "r") as f:
            print("log file:")
            print(f.read())
        raise e

    cycles_log_file = results_dir / "{}.result.nvprof.cycles.txt".format(r)
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
        _, stdout, stderr, _ = utils.run_cmd(
            cycles_cmd,
            cwd=path,
            timeout_sec=timeout_mins * 60,
            save_to=results_dir / "nvprof-cycles",
        )
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)

        with open(str(cycles_log_file.absolute()), "r") as f:
            print("log file:")
            print(f.read())

    except utils.ExecError as e:
        with open(str(cycles_log_file.absolute()), "r") as f:
            print("log file:")
            print(f.read())
        raise e


class NativeBenchmarkConfig(BenchmarkConfig):
    @staticmethod
    def _run(path, inp, repetitions=1, timeout_mins=5, parse_only=False, **kwargs):
        print("native run")
        pprint(
            dict(
                path=path,
                inp=inp,
                repetitions=repetitions,
                timeout_mins=timeout_mins,
                kwargs=kwargs,
            )
        )

        if not parse_only:
            devices = get_devices()
            if len(devices) < 1:
                raise AssertionError("no GPU device found")
            device = devices[0]
            if len(devices) > 1:
                print(
                    "warn: multiple GPU devices found, using compute capability of",
                    device.name,
                )

            print(
                "{} has compute capability {}.{}".format(
                    device.name, device.major, device.minor
                )
            )

            use_nvsight = int(device.major) >= 8

            results_dir = path / "results"
            os.makedirs(str(results_dir.absolute()), exist_ok=True)

            for r in range(repetitions):
                args = dict(
                    path=path,
                    inp=inp,
                    results_dir=results_dir,
                    r=r,
                    timeout_mins=timeout_mins,
                )

                if use_nvsight:
                    profile_nvsight(**args)
                else:
                    profile_nvprof(**args)

        # parse the results
        nsight_results = sorted(list(results_dir.rglob("*result.nsight.txt")))
        nvprof_cycles_results = sorted(
            list(results_dir.rglob("*result.nvprof.cycles.txt"))
        )
        nvprof_results = sorted(list(results_dir.rglob("*result.nvprof.txt")))

        for result in nvprof_results + nvprof_cycles_results + nsight_results:
            convert_hw_csv(result, result.with_suffix(".csv"))

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

    # ref: https://docs.nvidia.com/cuda/profiler-users-guide/index.html#metrics-reference # noqa: E501
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
