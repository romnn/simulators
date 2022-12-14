class PlotData:
    def __init__(
        self,
        benchmark,
        config,
        inp,
        hw_df=None,
        m2s_df=None,
        macsim_df=None,
        tejas_df=None,
        accel_ptx_df=None,
        accel_sass_df=None,
    ):
        self.benchmark = benchmark
        self.config = config
        self.inp = inp
        self.hw_df = hw_df
        self.m2s_df = m2s_df
        self.macsim_df = macsim_df
        self.tejas_df = tejas_df
        self.accel_ptx_df = accel_ptx_df
        self.accel_sass_df = accel_sass_df

    def get(self, key):
        try:
            return getattr(self, key)
        except AttributeError:
            return None

    def __getitem__(self, key):
        return getattr(self, key)

    def __setitem__(self, key, value):
        setattr(self, key, value)
