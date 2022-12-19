import os
import datetime
import itertools
from pprint import pprint
from pathlib import Path
from invoke import task

import gpusims
import gpusims.cuda as cuda
import gpusims.utils as utils
from gpusims.bench import parse_benchmarks


ROOT_DIR = Path(__file__).parent.parent


@task(
    help={
        "benchmark-dir": "Benchmark dir",
        "run-dir": "Run dir",
        "simulator": "simulator to run",
        "benchmark": "list of benchmarks to run",
        "config": "list of configurations to run",
        "repetitions": "number of repetitions (only applies to native execution)",
        "timeout-mins": "timeout in minutes per simulation run",
        "slurm": "submit jobs using slurm (only for native and accelsim-sass)",
        "slurm-node": "the slurm node to use",
        "simulate": "run simulation",
        "trace": "generate traces",
        "parse-only": "only parse results",
        "dry-run": "print commands that would be executed but do not simulate",
        "enable": "force running disabled benchmarks or inputs",
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
    benchmark_dir=None,
    timeout_mins=30,
    slurm=False,
    slurm_node=None,
    simulate=True,
    trace=False,
    parse_only=False,
    dry_run=False,
    enable=False,
):
    """Run benchmarks"""
    simulator = simulator.lower()
    benchmark = [b.lower() for b in benchmark]

    if benchmark_dir is not None:
        benchmark_dir = Path(benchmark_dir)
    else:
        benchmark_dir = Path(__file__).parent.parent / "benchmarks"

    print("running benchmarks ...")
    assert benchmark_dir.is_dir()
    if simulator not in gpusims.SIMULATORS:
        raise ValueError("unknown simulator: {}".format(simulator))

    configs = gpusims.config.parse_configs(benchmark_dir / "configs" / "configs.yml")
    benchmarks = parse_benchmarks(benchmark_dir / "benchmarks.yml")
    assert len(configs) > 0
    assert len(benchmarks) > 0

    run_dir = Path(run_dir)
    sim_run_dir = run_dir / simulator.lower()

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
    for config_name, b in list(itertools.product(config, benchmark)):
        conf = configs.get(config_name.lower())
        if simulator == gpusims.NATIVE:
            if conf is None:
                # create a mock config
                conf = gpusims.config.Config(
                    key=config_name.lower(),
                    name=config_name.lower(),
                    path=None,
                    spec={},
                )
            else:
                # do not inject any config files into the run dir
                conf = gpusims.config.Config(
                    key=conf.key, name=conf.name, path=None, spec=conf.spec
                )

        if conf is None:
            have = ",".join(configs.keys())
            raise KeyError("no such config: {} (have: {})".format(config_name, have))

        bench = benchmarks.get(b)
        if bench is None:
            have = ",".join(benchmarks.keys())
            raise KeyError("no such benchmark: {} (have: {})".format(b, have))

        # check if the benchmark is enabled
        if not enable and not bench.enabled(simulator):
            print("skipping {} {} ...".format(config_name, b))
            continue

        print("adding {} {} ...".format(config_name, b))
        bench_cls = gpusims.SIMULATORS[simulator.lower()]
        bench_config = bench_cls(
            run_dir=sim_run_dir,
            benchmark=bench,
            config=conf,
        )

        pending.append(bench_config)

    for b in pending:
        pprint(b)
        if slurm:
            cmd = [
                "inv",
                "run",
                "--benchmark-dir",
                str(benchmark_dir.absolute()),
                "--run-dir",
                str(run_dir.absolute()),
                "--simulator",
                simulator,
                "--config",
                b.config.key,
                "--benchmark",
                b.benchmark.name.lower(),
                "--repetitions",
                str(repetitions),
                "--timeout-mins",
                str(timeout_mins),
                "--simulate" if simulate else "--no-simulate",
            ]
            if trace:
                cmd += ["--trace"]
            if parse_only:
                cmd += ["--parse-only"]

            slurm_job_name = "-".join(
                [
                    simulator,
                    b.benchmark.sanitized_name(),
                    utils.slugify(b.config.key.lower()),
                ]
            )

            slurm_dir = ROOT_DIR / ".slurm"
            slurm_job_file = (slurm_dir / slurm_job_name).with_suffix(".job")
            slurm_stdout_file = (slurm_dir / slurm_job_name).with_suffix(".stdout")
            slurm_stderr_file = (slurm_dir / slurm_job_name).with_suffix(".stderr")

            slurm_job = [
                "#!/bin/sh",
                "#SBATCH --job-name={}".format(slurm_job_name),
                "#SBATCH --output={}".format(str(slurm_stdout_file.absolute())),
                "#SBATCH --error={}".format(str(slurm_stderr_file.absolute())),
                # format: 00:15:00"
                "#SBATCH --time={}".format(
                    utils.duration_to_slurm(datetime.timedelta(minutes=timeout_mins))
                ),
                "#SBATCH -N 1",
                "#SBATCH --gres=gpu:1",
            ]

            if slurm_node is not None:
                slurm_job += ["#SBATCH -C {}".format(slurm_node)]

            slurm_job += [" ".join(cmd)]

            os.makedirs(str(slurm_job_file.parent.absolute()), exist_ok=True)

            with open(str(slurm_job_file.absolute()), "w") as f:
                f.write("\n".join(slurm_job))
            print("written job to", str(slurm_job_file.absolute()))

            if not dry_run:
                # submit the job
                submit_cmd = ["sbatch", str(slurm_job_file.absolute())]
                c.run(" ".join(submit_cmd))

            # module load cuda11.2/toolkit
            # !/bin/bash
            # SBATCH --time=00:15:00
            # SBATCH -N 2
            # SBATCH --ntasks-per-node=16
            # . /etc/bashrc
            # DAS-5:
            # . /etc/profile.d/modules.sh
            # DAS-6:
            # . /etc/profile.d/lmod.sh
            # module load openmpi/gcc/64

        else:
            for inp in b.benchmark.inputs:
                if enable or inp.enabled(simulator):
                    b.run(
                        inp=inp,
                        repetitions=repetitions,
                        timeout_mins=timeout_mins,
                        parse_only=parse_only,
                        simulate=simulate,
                        trace=trace,
                        dry_run=dry_run,
                    )
