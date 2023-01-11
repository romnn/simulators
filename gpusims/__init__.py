from pathlib import Path
from collections import namedtuple

import gpusims.config  # noqa: F401

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

BenchmarkContainerSpec = namedtuple(
    "BenchmarkContainerSpec",
    ["bench", "base"],
)

Container = namedtuple(
    "Container",
    [
        "tag",
        "ctx",
        "dockerfile",
        "dependencies",
    ],
)

IMAGE_REPO = "gpgpusims"

TOOLS_CONTAINER = Container(
    tag=IMAGE_REPO + "/tools",
    ctx=ROOT_DIR,
    dockerfile=ROOT_DIR / "docker/tools.dockerfile",
    dependencies=[],
)

OCELOT_CONTAINER = Container(
    tag=IMAGE_REPO + "/ocelot",
    ctx=ROOT_DIR / "docker/ocelot",
    dockerfile=ROOT_DIR / "docker/ocelot/original.dockerfile",
    dependencies=[],
)


CONTAINERS = {
    NATIVE: BenchmarkContainerSpec(
        bench=Container(
            tag=IMAGE_REPO + "/native-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/native/bench.dockerfile",
            dependencies=[TOOLS_CONTAINER],
        ),
        base=Container(
            tag=IMAGE_REPO + "/native-base",
            ctx=ROOT_DIR / "docker/native",
            dockerfile=ROOT_DIR / "docker/native/base.dockerfile",
            dependencies=[],
        ),
    ),
    ACCELSIM_PTX: BenchmarkContainerSpec(
        bench=Container(
            tag=IMAGE_REPO + "/accelsim-ptx-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/accelsim/bench.ptx.dockerfile",
            dependencies=[TOOLS_CONTAINER],
        ),
        base=Container(
            tag=IMAGE_REPO + "/accelsim-base",
            ctx=ROOT_DIR / "docker/accelsim",
            dockerfile=ROOT_DIR / "docker/accelsim/base.dockerfile",
            dependencies=[],
        ),
    ),
    ACCELSIM_SASS: BenchmarkContainerSpec(
        bench=Container(
            tag=IMAGE_REPO + "/accelsim-sass-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/accelsim/bench.sass.dockerfile",
            dependencies=[TOOLS_CONTAINER],
        ),
        base=Container(
            tag=IMAGE_REPO + "/accelsim-base",
            ctx=ROOT_DIR / "docker/accelsim",
            dockerfile=ROOT_DIR / "docker/accelsim/base.dockerfile",
            dependencies=[],
        ),
    ),
    TEJAS: BenchmarkContainerSpec(
        bench=Container(
            tag=IMAGE_REPO + "/tejas-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/tejas/bench.dockerfile",
            dependencies=[TOOLS_CONTAINER, OCELOT_CONTAINER],
        ),
        base=Container(
            tag=IMAGE_REPO + "/tejas-base",
            ctx=ROOT_DIR / "docker/tejas",
            dockerfile=ROOT_DIR / "docker/tejas/base.dockerfile",
            dependencies=[OCELOT_CONTAINER],
        ),
    ),
    MULTI2SIM: BenchmarkContainerSpec(
        bench=Container(
            tag=IMAGE_REPO + "/m2s-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/m2s/bench.dockerfile",
            dependencies=[TOOLS_CONTAINER],
        ),
        base=Container(
            tag=IMAGE_REPO + "/m2s-base",
            ctx=ROOT_DIR / "docker/m2s",
            dockerfile=ROOT_DIR / "docker/m2s/base.dockerfile",
            dependencies=[],
        ),
    ),
    MACSIM: BenchmarkContainerSpec(
        bench=Container(
            tag=IMAGE_REPO + "/macsim-bench",
            ctx=ROOT_DIR,
            dockerfile=ROOT_DIR / "docker/macsim/bench.dockerfile",
            dependencies=[TOOLS_CONTAINER, OCELOT_CONTAINER],
        ),
        base=Container(
            tag=IMAGE_REPO + "/macsim-base",
            ctx=ROOT_DIR / "docker/macsim",
            dockerfile=ROOT_DIR / "docker/macsim/base.dockerfile",
            dependencies=[OCELOT_CONTAINER],
        ),
    ),
}
