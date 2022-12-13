import re
from collections import namedtuple
from pprint import pprint  # noqa: F401


GPGPUSimConfig = namedtuple(
    "GPGPUSimConfig",
    [
        "num_tpc_sm_clusters",
        "num_tpc_sm_per_cluster",
        "num_sp_units",
        "num_registers_per_sm",
        "num_register_banks",
        "num_shmem_banks",
        "num_register_file_ports",
        "mem_unit_ports",
        "dram_latency",
        "dram_num_memory_controllers",
        "shmem_latency",
        "max_threads_per_sm",
        "warp_size",
        "max_warps_per_sm",
        "num_warp_schedulers_per_sm",
        "core_clock_mhz",
        "interconnect_clock_mhz",
        "l2_clock_mhz",
        "dram_clock_mhz",
        "shmem_size",
        "shmem_size_pref_l1",
        "shmem_size_pref_shared",
        "dram_timings",
        "l1_const_cache_assoc",
        "l1_const_cache_blocksize",
        "l1_const_cache_num_sets",
        "l1_tex_cache_assoc",
        "l1_tex_cache_blocksize",
        "l1_tex_cache_num_sets",
        "l1_instr_cache_assoc",
        "l1_instr_cache_blocksize",
        "l1_instr_cache_num_sets",
        "l1_data_cache_assoc",
        "l1_data_cache_blocksize",
        "l1_data_cache_num_sets",
        "l2_data_cache_assoc",
        "l2_data_cache_blocksize",
        "l2_data_cache_num_sets",
    ],
)


def get_group_int(match, idx, required=True, default=None):
    value = get_group(match=match, idx=idx, required=required, default=default)
    try:
        return int(value)
    except (TypeError, ValueError) as e:
        if not required or default is not None:
            return default
        raise e


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


def parse_cache_config(match):
    # <nsets>:<bsize>:<assoc>,<rep>:<wr>:<alloc>:<wr_alloc>,<mshr>:<N>:<merge>,<mq>
    # N:128:64:2,L:R:f:N:L,A:2:64,4
    cache_config = {}
    if match is not None:
        parts = match.group(1)
        parts = parts.split(",")
        parts = [p.strip().split(":") for p in parts]
        # pprint(parts)
        if len(parts) > 0:
            cache_config["assoc"] = parts[0][-1]
            cache_config["bsize"] = parts[0][-2]
            cache_config["nsets"] = parts[0][-3]
    return cache_config


def parse_gpgpusim_config(config):
    def extract(pat):
        return re.search(re.compile(pat, re.MULTILINE), config)

    gpgpu_n_clusters = extract(r"^\s*-gpgpu_n_clusters\s*(\d+)")
    gpgpu_n_cores_per_cluster = extract(r"^\s*-gpgpu_n_cores_per_cluster\s*(\d+)")
    gpgpu_num_sp_units = extract(r"^\s*-gpgpu_num_sp_units\s*(\d+)")
    gpgpu_num_reg_banks = extract(r"^\s*-gpgpu_num_reg_banks\s*(\d+)")
    gpgpu_shmem_num_banks = extract(r"^\s*-gpgpu_shmem_num_banks\s*(\d+)")
    gpgpu_shader_registers = extract(r"^\s*-gpgpu_shader_registers\s*(\d+)")

    gpgpu_mem_unit_ports = extract(r"^\s*-gpgpu_mem_unit_ports\s*(\d+)")

    gpgpu_reg_file_port_throughput = extract(
        r"^\s*-gpgpu_reg_file_port_throughput\s*(\d+)"
    )
    dram_latency = extract(r"^\s*-dram_latency\s*(\d+)")
    gpgpu_n_mem = extract(r"^\s*-gpgpu_n_mem\s*(\d+)")
    gpgpu_smem_latency = extract(r"^\s*-gpgpu_smem_latency\s*(\d+)")
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

    gpgpu_shmem_sizeDefault = extract(r"^\s*-gpgpu_shmem_sizeDefault\s*(\d+)")
    gpgpu_shmem_size_PrefL1 = extract(r"^\s*-gpgpu_shmem_size_PrefL1\s*(\d+)")
    gpgpu_shmem_size_PrefShared = extract(r"^\s*-gpgpu_shmem_size_PrefShared\s*(\d+)")

    # L1 constant cache
    gpgpu_const_cache_l1 = extract(
        r"^\s*-gpgpu_const_cache:l1\s*['\"]?\s*([\w\d :,]+)\s*['\"]?",
    )
    gpgpu_const_cache_l1 = parse_cache_config(gpgpu_const_cache_l1)

    # L1 tex cache
    gpgpu_tex_cache_l1 = extract(
        r"^\s*-gpgpu_tex_cache:l1\s*['\"]?\s*([\w\d :,]+)\s*['\"]?",
    )
    gpgpu_tex_cache_l1 = parse_cache_config(gpgpu_tex_cache_l1)

    # L1 instruction cache
    gpgpu_cache_il1 = extract(
        r"^\s*-gpgpu_cache:il1\s*['\"]?\s*([\w\d :,]+)\s*['\"]?",
    )
    gpgpu_cache_il1 = parse_cache_config(gpgpu_cache_il1)

    # L1 data cache
    gpgpu_cache_dl1 = extract(
        r"^\s*-gpgpu_cache:dl1\s*['\"]?\s*([\w\d :,]+)\s*['\"]?",
    )
    gpgpu_cache_dl1 = parse_cache_config(gpgpu_cache_dl1)

    # L2 data cache
    gpgpu_cache_dl2 = extract(
        r"^\s*-gpgpu_cache:dl2\s*['\"]?\s*([\w\d :,]+)\s*['\"]?",
    )
    gpgpu_cache_dl2 = parse_cache_config(gpgpu_cache_dl2)

    gpgpu_dram_timing_opt = extract(
        r"^\s*-gpgpu_dram_timing_opt\s*['\"]?((\w+\s*=\s*\d+\s*:?\s*)+)['\"]?",
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

    return GPGPUSimConfig(
        num_tpc_sm_clusters=int(gpgpu_n_clusters.group(1)),
        num_tpc_sm_per_cluster=int(gpgpu_n_cores_per_cluster.group(1)),
        num_warp_schedulers_per_sm=int(gpgpu_num_sched_per_core.group(1)),
        num_sp_units=int(gpgpu_num_sp_units.group(1)),
        num_register_banks=int(gpgpu_num_reg_banks.group(1)),
        num_shmem_banks=int(gpgpu_shmem_num_banks.group(1)),
        num_registers_per_sm=int(gpgpu_shader_registers.group(1)),
        mem_unit_ports=get_group_int(gpgpu_mem_unit_ports, 1, required=False),
        num_register_file_ports=get_group_int(
            gpgpu_reg_file_port_throughput, 1, required=False
        ),
        shmem_size=get_group_int(gpgpu_shmem_sizeDefault, 1, required=False),
        shmem_size_pref_l1=get_group_int(gpgpu_shmem_size_PrefL1, 1, required=False),
        shmem_size_pref_shared=get_group_int(
            gpgpu_shmem_size_PrefShared, 1, required=False
        ),
        dram_latency=int(dram_latency.group(1)),
        dram_num_memory_controllers=get_group_int(gpgpu_n_mem, 1, required=False),
        shmem_latency=get_group_int(gpgpu_smem_latency, 1, required=False),
        max_threads_per_sm=int(gpgpu_shader_core_pipeline.group(1)),
        warp_size=int(gpgpu_shader_core_pipeline.group(2)),
        max_warps_per_sm=int(gpgpu_shader_cta.group(1)),
        core_clock_mhz=float(gpgpu_clock_domains[0]),
        interconnect_clock_mhz=float(gpgpu_clock_domains[1]),
        l2_clock_mhz=float(gpgpu_clock_domains[2]),
        dram_clock_mhz=float(gpgpu_clock_domains[3]),
        dram_timings=gpgpu_dram_timing_opt,
        # cache configuration
        l1_const_cache_assoc=gpgpu_const_cache_l1.get("assoc"),
        l1_const_cache_blocksize=gpgpu_const_cache_l1.get("bsize"),
        l1_const_cache_num_sets=gpgpu_const_cache_l1.get("nsets"),
        l1_tex_cache_assoc=gpgpu_tex_cache_l1.get("assoc"),
        l1_tex_cache_blocksize=gpgpu_tex_cache_l1.get("bsize"),
        l1_tex_cache_num_sets=gpgpu_tex_cache_l1.get("nsets"),
        l1_instr_cache_assoc=gpgpu_cache_il1.get("assoc"),
        l1_instr_cache_blocksize=gpgpu_cache_il1.get("bsize"),
        l1_instr_cache_num_sets=gpgpu_cache_il1.get("nsets"),
        l1_data_cache_assoc=gpgpu_cache_dl1.get("assoc"),
        l1_data_cache_blocksize=gpgpu_cache_dl1.get("bsize"),
        l1_data_cache_num_sets=gpgpu_cache_dl1.get("nsets"),
        l2_data_cache_assoc=gpgpu_cache_dl2.get("assoc"),
        l2_data_cache_blocksize=gpgpu_cache_dl2.get("bsize"),
        l2_data_cache_num_sets=gpgpu_cache_dl2.get("nsets"),
    )
