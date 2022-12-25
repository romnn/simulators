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

SIM_NAME = {
    gpusims.TEJAS: "GPUTejas",
    gpusims.ACCELSIM_PTX: "AccelSim PTX",
    gpusims.ACCELSIM_SASS: "AccelSim SASS",
    gpusims.NATIVE: "Hardware",
    gpusims.MULTI2SIM: "Multi2Sim",
    gpusims.MACSIM: "MacSim",
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
        l=MARGIN, r=MARGIN, t=1.5*MARGIN, b=MARGIN
    ),
    width=900,
    height=500,
)

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