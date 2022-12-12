from pprint import pprint
import re


def configure_macsim(ref, macsim_template):
    config = {}
    for line in macsim_template.splitlines():
        match = re.match(re.compile(r"^\s*(?<!#)\s*(\w+)\s+(\w+)"), line)
        if match is not None:
            key = match.group(1)
            value = match.group(2)
            config[key] = value
        # print(line)

    override = {}

    # Core config
    sm_cores = ref.num_tpc_sm_clusters * ref.num_tpc_sm_per_cluster
    override["num_sim_cores"] = sm_cores
    override["num_sim_small_cores"] = sm_cores
    override["num_warp_scheduler"] = ref.num_warp_schedulers_per_sm  # 2
    override["max_threads_per_core"] = ref.max_threads_per_sm
    override["clock_gpu"] = ref.core_clock_mhz / 1000.0
    override["clock_mc"] = ref.dram_clock_mhz / 1000.0
    override["clock_llc"] = ref.l2_clock_mhz / 1000.0
    override["clock_noc"] = ref.interconnect_clock_mhz / 1000.0

    # Memory config
    try:
        override["const_cache_size"] = (
            ref.l1_const_cache_num_sets
            * ref.l1_const_cache_blocksize
            * ref.l1_const_cache_assoc
        )
    except TypeError:
        pass

    try:
        override["texture_cache_size"] = (
            ref.l1_tex_cache_num_sets
            * ref.l1_tex_cache_blocksize
            * ref.l1_tex_cache_assoc
        )
    except TypeError:
        pass
    # # note: this is per SM 768KB*1000 / 512shading units = 1500
    override["shared_mem_size"] = (
        ref.shmem_size_pref_shared or ref.shmem_size_pref_l1 or ref.shmem_size
    )
    override["shared_mem_banks"] = ref.num_shmem_banks  # 32
    override["shared_mem_cycles"] = ref.shmem_latency  # 2
    override["shared_mem_ports"] = ref.mem_unit_ports  # 1

    # L1 cache config
    override["l1_small_line_size"] = ref.l1_data_cache_blocksize  # 128
    override["l1_small_num_set"] = ref.l1_data_cache_num_sets  # 64
    override["l1_small_assoc"] = ref.l1_data_cache_assoc  # 6

    # l2 cache
    override["llc_num_set"] = ref.l2_data_cache_num_sets  # 128
    override["llc_line_size"] = ref.l2_data_cache_blocksize  # 128
    override["llc_assoc"] = ref.l2_data_cache_assoc  # 8
    # override["llc_num_bank"] = #  4 = gpgpu_cache:dl2<N> but how to parse that??

    # DRAM config
    override["dram_num_mc"] = ref.dram_num_memory_controllers  # 6
    override["dram_activate"] = ref.dram_timings.get("RAS")  # 25
    override["dram_precharge"] = ref.dram_timings.get("RP")  # 10
    override["dram_num_banks"] = ref.dram_timings.get("nbk")  # 16
    override["dram_num_channel"] = ref.dram_timings.get("nbkgrp")  # 1 not sure

    override = {k: v for k, v in override.items() if v is not None}
    pprint(override)
    config.update(override)

    new_config = "THIS FILE IS AUTO GENERATED. DO NOT EDIT.\n\n"
    new_config += "\n".join(["{} {}".format(k, v) for k, v in config.items()])
    return new_config.encode("utf-8")
