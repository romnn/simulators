from gpusims.simulators import *

from gpusims.native import NativeBenchmarkConfig
from gpusims.accelsim import AccelSimPTXBenchmarkConfig
from gpusims.accelsim_sass import AccelSimSASSBenchmarkConfig
from gpusims.tejas import TejasBenchmarkConfig
from gpusims.multi2sim import Multi2SimBenchmarkConfig
from gpusims.macsim import MacSimBenchmarkConfig


SIMULATORS = {
    NATIVE: NativeBenchmarkConfig,
    ACCELSIM_PTX: AccelSimPTXBenchmarkConfig,
    ACCELSIM_SASS: AccelSimSASSBenchmarkConfig,
    TEJAS: TejasBenchmarkConfig,
    MULTI2SIM: Multi2SimBenchmarkConfig,
    MACSIM: MacSimBenchmarkConfig,
}
