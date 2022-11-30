from invoke import task, Collection

gpgpusim = Collection("gpgpusim")
accelsim = Collection("accelsim")


@task
def build_accelsim(c, deps=False):
    if deps:
        c.run(
            " ".join(
                [
                    "sudo",
                    "apt-get",
                    "install",
                    "-y",
                    "wget",
                    "git",
                    "g++",
                    "libssl-dev",
                    "libxml2-dev",
                    "libboost-all-dev",
                    "build-essential",
                    "xutils-dev",
                    "bison",
                    "zlib1g-dev",
                    "flex",
                    "libglu1-mesa-dev",
                    "libxi-dev",
                    "libxmu-dev",
                    # "libglut3-dev",
                ]
            ),
            pty=True,
        )
    c.run(
        " && ".join(
            [
                f"export CUDA_INSTALL_PATH={CUDA_INSTALL_PATH}",
                "cd accel-sim",
                "source ./gpu-simulator/setup_environment.sh",
                "make -j -C ./gpu-simulator/",
            ]
        ),
        pty=True,
    )


@task
def build_gpgpusim(c, deps=False):
    if deps:
        c.run(
            " ".join(
                [
                    "sudo",
                    "apt-get",
                    "install",
                    "-y",
                    "build-essential",
                    "xutils-dev",
                    "bison",
                    "zlib1g-dev",
                    "flex",
                    "libglu1-mesa-dev",
                    "libxi-dev",
                    "libxmu-dev",
                    # "libglut3-dev",
                ]
            ),
            pty=True,
        )
    c.run(
        " && ".join(
            [
                f"export CUDA_INSTALL_PATH={CUDA_INSTALL_PATH}",
                "cd gpgpu-sim",
                "source setup_environment",
                "make -j",
            ]
        ),
        pty=True,
    )


accelsim.add_task(build_accelsim, "build")
gpgpusim.add_task(build_gpgpusim, "build")


def _prep_gpgpusim(root):
    CUDA_VERSION_STRING = sp.check_output(
        [Path(CUDA_INSTALL_PATH) / "bin/nvcc", "--version"]
    )
    CUDA_VERSION_STRING = re.findall(
        r"release (\d+.\d+),", CUDA_VERSION_STRING.decode("utf-8")
    )
    major, minor = tuple(CUDA_VERSION_STRING[0].split("."))
    CUDA_VERSION_NUMBER = "{:02d}{:02d}".format(10 * int(major), 10 * int(minor))

    CC_VERSION = sp.check_output(["gcc", "--version"])
    CC_VERSION = re.findall(r"\s+([0-9]\.[0-9]\.[0-9])\s+", CC_VERSION.decode("utf-8"))

    GPGPUSIM_CONFIG = f"gcc-{CC_VERSION[0]}/cuda-{CUDA_VERSION_NUMBER}/release"

    GPGPUSIM_LIB_PATH = root / "lib" / GPGPUSIM_CONFIG
    if sys.platform == "darwin":
        add_paths_to_env("DYLD_LIBRARY_PATH", [GPGPUSIM_LIB_PATH])
    else:
        add_paths_to_env("LD_LIBRARY_PATH", [GPGPUSIM_LIB_PATH])


@task
def prep_accelsim(c, deps=False):
    ACCELSIM_ROOT = ROOT_DIR / "accelsim" / "gpu-simulator"
    _prep_gpgpusim(ACCELSIM_ROOT)


@task
def prep_gpgpusim(c, deps=False):
    GPGPUSIM_ROOT = ROOT_DIR / "gpgpu-sim"
    _prep_gpgpusim(GPGPUSIM_ROOT)


accelsim.add_task(prep_accelsim, "prepare")
gpgpusim.add_task(prep_gpgpusim, "prepare")
