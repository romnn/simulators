import load
import gpusims


class PlotNativeBenchmarkConfig(gpusims.native.NativeBenchmarkConfig):
    def load_dataframe(self, inp):
        results_dir = self.input_path(inp) / "results"
        assert results_dir.is_dir(), f"{results_dir} is a dir"
        return load.build_hw_df(
            cycle_csv_files=list(results_dir.rglob(r"result.cycles.csv.*")),
            kernel_csv_files=list(results_dir.rglob(r"result.csv.*")),
        )