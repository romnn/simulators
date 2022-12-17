import itertools
from pprint import pprint
from pathlib import Path
from invoke import task

import gpusims
import gpusims.cuda as cuda
from gpusims.bench import parse_benchmarks


@task(
    help={
        "dir": "Benchmark dir",
        "run-dir": "Run dir",
        "simulator": "simulator to run",
        "force": "force rerunning of benchmarks",
        "benchmark": "list of benchmarks to run",
        "config": "list of configurations to run",
        "repetitions": "number of repetitions (only applies to native execution)",
        "timeout-mins": "timeout in minutes per simulation run",
    },
    iterable=["benchmark", "config"],
)
def run(
    c,
    run_dir,
    benchmark,
    config,
    simulator,
    repetitions=3,
    force=False,
    _dir=None,
    timeout_mins=10,
):
    """Run benchmarks"""
    simulator = simulator.lower()
    benchmark = [b.lower() for b in benchmark]

    benchmark_dir = Path(__file__).parent.parent / "benchmarks"
    if _dir is not None:
        benchmark_dir = Path(_dir)

    print("running benchmarks ...")
    assert benchmark_dir.is_dir()
    if simulator not in gpusims.SIMULATORS:
        raise ValueError("unknown simulator: {}".format(simulator))

    configs = gpusims.config.parse_configs(benchmark_dir / "configs" / "configs.yml")
    benchmarks = parse_benchmarks(benchmark_dir / "benchmarks.yml")
    assert len(configs) > 0
    assert len(benchmarks) > 0

    sim_run_dir = Path(run_dir) / simulator.lower()

    if len(config) < 1:
        if simulator == gpusims.NATIVE:
            # find current hardware
            devices = cuda.get_devices()
            config = [devices[0].name]
        else:
            # run all configs
            config = list(configs.keys())

    if len(benchmark) < 1:
        benchmark = list(benchmarks.keys())

    pending = []
    for c, b in list(itertools.product(config, benchmark)):
        conf = configs.get(c.lower())
        if simulator == gpusims.NATIVE:
            if conf is None:
                # create a mock config
                conf = gpusims.config.Config(
                    key=c.lower(), name=c.lower(), path=None, spec={}
                )
            else:
                # do not inject any config files into the run dir
                conf = gpusims.config.Config(
                    key=conf.key, name=conf.name, path=None, spec=conf.spec
                )

        if conf is None:
            have = ",".join(configs.keys())
            raise KeyError("no such config: {} (have: {})".format(c, have))

        bench = benchmarks.get(b)
        if bench is None:
            have = ",".join(benchmarks.keys())
            raise KeyError("no such benchmark: {} (have: {})".format(b, have))

        # check if the benchmark is enabled
        if not bench.enabled(simulator):
            print("skipping {} {} ...".format(c, b))
            continue

        print("adding {} {} ...".format(c, b))
        bench_cls = gpusims.SIMULATORS[simulator.lower()]
        bench_config = bench_cls(
            run_dir=sim_run_dir,
            benchmark=bench,
            config=conf,
        )

        pending.append(bench_config)

    for b in pending:
        pprint(b)
        for inp in b.benchmark.inputs:
            if inp.enabled(simulator):
                b.run(
                    inp, repetitions=repetitions, force=force, timeout_mins=timeout_mins
                )
