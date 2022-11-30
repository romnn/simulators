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

import gpusims.gputejas as tejas

# import gpusims.gpgpusim as gpgpusim
# import gpusims.accelsim as accel

# Path().expanduser()
ns = Collection()

ROOT_DIR = Path(__file__).parent
SRC_DIR = ROOT_DIR / "gpusims"
PYTHON_FILES = [str(f) for f in SRC_DIR.rglob("*.py")]

gputejas = Collection("tejas")
gputejas.add_task(tejas.setup_unsafe, "setup")
gputejas.add_task(tejas.clean, "clean")
gputejas.add_task(tejas.build, "build")
gputejas.add_task(tejas.traces, "traces")
ns.add_collection(gputejas)


@task
def prep_mgpusim(c, deps=False):
    # MGPUSIM_ROOT = ROOT_DIR / "mgpusim"
    # _prep_gpgpusim(GPGPUSIM_ROOT)
    pass


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
def lint(c):
    """Lint code"""
    c.run(f"pipenv run flake8 {SRC_DIR}")


ns.add_task(lint, "lint")

# @task(pre=[clean_build, clean_wasm])
# def clean(c):
#     """Runs all clean sub-tasks"""
#     pass
