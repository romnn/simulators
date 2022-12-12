import io
import configparser
from pprint import pprint  # noqa: F401


def configure_multi2sim(ref, m2s_template):
    # print(m2s_template)
    config = configparser.ConfigParser()
    config.optionxform = str
    config.read_string(m2s_template)

    if "Device" not in config.sections():
        config["Device"] = {}

    config["Device"]["Frequency"] = str(int(ref.core_clock_mhz))
    config["Device"]["NumSMs"] = str(
        ref.num_tpc_sm_clusters * ref.num_tpc_sm_per_cluster
    )

    if "SM" not in config.sections():
        config["SM"] = {}
    # default NumWarpPools is 4, if we set less here, the simulator crashes
    # config["SM"]["NumWarpPools"] = str(ref.num_warp_schedulers_per_sm)
    config["SM"]["MaxWarpsPerWarpPool"] = str(ref.max_warps_per_sm)
    config["SM"]["NumRegisters"] = str(ref.num_registers_per_sm)

    with io.StringIO() as s:
        config.write(s)
        s.seek(0)
        return s.read().encode("utf-8")
