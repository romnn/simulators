import shutil
import os
import shlex
import subprocess as sp
from pathlib import Path


def run_cmd(cmd, cwd=None, timeout_sec=None):
    cmd = shlex.split(cmd)
    print("running", cmd)

    if isinstance(cwd, Path):
        cwd = str(cwd.absolute())
    proc = sp.Popen(cmd, stdout=sp.PIPE, stderr=sp.PIPE, cwd=cwd)
    ret = proc.wait(timeout=timeout_sec)
    stdout, stderr = proc.communicate()
    print(stdout.decode("utf-8"))
    print(stderr.decode("utf-8"))
    if ret != 0:
        raise ValueError("{} failed".format(cmd))

    pass


def ensure_empty(d):
    try:
        shutil.rmtree(str(d.absolute()))
    except FileNotFoundError:
        pass
    # also creates parents
    os.makedirs(str(d.absolute()), exist_ok=True)


def merge_dicts(*dicts):
    merged = {}
    for d in dicts:
        merged.update(d)
    return merged


def dedup_stable(seq):
    seen = set()
    return [x for x in seq if not (x in seen or seen.add(x))]


def add_paths_to_env(env, paths):
    new_paths = os.environ.get(env, "").split(os.pathsep)
    new_paths += [str(p) for p in paths]
    # filter empty paths
    new_paths = [p for p in new_paths if len(p) > 0]
    new_paths = os.pathsep.join(dedup_stable(new_paths))
    os.environ[env] = new_paths
    print("{}={}".format(env, new_paths))
    return new_paths
