from pathlib import Path

from gpusims.native import NativeBenchmarkConfig
from gpusims.accelsim import AccelSimBenchmarkConfig
from gpusims.tejas import TejasBenchmarkConfig
from gpusims.multi2sim import Multi2SimBenchmarkConfig

ROOT_DIR = Path(__file__).parent.parent

NATIVE = "native"
ACCELSIM = "accelsim"
TEJAS = "tejas"
MULTI2SIM = "m2s"

SIMULATORS = {
    NATIVE: NativeBenchmarkConfig,
    ACCELSIM: AccelSimBenchmarkConfig,
    TEJAS: TejasBenchmarkConfig,
    MULTI2SIM: Multi2SimBenchmarkConfig,
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
    ACCELSIM: dict(
        bench=dict(
            tag="romnn/accelsim-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/accelsim/bench.dockerfile",
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
}
