import sys
sys.path.append("../")
import gpusims

COLORS = ["#5F34FA", "#49DFE3", "#8CFA5D", "#E3BC49", "#FF7357", "#EE34FA"]
SIM_COLOR = {
    gpusims.TEJAS: "#7E61FA", # colors[0],
    gpusims.ACCELSIM_PTX: COLORS[1],
    gpusims.ACCELSIM_SASS: COLORS[2],
    gpusims.NATIVE: "#FF3C1E", # colors[3],
    gpusims.MULTI2SIM: COLORS[4],
    gpusims.MACSIM: "#FA7AF4", # COLORS[5],
    #"GPUTejas": "#7E61FA", # colors[0],
    #"AccelSim PTX": COLORS[1],
    #"AccelSim SASS": COLORS[2],
    #"Hardware": "#FF3C1E", # colors[3],
    #"Multi2Sim": COLORS[4],
    #"MacSim": COLORS[5],
}

# '', '/', '\\', 'x', '-', '|', '+', '.'
SIM_PATTERN = {
    gpusims.TEJAS: "x",
    gpusims.ACCELSIM_PTX: ".",
    gpusims.ACCELSIM_SASS: "+",
    gpusims.NATIVE: "",
    gpusims.MULTI2SIM: "-",
    gpusims.MACSIM: "/",
}

SIM_NAME = {
    gpusims.TEJAS: "GPUTejas",
    gpusims.ACCELSIM_PTX: "AccelSim PTX",
    gpusims.ACCELSIM_SASS: "AccelSim SASS",
    gpusims.NATIVE: "Hardware",
    gpusims.MULTI2SIM: "Multi2Sim",
    gpusims.MACSIM: "MacSim",
}

SIM_NAME_TEX = {
    gpusims.TEJAS: r"\textsc{GpuTejas}",
    gpusims.ACCELSIM_PTX: r"\textsc{AccelSim PTX}",
    gpusims.ACCELSIM_SASS: r"\textsc{AccelSim SASS}",
    gpusims.NATIVE: r"Hardware",
    gpusims.MULTI2SIM: r"\textsc{Multi2Sim}",
    gpusims.MACSIM: r"\textsc{MacSim}",
}

SIM_ABBR = {
    gpusims.TEJAS: "Tejas",
    gpusims.ACCELSIM_PTX: "Accel PTX",
    gpusims.ACCELSIM_SASS: "Accel SASS",
    gpusims.NATIVE: "HW",
    gpusims.MULTI2SIM: "M2S",
    gpusims.MACSIM: "MS",
    # "GPUTejas": "Tejas",
    #"AccelSim PTX": "Accel PTX",
    #"AccelSim SASS": "Accel SASS",
    #"Hardware": "HW",
    #"Multi2Sim": "M2S",
    #"MacSim": "MS",
}

MARGIN = 50

DEFAULT_LAYOUT_OPTIONS = dict(
    plot_bgcolor="white",
    margin=dict(
        pad=10,
        autoexpand=True,
        l=MARGIN, r=MARGIN, t=MARGIN, b=MARGIN
    ),
    width=900,
    height=500,
)

PDF_OPTS = dict(format='pdf', scale=8)

# define ordering that makes sense (e.g. hw and accel close)
selected_simulators = [
    gpusims.TEJAS, gpusims.MACSIM, gpusims.MULTI2SIM,
    gpusims.ACCELSIM_PTX, gpusims.ACCELSIM_SASS, gpusims.NATIVE]

BENCH_ABBR = {
    "babelstream": "BabelStream",
    "vectoradd": "VectorAdd",
    "cuda4-matrixmul": "MatrixMul",
    "cuda10-matrixmul": "MatrixMul",
    "cuda6-transpose": "Transpose",
    "cuda10-transpose": "Transpose",
}

# define ordering of inputs that makes sense
selected_benchmarks = [
    ("babelstream", [
        ("--arraysize 1024 --numtimes 1", "1024"),
        ("--arraysize 10240 --numtimes 1", "10240"),
        ("--arraysize 102400 --numtimes 1", "102400"),
        # ("--arraysize 1024 --numtimes 2", "1024 (2x)"),
    ]),
    ("vectoradd", [
        # [inp.args for inp in benchmarks["vectoradd"].inputs]),
        ("1000", "1K"),
        ("1000000", "1M"),
    ]),
    ("cuda4-matrixmul", [
        # [inp.args for inp in benchmarks["cuda4-matrixmul"].inputs]),
        ("32", "32x32"),
        ("128", "128x128"),
        ("512", "512x512"),
    ]),
    ("cuda10-matrixmul", [
        ("-wA=32 -hA=32 -wB=32 -hB=32", "32x32 32x32"),
        ("-wA=128 -hA=128 -wB=128 -hB=128", "128x128 128x128"),
        ("-wA=512 -hA=512 -wB=512 -hB=512", "512x512 512x512"),
        # ("-wA=32 -hA=64 -wB=64 -hB=32", "32x64 64x32"),
    ]),
    ("cuda6-transpose", [
        ("-repeat=1 -dimX=32 -dimY=32", "32x32"),
        ("-repeat=1 -dimX=64 -dimY=64", "64x64"),
        ("-repeat=1 -dimX=128 -dimY=128", "128x128"),
        # ("-repeat=3 -dimX=32 -dimY=32", "32x32 (3x)"),
    ]),
    ("cuda10-transpose", [
        ("-repeat=1 -dimX=32 -dimY=32", "32x32"),
        ("-repeat=1 -dimX=64 -dimY=64", "64x64"),
        ("-repeat=1 -dimX=128 -dimY=128", "128x128"),
        # ("-repeat=3 -dimX=32 -dimY=32", "32x32"),
    ]),
]

# define ordering that makes sense
# a4000 is so close to the rtx3070 we exclude it?
selected_configs = ["sm6_gtx1080", "sm86_a4000", "sm86_rtx3070"]
plot_configs = ["sm6_gtx1080", "sm86_rtx3070"]
stat_log = ""

def human_format(num, round_to=2):
    magnitude = 0
    while abs(num) >= 1000:
        magnitude += 1
        num = round(num / 1000.0, round_to)
    return '{:.{}f}{}'.format(num, round_to, ['', 'K', 'M', 'G', 'T', 'P'][magnitude])

def hex_to_rgb(hex_color):
    hex_color = hex_color.lstrip("#")
    if len(hex_color) == 3:
        hex_color = hex_color * 2
    return int(hex_color[0:2], 16), int(hex_color[2:4], 16), int(hex_color[4:6], 16)

def sim_marker(sim):
    import plotly.graph_objects as go
    return go.bar.Marker(
        color=SIM_COLOR[sim],
        pattern= go.bar.marker.Pattern(
            shape=SIM_PATTERN[sim],
            fgcolor="white",
            size=20,
        ),
        line=dict(
            # color='rgba(50, 171, 96, 1.0)',
            width=0,
        ),
    )