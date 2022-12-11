from pathlib import Path

from gpusims.native import NativeBenchmarkConfig
from gpusims.accelsim import AccelSimPTXBenchmarkConfig
from gpusims.accelsim_sass import AccelSimSASSBenchmarkConfig
from gpusims.tejas import TejasBenchmarkConfig
from gpusims.multi2sim import Multi2SimBenchmarkConfig
from gpusims.macsim import MacSimBenchmarkConfig

ROOT_DIR = Path(__file__).parent.parent

NATIVE = "native"
ACCELSIM_PTX = "accelsim-ptx"
ACCELSIM_SASS = "accelsim-sass"
TEJAS = "tejas"
MULTI2SIM = "m2s"
MACSIM = "macsim"

SIMULATORS = {
    NATIVE: NativeBenchmarkConfig,
    ACCELSIM_PTX: AccelSimPTXBenchmarkConfig,
    ACCELSIM_SASS: AccelSimSASSBenchmarkConfig,
    TEJAS: TejasBenchmarkConfig,
    MULTI2SIM: Multi2SimBenchmarkConfig,
    MACSIM: MacSimBenchmarkConfig,
}

CONTAINERS = {
    NATIVE: dict(
        bench=dict(
            tag="romnn/native-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/native/bench.dockerfile",
        ),
        base=dict(
            tag="romnn/native-base",
            ctx=ROOT_DIR / "docker/native",
            dockerfile=ROOT_DIR / "docker/native/base.dockerfile",
        ),
    ),
    ACCELSIM_PTX: dict(
        bench=dict(
            tag="romnn/accelsim-ptx-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/accelsim/bench.ptx.dockerfile",
        ),
        base=dict(
            tag="romnn/accelsim-base",
            ctx=ROOT_DIR / "docker/accelsim",
            dockerfile=ROOT_DIR / "docker/accelsim/base.dockerfile",
        ),
    ),
    ACCELSIM_SASS: dict(
        bench=dict(
            tag="romnn/accelsim-sass-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/accelsim/bench.sass.dockerfile",
        ),
        base=dict(
            tag="romnn/accelsim-base",
            ctx=ROOT_DIR / "docker/accelsim",
            dockerfile=ROOT_DIR / "docker/accelsim/base.dockerfile",
        ),
    ),
    TEJAS: dict(
        bench=dict(
            tag="romnn/tejas-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/tejas/bench.dockerfile",
        ),
        base=dict(
            tag="romnn/tejas-base",
            ctx=ROOT_DIR / "docker/tejas",
            dockerfile=ROOT_DIR / "docker/tejas/base.dockerfile",
        ),
    ),
    MULTI2SIM: dict(
        bench=dict(
            tag="romnn/m2s-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/m2s/bench.dockerfile",
        ),
        base=dict(
            tag="romnn/m2s-base",
            ctx=ROOT_DIR / "docker/m2s",
            dockerfile=ROOT_DIR / "docker/m2s/base.dockerfile",
        ),
    ),
    MACSIM: dict(
        bench=dict(
            tag="romnn/macsim-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/macsim/bench.dockerfile",
        ),
        base=dict(
            tag="romnn/macsim-base",
            ctx=ROOT_DIR / "docker/macsim",
            dockerfile=ROOT_DIR / "docker/macsim/base.dockerfile",
        ),
    ),
}
