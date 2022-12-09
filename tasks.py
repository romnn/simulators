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
SRC_DIR = ROOT_DIR / "gpusims"
PYTHON_FILES = [str(f) for f in SRC_DIR.rglob("*.py")]

ns = Collection()
ns.add_task(gpusims.run.run, "run")

# gputejas = Collection("tejas")
# gputejas.add_task(tejas.setup_unsafe, "setup")
# gputejas.add_task(tejas.clean, "clean")
# gputejas.add_task(tejas.build, "build")
# gputejas.add_task(tejas.traces, "traces")
# ns.add_collection(gputejas)


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


def build_container(c, ctx, tag, dockerfile=None):
    cmd = ["docker", "build", "-t", tag]
    if dockerfile is not None:
        cmd += ["-f", str(dockerfile.absolute())]
    cmd += [str(ctx.absolute())]
    cmd = " ".join(cmd)
    c.run(cmd)


@task(
    help={
        "base": "also rebuild the base containers",
        "simulator": "simulator to build container",
    },
    iterable=["simulator"],
)
def build(c, simulator, base=False):
    """Build all the benchmark docker containers"""
    simulator = [s.lower() for s in simulator]
    should_build = [
        s for s in gpusims.SIMULATORS.keys() if len(simulator) < 1 or s in simulator
    ]
    print("==> building", should_build)

    containers = []
    for sim in should_build:
        if base:
            # base container first
            containers.append(gpusims.CONTAINERS[sim]["base"])
        containers.append(gpusims.CONTAINERS[sim]["bench"])

    for container in containers:
        build_container(
            c,
            ctx=container.get("ctx"),
            tag=container.get("tag"),
            dockerfile=container.get("dockerfile"),
        )


ns.add_task(build, "build")


@task(
    # post=[chown],
    help={
        "benchmark": "list of benchmarks to run",
        "simulator": "simulator to build container",
        "force": "force rerunning of benchmarks",
        "config": "list of configurations to run",
        "repetitions": "number of repetitions (only applies to native execution)",
    },
    iterable=["simulator", "benchmark", "config"],
)
def bench(c, simulator, benchmark, config, repetitions=5, force=False):
    """Benchmark in simulator inside docker containers"""
    simulator = [s.lower() for s in simulator]
    for s in simulator:
        if s not in gpusims.SIMULATORS:
            raise ValueError("unknown simulator: {}".format(s))
    simulators = [s for s in gpusims.SIMULATORS if s in simulator or len(simulator) < 1]
    print(simulators)

    for s in simulators:
        print("==> benchmarking", s)
        container = gpusims.CONTAINERS[s]["bench"]
        container_run_dir = "/benchrun"
        volumes = {
            ROOT_DIR / "tasks.py": "/tasks.py",
            ROOT_DIR / "gpusims": "/gpusims",
            ROOT_DIR / "run": container_run_dir,
        }
        cmd = ["docker", "run"]
        if s == gpusims.NATIVE:
            # map in the GPU
            cmd += ["--cap-add", "SYS_ADMIN", "--privileged", "--gpus", "all"]
        for src, dest in volumes.items():
            cmd += ["-v", "{}:{}".format(str(src.absolute()), dest)]

        cmd += [container["tag"]]
        cmd += ["inv", "run", "--simulator", s, "--run-dir", container_run_dir]
        for cfg in config:
            cmd += ["--config", cfg]
        for b in benchmark:
            cmd += ["--benchmark", b]

        cmd = " ".join(cmd)
        print(cmd)
        c.run(cmd)


ns.add_task(bench, "bench")


# @task(pre=[clean_build, clean_wasm])
# def clean(c):
#     """Runs all clean sub-tasks"""
#     pass
