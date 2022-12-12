import xml.etree.ElementTree as ET


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
