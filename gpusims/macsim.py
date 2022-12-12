import re
import csv
from gpusims.bench import BenchmarkConfig
import gpusims.utils as utils


def parse_stats(stats_dir):
    """At the end of a simulation several files with the extension
    stat.out are generated, these files include the stat values at
    the end of the simulation.  As mentioned, for each stat two values
    are generated, one is the raw stat value and other is a value
    calculated based on the type of the stat. For simulations with
    multiple applications, multiple sets of stat files are generated.
    Each simulated application is assigned an integer id
    (these ids are assigned according to the order in which the
    applications appear in the trace_file_list), when an application
    terminates (for the first time, note that applications may be
    repeated), stat files suffixed with the the id of the application,
    i.e. *.stat.out.<appl_id>, are generated. These stat files contain
    the value of the stats until that point in the simulation.
    At the end of the simulation, *.stat.out files are generated as usual.
    """
    raw_stat_files = list(stats_dir.rglob("*stat.out*"))
    for raw_stat_file in raw_stat_files:
        stat_file = raw_stat_file.parent / (raw_stat_file.name + ".csv")
        with open(str(raw_stat_file.absolute()), "r") as f:
            with open(str(stat_file.absolute()), "w") as out:
                print("parsing {}".format(str(stat_file.absolute())))
                output_writer = csv.writer(out)
                output_writer.writerow(["stat", "raw_value", "agg_value"])
                for line in f.readlines():
                    match = re.match(
                        re.compile(r"^\s*(\w+)\s+([%.,\w]+)\s+([%.,\w]+)$"), line
                    )
                    if match is not None:
                        stat_name = match.group(1)
                        raw_value = match.group(2)
                        agg_value = match.group(3)
                        row = [stat_name, raw_value, agg_value]
                        # print(row)
                        output_writer.writerow(row)


class MacSimBenchmarkConfig(BenchmarkConfig):
    @staticmethod
    def run_input(path, inp, force=False, **kwargs):
        print("macsim run:", inp)

        executable = path / inp.executable
        assert executable.is_file()
        utils.chmod_x(executable)

        results_dir = path / "results"
        trace_dir = results_dir / "traces"

        utils.ensure_empty(results_dir)
        utils.ensure_empty(trace_dir)

        # read gpgpusim.config
        gpgpusim_config_file = path / "gpgpusim.config"
        with open(str(gpgpusim_config_file.absolute()), "r") as f:
            gpgpusim_config = f.read()
        # print(gpgpusim_config)

        # extract compute version
        compute_version = re.search(
            re.compile(r"^\s*-gpgpu_occupancy_sm_number\s*(\d+)\s*$", re.MULTILINE),
            gpgpusim_config,
        )
        if compute_version is None:
            raise ValueError(
                "gpgpusim config does not specify gpgpu_occupancy_sm_number"
            )
        compute_version = compute_version.group(1)
        print(compute_version)

        kernel_info_file = path / "occupancy.txt"
        env = {
            "TRACE_PATH": str(trace_dir.absolute()),
            # "TRACE_NAME": "please",
            # "USE_KERNEL_NAME": "1",
            "KERNEL_INFO_PATH": str(kernel_info_file.absolute()),
            # "KERNEL_INFO_PATH": "occupancy.txt",
            # "COMPUTE_VERSION": compute_version,
            "COMPUTE_VERSION": "2.0",
        }
        tmp_run_sh = "set -e\n"
        for k, v in env.items():
            tmp_run_sh += 'export {}="{}"\n'.format(k, v)
        tmp_run_sh += "{} {}\n".format(str(executable.absolute()), inp.args)

        print("\nrunning:\n")
        print(tmp_run_sh)
        print("")

        tmp_run_file = path / "run.tmp.sh"
        with open(str(tmp_run_file.absolute()), "w") as f:
            f.write(tmp_run_sh)

        _, stdout, stderr = utils.run_cmd(
            "bash " + str(tmp_run_file.absolute()),
            cwd=path,
            timeout_sec=5 * 60,
            shell=True,
        )
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)

        # https://github.com/gthparch/macsim/blob/master/doc/macsim.pdf
        # macsim needs:
        #  - a params.in config file (already present based on the bench config)
        #  - a file "trace_file_list" with the number of traces and their locations
        trace_file_list = path / "trace_file_list"
        trace_kernel_config = trace_dir / "kernel_config.txt"
        if not trace_kernel_config.is_file():
            raise AssertionError(
                "{} not found. ".format(trace_kernel_config)
                + "ocelot did not trace execution (did you build with g++4.4)"
            )
        with open(str(trace_file_list.absolute()), "w") as f:
            f.write("1\n")
            f.write("{}\n".format(str(trace_kernel_config.absolute())))

        stats_dir = results_dir / "stats"
        utils.ensure_empty(stats_dir)
        _, stdout, stderr = utils.run_cmd(
            " ".join(
                [
                    "macsim",
                    # ref: STATISTICS_OUT_DIRECTORY knob
                    "--out={}".format(str(stats_dir.absolute())),
                ]
            ),
            cwd=path,
            timeout_sec=5 * 60,
        )

        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)

        parse_stats(stats_dir)
