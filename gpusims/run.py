import itertools
from pprint import pprint
from pathlib import Path
from invoke import task
from gpusims.bench import parse_benchmarks
from gpusims.config import parse_configs
from gpusims.tejas import TejasBenchmarkConfig
from gpusims.accelsim import AccelSimBenchmarkConfig
from gpusims.multi2sim import Multi2SimBenchmarkConfig
from gpusims.native import NativeBenchmarkConfig


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


@task(
    help={
        "dir": "Benchmark dir",
        "run-dir": "Run dir",
        "simulator": "simulator to run",
        "force": "force rerunning of benchmarks",
        "benchmark": "list of benchmarks to run",
        "config": "list of configurations to run",
    },
    iterable=["benchmark", "config"],
)
def run(c, run_dir, benchmark, config, simulator, force=False, _dir=None):
    """Run benchmarks"""
    benchmark_dir = Path(__file__).parent.parent / "benchmarks"
    if _dir is not None:
        benchmark_dir = Path(_dir)

    assert benchmark_dir.is_dir()
    assert simulator.lower() in [ACCELSIM, TEJAS, MULTI2SIM]

    configs = parse_configs(benchmark_dir / "configs" / "configs.yml")
    benchmarks = parse_benchmarks(benchmark_dir / "benchmarks.yml")

    sim_run_dir = Path(run_dir) / simulator.lower()

    pending = []
    for c, b in itertools.product(config, benchmark):
        conf = configs.get(c.lower())
        if conf is None:
            have = ",".join(configs.keys())
            raise KeyError(f"no such config: {b} (have: {have})")

        bench = benchmarks.get(b.lower())
        if bench is None:
            have = ",".join(benchmarks.keys())
            raise KeyError(f"no such benchmark: {b} (have: {have})")

        bench_cls = SIMULATORS[simulator.lower()]
        bench_config = bench_cls(
            run_dir=sim_run_dir,
            benchmark=bench,
            config=conf,
        )

        pending.append(bench_config)

    for b in pending:
        pprint(b)
        bench_config.setup()
        b.run(force=force)
