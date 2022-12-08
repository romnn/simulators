import sys
import stat
import shutil
import os
import shlex
import subprocess as sp
from pathlib import Path


class ExecError(Exception):
    def __init__(self, cmd, status, stdout, stderr):
        self.cmd = cmd
        self.status = status
        self.stdout = stdout
        self.stderr = stderr
        super().__init__(
            "command completed with non-zero exit code ({})".format(status)
        )


def run_cmd(cmd, cwd=None, shell=False, timeout_sec=None, env=None):
    if not shell and not isinstance(cmd, list):
        cmd = shlex.split(cmd)
    print("running", cmd)

    if isinstance(cwd, Path):
        cwd = str(cwd.absolute())

    # the subprocess may take a long time, hence flush all buffers before
    sys.stdout.flush()
    proc = sp.Popen(cmd, stdout=sp.PIPE, stderr=sp.PIPE, cwd=cwd, env=env, shell=shell)
    try:
        stdout, stderr = proc.communicate(timeout=timeout_sec)
    except sp.TimeoutExpired as timeout:
        raise timeout

    stdout = stdout.decode("utf-8")
    stderr = stderr.decode("utf-8")
    if proc.returncode != 0:
        print("stdout:")
        print(stdout)
        print("stderr:")
        print(stderr)
        raise ExecError(cmd=cmd, status=proc.returncode, stdout=stdout, stderr=stderr)

    return proc.returncode, stdout, stderr


def ensure_empty(d):
    try:
        shutil.rmtree(str(d.absolute()))
    except FileNotFoundError:
        pass
    # also creates parents
    os.makedirs(str(d.absolute()), exist_ok=True)


def chmod_x(executable):
    executable.chmod(executable.stat().st_mode | stat.S_IEXEC)


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
