import invoke
from invoke import task
from pathlib import Path

ROOT_DIR = Path(__file__).parent.parent
TEJAS_DIR = ROOT_DIR / "gputejas"


def is_64bit() -> bool:
    # https://docs.python.org/3/library/sys.html#sys.maxsize
    import sys

    return sys.maxsize > 2**32


# docker build -t tejas --load -f gpusims/tejas/Dockerfile .
# docker run -it -v "$PWD/tasks.py:/app/tasks.py:ro" -v "$PWD/gpusims:/app/gpusims:ro" tejas


@task
def clean(c):
    print("cleaning...")
    c.run("rm *.txt *.o tmp tracegen")


def get_threads_from_config(config_file) -> int:
    # gputejas/gputejas/src/simulator/config/config.xml
    # threadNum=`grep -o '<MaxNumJavaThreads>.*</MaxNumJavaThreads>' $configPath | cut -d'<' -f 2 | cut -d'>' -f 2`
    import xml.etree.ElementTree as ET

    tree = ET.parse(config_file)
    root = tree.getroot()
    try:
        matches = list(root.findall("./Simulation/MaxNumJavaThreads"))
        return int(matches[0].text)
    except Exception as e:
        print("failed to get max threads")
        raise e


@task(iterable=["arg"])
def traces(c, config, benchmark, output, arg):
    # should make a temp dir?
    print("generating traces...")
    # clean(c)

    print(arg)
    config = Path(config).absolute()
    assert config.is_file()

    benchmark = Path(benchmark).absolute()
    assert benchmark.is_dir()

    output = Path(output)
    output.mkdir(parents=True, exist_ok=True)

    threads = get_threads_from_config(config)
    trace_dir = output / str(threads)
    c.run(f"rm -rf {trace_dir}")

    import tempfile

    with tempfile.TemporaryDirectory() as tmp:
        tmp = Path(tmp)
        # c.run(f"cd {TEJAS_DIR} && g++-4.8 -std=c++0x Tracegen.cpp -c -I .")

        sos = TEJAS_DIR / "so_files_64bit"
        _includes = [
            TEJAS_DIR,
            sos,
            "/usr/local/cuda/lib64",
            "/usr/lib/x86_64-linux-gnu/",
        ]

        includes = " ".join([f"-I{i}" for i in _includes])
        libs = " ".join([f"-L{i}" for i in _includes])

        # compiling tracegen.cpp
        print(includes)
        c.run(
            f"cd {tmp} "
            + f"&& g++ -std=c++0x {includes} -c {TEJAS_DIR / 'Tracegen.cpp'}"
        )

        # inv tejas.traces --benchmark ./samples/vectoradd_tejas/ --config ./gputejas/gputejas/src/simulator/config/config.xml --output ./output
        # how the ocelot dockerfile looks like:
        # https://github.com/gthparch/gpuocelot/blob/master/docker/Dockerfile

        # set to 4.6
        # update-alternatives --set gcc "/usr/bin/gcc-4.6
        # update-alternatives --set g++ "/usr/bin/g++-4.6

        # generating tracegen executable
        # make copy of vectoradd that generates *.o files
        # print(f"g++ {benchmark}/*.o Tracegen.o {libs} -locelot -ltinfo -o tracegen")
        print(libs)
        # f"env VTK_USE_X=off && cd {tmp} "
        c.run(
            f"cd {tmp} "
            + f"&& g++ -o tracegen {benchmark}/*.o Tracegen.o "
            + f"{libs} -Wl,--whole-archive -locelot -Wl,--no-whole-archive "
            + f"-ltinfo -lcuda -lcudart -Wl,-rpath-link={sos}"
        )
        # g++-4.4 -o tracegen $bench_path/*.o Tracegen.o -locelot -ltinfo

        # generating traces
        # vectoradd does not even have a main method, so ocelot is doing the calling here
        # it should? also do emulation, yet the results are wrong,
        # because nothing is being computed
        # i guess at this point it might really be due to g++4.6 being required for linking
        # or we could also try whole archive?
        c.run(f"cd {TEJAS_DIR} " + f"&& {tmp / 'tracegen'} {' '.join(arg)} {trace_dir}")
        # c.run(f"cd {TEJAS_DIR} " + f"&& ./tracegen {' '.join(args)} {threads}")
        return

        # check number of kernels
        with open(TEJAS_DIR / "0.txt", "r") as f:
            kernels = len([l for l in f.readlines() if "KERNEL START" in l])
        # kernels=`grep "KERNEL START" 0.txt | wc -l`
        print(kernels)

        # create a new folder and moving the text files
        (TEJAS_DIR / str(threads)).mkdir(parent=True, exist_ok=True)

        c.run(f"cd {TEJAS_DIR} && mv *.txt {str(threads)}")

        # simplify traces
        simplifier = TEJAS_DIR / "gputejas/Tracesimplifier.jar"
        c.run(f"java -jar {simplifier} {config.absolute()} tmp . {kernels}")

        print("Generated the traces, please run the benchmark using:\n\ninv tejas.run")


@task
def run(c, config, output):
    config = Path(config)
    assert config.is_file()

    output = Path(output)
    benchmark.mkdir(parents=True, exist_ok=True)

    # output.txt
    get_threads_from_config(config)
    trace_dir = TEJAS_DIR / str(threads)

    import glob.glob

    kernels = int(list(glob(trace_dir / "hashfile_*")))
    print("kernels:", kernels)

    gputejas = TEJAS_DIR / "gputejas/jars/GPUTejas.jar"
    c.run(f"java -jar {gputejas} {config.absolute()} {output.absolute()} . {kernels}")


@task
def setup_unsafe(c, deps=False):
    # make sure we are running 64 bit
    assert is_64bit()
    c.run("sudo apt-get remove nvidia-cuda-toolkit")

    gcc = Path("/usr/bin/gcc")
    if gcc.is_file():
        c.run(f"sudo rm {gcc}")
        c.run("sudo ln -s gcc-4.4 gcc")

    toolkit = Path("cuda-toolkit")
    if toolkit.is_dir():
        c.run(f"sudo cp -r {toolkit / 'bin'} /usr/bin/")
        c.run(f"sudo cp -r {toolkit / 'include/crt'} /usr/include/")
        c.run(f"sudo cp -r {toolkit / 'include/*.h'} /usr/include/")
        c.run(f"sudo cp -r {toolkit} /usr/lib/")

    c.run("sudo apt-get install -y ant freeglut3-dev libxi-dev libxmu-dev")

    try:
        c.run("sudo apt-get install -y g++-4.4")
    except invoke.exceptions.UnexpectedExit as e:
        c.run("sudo apt-get install -y software-properties-common")

        # wget http://ftp.gnu.org/gnu/glibc/glibc-2.19.tar.gz
        # tar -xf glibc-2.19.tar.gz
        # mkdir glibcbuild && cd glibcbuild
        # ../glibc-2.19/configure --prefix=/opt/glibc2.19

        # git clone git://gcc.gnu.org/git/gcc.git
        # or download from https://gcc.gnu.org/releases.html
        # ftp://ftp.fu-berlin.de/unix/languages/gcc/ (not found)
        # https://mirror.linux-ia64.org/gnu/gcc/releases
        # https://ftp.nluug.nl/languages/gcc/releases/

        # the Git tag for GCC X.Y.Z is of the form releases/gcc-X.Y.Z
        # list all branches: git branch -a
        # list all tags: git tag -l
        # e.g. releases/gcc-4.9 (a branch)
        # git checkout gcc-4.8

        # sudo apt-get install -y wget libgmp-dev libmpfr-dev libmpc-dev libc6-dev
        # sudo apt-get install -y gcc-multilib g++-multilib
        # wget http://www.netgull.com/gcc/releases/gcc-4.6.0/gcc-4.6.0.tar.bz2
        # tar -xvjf gcc-4.6.0.tar.bz2
        # mkdir gccbuild && cd gccbuild
        # ../gcc-4.6.0/configure --prefix=/usr/bin --disable-multilib
        # make -j

    # print("error", e)
    # print("output: ", test)
    # try again now
    # c.run("sudo apt-get install -y g++-4.4")

    def remove(f):
        print("removing", f)
        c.run(f"rm -f {f}")

    def cp(s, d):
        print("copy", s, "to", d)
        c.run(f"cp -r {s} {d}")

    src = TEJAS_DIR / "so_files_64bit"
    dest = Path("/usr/lib/x86_64-linux-gnu/")

    cp(src / "libocelot.so", "/usr/lib/libocelot.so")
    # remove("/lib/x86_64-linux-gnu/libtinfo.so*")

    # some libtinfo.so.5 gets linked somewhere so we need to use that suffix.
    cp(src / "libtinfo.so", "/lib/x86_64-linux-gnu/libtinfo.so.5")
    cp(src / "libboost_thread.so.1.54.0", dest / "libboost_thread.so.1.54.0")
    cp(src / "libboost_system.so.1.54.0", dest / "libboost_system.so.1.54.0")
    # /lib/x86_64-linux-gnu ???
    cp(src / "libz.so.1.2.8", dest / "libz.so")
    remove("/usr/lib/x86_64-linux-gnu/libGLEW.so.1.10")
    cp(src / "libGLEW.so.1.10", dest / "libGLEW.so.1.10")

    build(c)


@task
def build(c):
    print("building...")
    c.run(f"cd {TEJAS_DIR / 'gputejas'} && ant clean && ant && ant make-jar")
