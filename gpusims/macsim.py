"""
# nvcc sourcefile.cu –locelot –ocelotTrace –lz

# Create a trace directory in the /storage/tracesexport
TRACE_PATH="/storage/traces/"
# kernel_infohas the kernel information
export KERNEL_INFO_PATH="kernel_info"
# Calculate occupancy based on compute capability 2.0
export COMPUTE_VERSION="2.0"

Availableknobscanbefoundin.param.deffilesindef/directoryundermainsourcedirectory
params.in
./macsim --l2_assoc=16

.stat.deffilesindef/
"""

import os
import re
from gpusims.bench import BenchmarkConfig
import gpusims.utils as utils


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
