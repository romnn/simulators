IS_WINDOWS = sys.platform == "win32"


def _find_cuda_home():
    """Finds the CUDA install path."""
    # Guess #1
    cuda_home = os.environ.get("CUDA_HOME") or os.environ.get("CUDA_PATH")
    if cuda_home is not None:
        return cuda_home

    # Guess #2
    which = "where" if IS_WINDOWS else "which"
    with open(os.devnull, "w") as devnull:
        try:
            nvcc = (
                sp.check_output([which, "nvcc"], stderr=devnull).decode().rstrip("\r\n")
            )
            cuda_home = op.dirname(op.dirname(nvcc))
            if cuda_home is not None:
                return cuda_home
        except sp.CalledProcessError:
            pass

    # Guess #3
    if IS_WINDOWS:
        cuda_homes = glob.glob(
            "C:/Program Files/NVIDIA GPU Computing Toolkit/CUDA/v*.*"
        )
        if len(cuda_homes) > 0 and op.exists(cuda_homes[0]):
            return cuda_homes[0]
    else:
        for common_install_path in ["/usr/local/cuda", "/usr/lib/cuda"]:
            if op.exists(common_install_path):
                return common_install_path
    return cuda_home


def _check_for_cuda():
    """Check whether the host system can compile CUDA kernels"""
    libs, includes = [], []
    CUDA_HOME = _find_cuda_home()
    if not CUDA_HOME:
        return False, None, libs, includes

    CUDNN_HOME = os.environ.get("CUDNN_HOME") or os.environ.get("CUDNN_PATH")

    if IS_WINDOWS:
        lib_dir = "lib/x64"
    else:
        lib_dir = "lib64"
    if not op.exists(op.join(CUDA_HOME, lib_dir)) and op.exists(
        op.join(CUDA_HOME, "lib")
    ):
        lib_dir = "lib"

    libs.append(op.join(CUDA_HOME, lib_dir))
    if CUDNN_HOME is not None:
        libs.append(op.join(CUDNN_HOME, lib_dir))

    cuda_home_include = op.join(CUDA_HOME, "include")
    # if we have the Debian/Ubuntu packages for cuda, we get /usr as cuda home.
    # but gcc doesn't like having /usr/include passed explicitly
    if cuda_home_include != "/usr/include":
        includes.append(cuda_home_include)
    if CUDNN_HOME is not None:
        includes.append(os.path.join(CUDNN_HOME, "include"))

    return True, op.join(CUDA_HOME, "bin", "nvcc"), libs, includes


can_compile_cuda, cuda_nvcc, cuda_libs, cuda_includes = _check_for_cuda()

CUDA_INSTALL_PATH = "/usr/lib/x86_64-linux-gnu/"
CUDA_INSTALL_PATH = "/usr/lib/cuda"
