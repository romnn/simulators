"""
tasks for maintaining the project.
run 'invoke --list' for guidance on using invoke
"""
import shutil
import sys
import re
import os
import os.path as op
import subprocess as sp
from pprint import pprint
from invoke import task, Collection
from pathlib import Path

import gpusims
import gpusims.run

ROOT_DIR = Path(__file__).parent
RUN_DIR = ROOT_DIR / "run"
BENCHMARK_DIR = ROOT_DIR / "benchmarks"
SRC_DIR = ROOT_DIR / "gpusims"
PYTHON_FILES = [str(f) for f in SRC_DIR.rglob("*.py")]

ns = Collection()
ns.add_task(gpusims.run.run, "run")


@task(help={"check": "Checks formatting without applying changes"})
def fmt(c, check=False):
    """Format code"""
    options = []
    if check:
        options.append("--diff")
    cmd = ["pipenv", "run", "black"]
    cmd += options
    cmd += PYTHON_FILES
    c.run(" ".join(cmd))


ns.add_task(fmt, "format")


@task
def chown(c):
    """Fix permission for the benchmark run dir"""
    c.run("sudo chown -R $(id -u):$(id -g) {}".format(str(RUN_DIR.absolute())))
    c.run("sudo chmod -R 744 {}".format(str(RUN_DIR.absolute())))


ns.add_task(chown, "chown")


@task
def lint(c):
    """Lint code"""
    c.run("pipenv run flake8 {}".format(SRC_DIR))


ns.add_task(lint, "lint")


@task()
def build_tools(c):
    """Build tools"""
    cmd = ["rustup", "target", "add", "x86_64-unknown-linux-musl"]
    cmd = " ".join(cmd)
    print(cmd)
    c.run(cmd)

    cmd = [
        "cargo",
        "build",
        "--release",
        "--all-targets",
        "--target",
        "x86_64-unknown-linux-musl",
    ]
    cmd = " ".join(cmd)
    print(cmd)
    c.run(cmd)


ns.add_task(build_tools, "build-tools")


def build_container(c, ctx, tag, dockerfile=None, dry_run=False):
    cmd = ["docker", "build", "-t", tag]
    if dockerfile is not None:
        cmd += ["-f", str(dockerfile.absolute())]
    cmd += [str(ctx.absolute())]
    cmd = " ".join(cmd)
    print(cmd)
    if not dry_run:
        c.run(cmd)


@task(
    help={
        "base": "also rebuild the base containers",
        "simulator": "simulator to build container",
        "dry-run": "print commands that would be run without executing",
    },
    iterable=["simulator"],
)
def build(c, simulator, base=False, dry_run=False):
    """Build all docker containers"""
    simulator = [s.lower() for s in simulator]
    for s in simulator:
        if s not in gpusims.SIMULATORS:
            have = list(gpusims.SIMULATORS.keys())
            raise ValueError("unknown simulator: {} (have {})".format(s, have))
    should_build = [
        s for s in gpusims.SIMULATORS.keys() if len(simulator) < 1 or s in simulator
    ]
    print("==> building", should_build)

    containers = []
    for sim in should_build:
        if base:
            # base container first
            containers.append(gpusims.CONTAINERS[sim].base)
        containers.append(gpusims.CONTAINERS[sim].bench)

    built = set()
    for container in containers:
        if container.tag in built:
            continue

        for dep in container.dependencies:
            if not base:
                break
            # build dependencies first
            if dep.tag in built:
                continue
            build_container(
                c,
                ctx=dep.ctx,
                tag=dep.tag,
                dockerfile=dep.dockerfile,
                dry_run=dry_run,
            )
            built.add(dep.tag)

        build_container(
            c,
            ctx=container.ctx,
            tag=container.tag,
            dockerfile=container.dockerfile,
            dry_run=dry_run,
        )
        built.add(container.tag)


ns.add_task(build, "build")


@task(
    help={
        "benchmark": "list of benchmarks to run",
        "simulator": "simulator to build container",
        "force": "force rerunning of benchmarks",
        "config": "list of configurations to run",
        "repetitions": "number of repetitions (only applies to native execution)",
        "timeout-mins": "timeout in minutes per simulation run",
        "dry-run": "dry run only prints commands that would be executed",
        "local": "disable mapping the run output volume, which will not write benchmark results",
        # "slurm": "submit jobs using slurm (only for native and accelsim-sass)",
        "trace-only": "only generate traces, but do not simulate",
        "parse-only": "only parse results",
        "enable": "force running disabled benchmarks or inputs",
    },
    iterable=["simulator", "benchmark", "config"],
)
def bench(
    c,
    simulator,
    benchmark,
    config,
    repetitions=3,
    force=False,
    timeout_mins=30,
    dry_run=False,
    local=False,
    # slurm=False,
    trace_only=False,
    parse_only=False,
    enable=False,
):
    """Benchmark in simulator inside docker containers"""
    simulator = set([s.lower() for s in simulator])
    for s in simulator:
        if s not in gpusims.SIMULATORS:
            have = list(gpusims.SIMULATORS.keys())
            raise ValueError("unknown simulator: {} (have {})".format(s, have))
    if len(simulator) < 1:
        # set default simulators
        # by default, do not run native and accelsim when running everything
        # this way, we enforce running them separately and explicitely specifying the config
        simulator = simulator.union(
            [
                s
                for s in gpusims.SIMULATORS
                if s not in [gpusims.NATIVE, gpusims.ACCELSIM_SASS]
            ]
        )

    simulators = sorted([s for s in gpusims.SIMULATORS if s in simulator])
    print(simulators)

    for s in simulators:
        print("==> benchmarking", s)
        container = gpusims.CONTAINERS[s].bench
        container_run_dir = "/benchrun"
        host_run_dir = ROOT_DIR / "run"
        os.makedirs(str(host_run_dir.absolute()), exist_ok=True)

        # map volumes into container
        volumes = {
            ROOT_DIR / "tasks.py": "/tasks.py",
            ROOT_DIR / "gpusims": "/gpusims",
        }
        if not local:
            volumes.update(
                {
                    host_run_dir: container_run_dir,
                }
            )
        cmd = ["docker", "run"]
        if s in [gpusims.ACCELSIM_SASS, gpusims.NATIVE]:
            # map in the GPU device
            cmd += ["--cap-add", "SYS_ADMIN", "--privileged", "--gpus", "all"]
        for src, dest in volumes.items():
            cmd += ["-v", "{}:{}".format(str(src.absolute()), dest)]

        cmd += [container.tag]
        cmd += ["inv", "run", "--simulator", s, "--run-dir", container_run_dir]
        # if slurm:
        #     cmd += ["--slurm"]
        if enable:
            cmd += ["--enable"]
        if trace_only:
            cmd += ["--parse-only"]
        if trace_only:
            cmd += ["--trace-only"]
        if timeout_mins is not None:
            cmd += ["--timeout-mins", str(timeout_mins)]
        if repetitions is not None:
            cmd += ["--repetitions", str(repetitions)]
        for cfg in config:
            cmd += ["--config", cfg]
        for b in benchmark:
            cmd += ["--benchmark", b]

        cmd = " ".join(cmd)
        print(cmd)
        if not dry_run:
            c.run(cmd)


ns.add_task(bench, "bench")


@task(pre=[chown])
def pack(c):
    """ create a tar archive with the results """
    cmd = ["tar", "-czf", "run.tar.gz", "-C", str(ROOT_DIR.absolute()), "run"]
    cmd = " ".join(cmd)
    print(cmd)
    c.run(cmd)


ns.add_task(pack, "pack")


@task
def configure_all(c):
    # read reference config
    config_dir = ROOT_DIR / "benchmarks" / "configs"
    configs = gpusims.config.parse_configs(config_dir / "configs.yml")
    config_templates = {
        gpusims.TEJAS: (
            gpusims.config.tejas.configure_tejas,
            config_dir / "tejas_default_config.xml",
            "tejas_config.xml",
        ),
        gpusims.MULTI2SIM: (
            gpusims.config.multi2sim.configure_multi2sim,
            None,
            "m2s.config.ini",
        ),
        gpusims.MACSIM: (
            gpusims.config.macsim.configure_macsim,
            config_dir / "macsim_default_params_gtx580",
            "params.in",
        ),
    }

    for config_name, config in configs.items():
        ref_config = config.path / "gpgpusim.config"
        print("configuring {} (reference config {})".format(config_name, ref_config))
        with open(ref_config, "r") as f:
            gpgpusim_config = f.read()

        gpgpusim_config = gpusims.config.gpgpusim.parse_gpgpusim_config(gpgpusim_config)
        pprint(gpgpusim_config._asdict())

        for simulator, (func, template_file, out_name) in config_templates.items():
            template = ""
            if template_file is not None:
                with open(template_file, "r") as f:
                    template = f.read()

            new_config = func(gpgpusim_config, template)
            # print(new_config.decode("utf-8"))
            out_file = config.path / out_name
            print("generated {}".format(out_file))
            with open(str(out_file.absolute()), "wb") as f:
                f.write(new_config)


ns.add_task(configure_all, "configure-all")


@task(
    help={
        "simulator": "simulator to generate config for",
        "base": "base config file path (gpgpusim config)",
        "template": "template file path",
        "out": "output config file path",
        "verbose": "print the new config to stdout",
    },
)
def configure(c, simulator, base, template=None, out=None, verbose=False):
    """Configure simulator based on gpgpusim base config parameters and a template"""
    simulator = simulator.lower()
    if simulator not in gpusims.SIMULATORS:
        have = list(gpusims.SIMULATORS.keys())
        raise ValueError("unknown simulator: {} (have {})".format(simulator, have))

    # read reference config
    with open(base, "r") as f:
        gpgpusim_config = f.read()

    # read template config
    config = ""
    if template is not None:
        with open(template, "r") as f:
            config = f.read()

    # print(gpgpusim_config)
    # print(config)
    gpgpusim_config = gpusims.config.gpgpusim.parse_gpgpusim_config(gpgpusim_config)
    pprint(gpgpusim_config._asdict())

    if simulator == gpusims.TEJAS:
        new_config = gpusims.config.tejas.configure_tejas(gpgpusim_config, config)
    elif simulator == gpusims.MULTI2SIM:
        new_config = gpusims.config.multi2sim.configure_multi2sim(
            gpgpusim_config, config
        )
    elif simulator == gpusims.MACSIM:
        new_config = gpusims.config.macsim.configure_macsim(gpgpusim_config, config)
    else:
        raise ValueError("cannot configure {}".format(simulator))

    if verbose:
        print("new config:")
        print(new_config.decode("utf-8"))
    if out is not None:
        out = Path(out)
        os.makedirs(out.parent, exist_ok=True)
        with open(str(out.absolute()), "wb") as f:
            f.write(new_config)


ns.add_task(configure, "configure")


@task
def migrate(c):
    """temporary migration"""
    import itertools
    from gpusims.bench import parse_benchmarks
    from gpusims.utils import slugify

    simulators = gpusims.SIMULATORS
    configs = gpusims.config.parse_configs(BENCHMARK_DIR / "configs" / "configs.yml")
    benchmarks = parse_benchmarks(BENCHMARK_DIR / "benchmarks.yml")
    for sim, conf, bench in list(
        itertools.product(simulators, configs.values(), benchmarks.values())
    ):
        # if not bench.enabled(sim):
        #     continue
        # print(sim, conf, bench)
        sim_run_dir = RUN_DIR / sim
        config_name = slugify(conf.key.lower())
        old_path = sim_run_dir / bench.sanitized_name() / config_name
        new_path = sim_run_dir / config_name / bench.sanitized_name()
        if not old_path.is_dir():
            # print("missing", old_path)
            pass
        else:
            print("mkdir", str(new_path.parent.absolute()))
            print(old_path, "=>", new_path)
            # os.makedirs(str(new_path.parent.absolute()), exist_ok=True)
            # old_path.rename(new_path)


ns.add_task(migrate, "migrate")


@task(pre=[])
def clean(c):
    """Performs cleanup"""
    make_clean_cmd = ["make", "-C", str(BENCHMARK_DIR.absolute()), "clean"]
    c.run(" ".join(make_clean_cmd))
    slurm_output_files = list(ROOT_DIR.rglob("slurm-*.out"))
    for slurm_output_file in slurm_output_files:
        slurm_output_file.unlink()
    c.run("rm -rf {}".format(str((ROOT_DIR / ".slurm").absolute())))


ns.add_task(clean, "clean")
