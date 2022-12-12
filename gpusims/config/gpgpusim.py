import re
from collections import namedtuple

GPGPUSimConfig = namedtuple(
    "GPGPUSimConfig",
    [
        "num_tpc_sm_clusters",
        "num_tpc_sm_per_cluster",
        "num_sp_units",
        "num_registers_per_sm",
        "num_register_banks",
        "num_register_file_ports",
        "dram_latency",
        "max_threads_per_sm",
        "warp_size",
        "max_warps_per_sm",
        "num_warp_schedulers_per_sm",
        "core_clock_mhz",
        "interconnect_clock_mhz",
        "l2_clock_mhz",
        "dram_clock_mhz",
        "dram_timings",
    ],
)


def parse_gpgpusim_config(config):
    def extract(pat):
        return re.search(re.compile(pat, re.MULTILINE), config)

    def get_group(match, idx, required=True, default=None):
        try:
            return match.group(idx)
        except (AttributeError, IndexError):
            pass

        if not required or default is not None:
            return default
        if match is None:
            raise IndexError("no match")
        raise IndexError("{} has no group {}".format(match, idx))

    gpgpu_n_clusters = extract(r"^\s*-gpgpu_n_clusters\s*(\d+)")
    gpgpu_n_cores_per_cluster = extract(r"^\s*-gpgpu_n_cores_per_cluster\s*(\d+)")
    gpgpu_num_sp_units = extract(r"^\s*-gpgpu_num_sp_units\s*(\d+)")
    gpgpu_num_reg_banks = extract(r"^\s*-gpgpu_num_reg_banks\s*(\d+)")
    gpgpu_shader_registers = extract(r"^\s*-gpgpu_shader_registers\s*(\d+)")

    gpgpu_reg_file_port_throughput = extract(
        r"^\s*-gpgpu_reg_file_port_throughput\s*(\d+)"
    )
    dram_latency = extract(r"^\s*-dram_latency\s*(\d+)")
    gpgpu_num_sched_per_core = extract(r"^\s*-gpgpu_num_sched_per_core\s*(\d+)")
    gpgpu_shader_cta = extract(r"^\s*-gpgpu_shader_cta\s*(\d+)")
    gpgpu_shader_core_pipeline = extract(
        r"^\s*-gpgpu_shader_core_pipeline\s*(\d+)\s*:\s*(\d+)"
    )

    gpgpu_clock_domains = extract(
        r"^\s*-gpgpu_clock_domains" + ":".join([r"\s*([\d+.]+)\s*"] * 4)
    )
    gpgpu_clock_domains = extract(r"^\s*-gpgpu_clock_domains\s*([\d+.: ]+)")
    gpgpu_clock_domains = gpgpu_clock_domains.group(1).split(":")

    gpgpu_dram_timing_opt = extract(
        "^\\s*-gpgpu_dram_timing_opt\\s*['\"]?((\\w+\\s*=\\s*\\d+\\s*:?\\s*)+)['\"]?",
    )
    if gpgpu_dram_timing_opt is not None:
        gpgpu_dram_timing_opt = (
            get_group(gpgpu_dram_timing_opt, 1, required=False) or ""
        )
        gpgpu_dram_timing_opt = gpgpu_dram_timing_opt.split(":")
        gpgpu_dram_timing_opt = [
            opt.strip().split("=") for opt in gpgpu_dram_timing_opt
        ]
        gpgpu_dram_timing_opt = {opt[0]: int(opt[1]) for opt in gpgpu_dram_timing_opt}

    # unsure: gpgpu_dram_buswidth
    # gpgpu_dram_timing_opt<nbk>
    # gpgpu_num_reg_banks
    # gpgpu_num_sp_units
    # gpgpu_clock_domains<DRAM Clock>
    # gpgpu_clock_domains[0]
    # gpgpu_shader_cta
    # gpgpu_num_sched_per_core
    # gpgpu_shader_core_pipeline [0]
    # dram_latency
    # gpgpu_n_cores_per_cluster
    # gpgpu_n_clusters
    # gpgpu_reg_file_port_throughput

    return GPGPUSimConfig(
        num_tpc_sm_clusters=int(gpgpu_n_clusters.group(1)),
        num_tpc_sm_per_cluster=int(gpgpu_n_cores_per_cluster.group(1)),
        num_warp_schedulers_per_sm=int(gpgpu_num_sched_per_core.group(1)),
        num_sp_units=int(gpgpu_num_sp_units.group(1)),
        num_register_banks=int(gpgpu_num_reg_banks.group(1)),
        num_registers_per_sm=int(gpgpu_shader_registers.group(1)),
        num_register_file_ports=int(
            get_group(gpgpu_reg_file_port_throughput, 1, default=1)
        ),
        dram_latency=int(dram_latency.group(1)),
        max_threads_per_sm=int(gpgpu_shader_core_pipeline.group(1)),
        warp_size=int(gpgpu_shader_core_pipeline.group(2)),
        max_warps_per_sm=int(gpgpu_shader_cta.group(1)),
        core_clock_mhz=float(gpgpu_clock_domains[0]),
        interconnect_clock_mhz=float(gpgpu_clock_domains[1]),
        l2_clock_mhz=float(gpgpu_clock_domains[2]),
        dram_clock_mhz=float(gpgpu_clock_domains[3]),
        dram_timings=gpgpu_dram_timing_opt,
    )
