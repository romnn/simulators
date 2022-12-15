import io
import configparser
from pprint import pprint  # noqa: F401


def merge(a, b, path=None):
    """recusively merges b into a"""
    if path is None:
        path = []
    for key in b:
        if key in a:
            merge(a[key], b[key], path + [str(key)])
        else:
            a[key] = b[key]
    return a


def configure_multi2sim(ref, m2s_template):
    # print(m2s_template)
    config = configparser.ConfigParser()
    config.optionxform = str
    config.read_string(m2s_template)

    override = {}
    if "Device" not in override:
        override["Device"] = {}

    override["Device"]["Frequency"] = str(int(ref.core_clock_mhz))
    override["Device"]["NumSMs"] = str(
        ref.num_tpc_sm_clusters * ref.num_tpc_sm_per_cluster
    )

    if "SM" not in override:
        override["SM"] = {}
    # default NumWarpPools is 4, if we set less here, the simulator crashes
    # config["SM"]["NumWarpPools"] = str(ref.num_warp_schedulers_per_sm)
    override["SM"]["MaxWarpsPerWarpPool"] = str(ref.max_warps_per_sm)
    override["SM"]["NumRegisters"] = str(ref.num_registers_per_sm)

    pprint(override)
    config = merge(config, override)

    with io.StringIO() as s:
        config.write(s)
        s.seek(0)
        return s.read().encode("utf-8")
