import re
from collections import namedtuple
import subprocess as sp
import ctypes

# check: https://gist.github.com/f0k/63a664160d016a491b2cbea15913d549

# from cuda.h
CUDA_SUCCESS = 0
CU_DEVICE_ATTRIBUTE_MULTIPROCESSOR_COUNT = 16
CU_DEVICE_ATTRIBUTE_MAX_THREADS_PER_MULTIPROCESSOR = 39
CU_DEVICE_ATTRIBUTE_CLOCK_RATE = 13
CU_DEVICE_ATTRIBUTE_MEMORY_CLOCK_RATE = 36

CUDADevice = namedtuple(
    "CUDADevice",
    [
        "name",
        "threads_per_core",
        "cores",
        "clockrate_mhz",
        "mem_clockrate_mhz",
        "major",
        "minor",
    ],
)


def load_cuda_driver():
    # this requires the CUDA driver API, so we actually need a device
    libnames = ("libcuda.so", "libcuda.dylib", "cuda.dll")
    for libname in libnames:
        try:
            cuda = ctypes.CDLL(libname)
            return cuda
        except OSError:
            continue
        else:
            break
    else:
        raise OSError("could not load any of: " + " ".join(libnames))


class CUDAError(OSError):
    def __init__(self, cuda, code, cmd, *args):
        error_str = ctypes.c_char_p()
        cuda.cuGetErrorString(code, ctypes.byref(error_str))
        super().__init__(
            "{} failed with error code {}: {}".format(
                [cmd] + args, code, error_str.value.decode()
            )
        )


def check_cuda(cuda, code, cmd, *args):
    if code != CUDA_SUCCESS:
        raise CUDAError(cuda=cuda, code=code, cmd=cmd, *args)


def get_devices():
    cuda = load_cuda_driver()
    check_cuda(cuda, cuda.cuInit(0), "cuInit", 0)

    name_buffer = b" " * 100
    gpu_count = ctypes.c_int()
    device = ctypes.c_int()
    cc_major = ctypes.c_int()
    cc_minor = ctypes.c_int()
    cores = ctypes.c_int()
    threads_per_core = ctypes.c_int()
    clockrate = ctypes.c_int()
    mem_clockrate = ctypes.c_int()

    devices = []
    check_cuda(cuda, cuda.cuDeviceGetCount(ctypes.byref(gpu_count)), "cuDeviceGetCount")

    for i in range(gpu_count.value):
        check_cuda(cuda, cuda.cuDeviceGet(ctypes.byref(device), i), "cuDeviceGet")
        check_cuda(
            cuda,
            cuda.cuDeviceGetName(
                ctypes.c_char_p(name_buffer), len(name_buffer), device
            ),
            "cuDeviceGetName",
        )
        device_name = name_buffer.split(b"\0", 1)[0].decode()

        check_cuda(
            cuda,
            cuda.cuDeviceComputeCapability(
                ctypes.byref(cc_major), ctypes.byref(cc_minor), device
            ),
            "cuDeviceComputeCapability",
            device,
        )

        check_cuda(
            cuda,
            cuda.cuDeviceGetAttribute(
                ctypes.byref(cores), CU_DEVICE_ATTRIBUTE_MULTIPROCESSOR_COUNT, device
            ),
            "cuDeviceGetAttribute",
            "CU_DEVICE_ATTRIBUTE_MULTIPROCESSOR_COUNT",
            device,
        )

        check_cuda(
            cuda,
            cuda.cuDeviceGetAttribute(
                ctypes.byref(threads_per_core),
                CU_DEVICE_ATTRIBUTE_MAX_THREADS_PER_MULTIPROCESSOR,
                device,
            ),
            "cuDeviceGetAttribute",
            "CU_DEVICE_ATTRIBUTE_MAX_THREADS_PER_MULTIPROCESSOR",
            device,
        )

        check_cuda(
            cuda,
            cuda.cuDeviceGetAttribute(
                ctypes.byref(clockrate), CU_DEVICE_ATTRIBUTE_CLOCK_RATE, device
            ),
            "cuDeviceGetAttribute",
            "CU_DEVICE_ATTRIBUTE_CLOCK_RATE",
            device,
        )

        check_cuda(
            cuda,
            cuda.cuDeviceGetAttribute(
                ctypes.byref(mem_clockrate),
                CU_DEVICE_ATTRIBUTE_MEMORY_CLOCK_RATE,
                device,
            ),
            "cuDeviceGetAttribute",
            "CU_DEVICE_ATTRIBUTE_MEMORY_CLOCK_RATE",
            device,
        )

        devices.append(
            CUDADevice(
                name=device_name,
                threads_per_core=threads_per_core.value,
                cores=cores.value,
                clockrate_mhz=clockrate.value / 1000.0,
                mem_clockrate_mhz=mem_clockrate.value / 1000.0,
                major=cc_major.value,
                minor=cc_minor.value,
            )
        )
    return devices


def get_cuda_version():
    stdout = sp.check_output(["nvcc", "--version"])
    # print(stdout.decode("utf-8"))
    cuda_version = re.search(r".*release (\d+\.\d+).*", stdout.decode("utf-8"))
    if cuda_version is not None:
        return cuda_version.group(1)
    return None
