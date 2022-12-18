import math
import xml.etree.ElementTree as ET


def cache_size_kb(sets, block_size, assoc):
    return int(math.floor((int(sets) * int(block_size) * int(assoc)) / 1024.0))


def configure_tejas(ref, tejas_template):
    tree = ET.ElementTree(ET.fromstring(tejas_template))
    root = tree.getroot()

    root.find("./Simulation/ThreadsPerCTA").text = str(ref.max_threads_per_sm)
    root.find("./System/NoOfTPC").text = str(ref.num_tpc_sm_clusters)

    tpc = root.find("./System/TPC")
    tpc.find("./NoOfSM").text = str(ref.num_tpc_sm_per_cluster)
    tpc.find("./SM/Frequency").text = str(int(ref.core_clock_mhz))
    tpc.find("./SM/NoOfWarpSchedulers").text = str(ref.num_warp_schedulers_per_sm)
    tpc.find("./SM/NoOfSP").text = str(ref.num_sp_units)
    tpc.find("./SM/WarpSize").text = str(ref.warp_size)
    # tpc.find("./SM/SP/NoOfThreadsSupported").text = str(ref.warp_size)  # correct?
    tpc.find("./SM/RegisterFile/Bank/BankSize").text = str(ref.num_register_banks)

    dram = root.find("./System/MainMemory")
    dram.find("./MainMemoryLatency").text = str(ref.dram_latency)
    dram.find("./MainMemoryFrequency").text = str(int(ref.dram_clock_mhz))

    dram_controller = root.find("./System/MainMemoryController")
    # dram_controller.find("./rankLatency").text = str(ref.dram_latency)  # correct ?
    dram_controller.find("./rankOperatingFrequency").text = str(int(ref.dram_clock_mhz))
    # seems that num chans is the number of memory controllers?
    dram_controller.find("./numChans").text = str(int(ref.dram_num_memory_controllers))

    # MSHRSize could also set MSHRSize  from gpgpusim config
    instr_cache = root.find("./Library/iCache")
    instr_cache.find("./BlockSize").text = str(int(ref.l1_instr_cache_blocksize))
    instr_cache.find("./Associativity").text = str(int(ref.l1_instr_cache_assoc))
    # size is in KB
    instr_cache.find("./Size").text = str(
        cache_size_kb(
            sets=ref.l1_instr_cache_num_sets,
            block_size=ref.l1_instr_cache_blocksize,
            assoc=ref.l1_instr_cache_assoc,
        )
    )
    # int(
    #     math.floor(
    #         (
    #             ref.l1_instr_cache_num_sets
    #             * ref.l1_instr_cache_blocksize
    #             * ref.l1_instr_cache_assoc
    #         )
    #         / 1024.0
    #     )
    # )
    # )

    const_cache = root.find("./Library/constantCache")
    const_cache.find("./BlockSize").text = str(int(ref.l1_const_cache_blocksize))
    const_cache.find("./Associativity").text = str(int(ref.l1_const_cache_assoc))
    # size is in KB
    const_cache.find("./Size").text = str(
        cache_size_kb(
            sets=ref.l1_const_cache_num_sets,
            block_size=ref.l1_const_cache_blocksize,
            assoc=ref.l1_const_cache_assoc,
        )
        # int(
        #     ref.l1_const_cache_num_sets
        #     * ref.l1_const_cache_blocksize
        #     * ref.l1_const_cache_assoc
        # )
    )

    l1d = root.find("./Library/dCache")
    l1d.find("./BlockSize").text = str(int(ref.l1_data_cache_blocksize))
    l1d.find("./Associativity").text = str(int(ref.l1_data_cache_assoc))
    # size is in KB, but total and not per SM
    l1d.find("./Size").text = str(
        cache_size_kb(
            sets=ref.l1_data_cache_num_sets,
            block_size=ref.l1_data_cache_blocksize,
            assoc=ref.l1_data_cache_assoc,
        )
        * int(ref.num_tpc_sm_clusters)
        * int(ref.num_tpc_sm_per_cluster)
        # int(
        #     ref.l1_data_cache_num_sets
        #     * ref.l1_data_cache_blocksize
        #     * ref.l1_data_cache_assoc
        # )
    )

    l2 = root.find("./Library/L2")
    l2.find("./BlockSize").text = str(int(ref.l2_data_cache_blocksize))
    l2.find("./Associativity").text = str(int(ref.l2_data_cache_assoc))
    l2.find("./Size").text = str(
        cache_size_kb(
            sets=ref.l2_data_cache_num_sets,
            block_size=ref.l2_data_cache_blocksize,
            assoc=ref.l2_data_cache_assoc,
        )
        * int(ref.dram_num_sub_partitions_per_memory_controller)
        * int(ref.dram_num_memory_controllers)
    )
    # int(
    #     ref.l2_data_cache_num_sets
    #     * ref.l2_data_cache_block_size
    #     * ref.l2_data_cache_assoc
    # )

    dram_mapping = {
        "numBanks": "nbk",
        "tCCD": "CDD",
        "tCL": "CL",
        "tRP": "RP",
        "tRC": "RC",
        "tRCD": "RCD",
        "tRAS": "RAS",
        "tWR": "WR",
    }
    for tejas_key, key in dram_mapping.items():
        if ref.dram_timings.get(key) is not None:
            dram_controller.find("./" + tejas_key).text = str(
                int(ref.dram_timings[key])
            )

    new_config = ET.tostring(root)
    # try:
    #     from bs4 import BeautifulSoup
    #     new_config = BeautifulSoup(new_config, "xml").prettify()
    # except ImportError:
    #     pass

    # try:
    #     from xml.dom import minidom
    #     new_config = minidom.parseString(new_config).toprettyxml(indent="   ")
    # except ImportError:
    #     pass

    return new_config
