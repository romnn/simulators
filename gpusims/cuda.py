import re
import subprocess as sp


def get_cuda_version():
    stdout = sp.check_output(["nvcc", "--version"])
    # print(stdout.decode("utf-8"))
    cuda_version = re.search(r".*release (\d+\.\d+).*", stdout.decode("utf-8"))
    if cuda_version is not None:
        return cuda_version.group(1)
    return None
