#!/usr/bin/env python3

import os
import re
import sys
import subprocess as sp
from pprint import pprint

if __name__ == "__main__":
    cmd = sys.argv[1:]
    ptxas_flag = "--ptxas-options=-v"
    cubin_flag = "--cubin"

    if ptxas_flag not in cmd:
        cmd.insert(1, ptxas_flag)
    if cubin_flag not in cmd:
        cmd.insert(1, cubin_flag)

    cwd = os.getcwd()
    pattern_kernel = re.compile(
        "ptxas info\s+: Compiling entry function '(\S+)' for '(\S+)'"  #  % ver
    )
    pattern_info = re.compile(
        "ptxas info\s+: Used ([0-9]+) registers, (([0-9\+]+) bytes smem)?"
    )

    cmd = " ".join(cmd)
    print(cmd)
    proc = sp.Popen(
        cmd,
        shell=True,
        cwd=cwd,
        stdout=sp.PIPE,
        stderr=sp.PIPE,
        close_fds=True,
    )

    stderr_lines = []
    (_, stdout, stderr) = (proc.stdin, proc.stdout, proc.stderr)

    occupancy_file = os.path.join(cwd, "occupancy.txt")
    with open(occupancy_file, "w") as f:
        kernel_dict = {}
        current_kernel_name = ""
        smem = ""
        register = ""

        # must read lines, just communicate does not wait long enough for some reason
        for line in stderr.readlines():
            line = line.decode("utf-8")
            stderr_lines.append(line)
            # print(line)
            match_kernel = pattern_kernel.match(line)
            match_info = pattern_info.match(line)

            if match_kernel is not None:
                current_kernel_name = match_kernel.group(1)
                # print("current kernel:", current_kernel_name)

            if match_info is not None:
                register = match_info.group(1)
                smem = 0
                # shared memory is optional: not all kernels use it
                if match_info.group(3) is not None:
                    smem = match_info.group(3)
                    smem = smem.split("+")
                    if len(smem) > 1:
                        smem = str(int(smem[0]) + int(smem[1]))
                    else:
                        smem = smem[0]

                if current_kernel_name not in kernel_dict:
                    f.write("{} {} {}\n".format(current_kernel_name, register, smem))
                    kernel_dict[current_kernel_name] = (register, smem)

        pprint(kernel_dict)

        # make sure the command succeeded
        proc.wait()
        if proc.returncode != 0:
            print("stdout:")
            print(stdout.read().decode("utf-8"))
            print("stderr:")
            print("\n".join(stderr_lines))
            raise ValueError(
                "cmd returned non zero exit code {}".format(proc.returncode)
            )
