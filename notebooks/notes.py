def build_hw_df(kernel_csv_files, cycle_csv_files):
    # pprint(kernel_csv_files)
    # pprint(cycle_csv_files)
    kernel_df = build_hw_kernel_df(kernel_csv_files)
    # print(kernel_df.index)
    # kernel_df = kernel_df.set_index(["Stream", "Context", "Device", "Kernel"])
    print("kernels shape", kernel_df.shape)
    # return kernel_df

    # print(kernel_df.T)
    cycle_df = build_hw_cycles_df(cycle_csv_files)
    # print(kernel_df.index)
    # return cycle_df
    # cycle_df = cycle_df.set_index(["Stream", "Context", "Device", "Kernel"])
    # cycle_df = cycle_df.drop_index()
    print("cycles shape", cycle_df.shape)
    # print(cycle_df.T)
    
    # same number of repetitions
    assert kernel_df.shape[0] == cycle_df.shape[0]
    
    # join them
    # common = list(set(kernel_df.columns.to_list()).intersection(set(cycle_df.columns.to_list())))
    # pprint(common)
    # print(kernel_df[common])
    # print(cycle_df[common])
    
    # assert kernel_df[common] == cycle_df[common]
    
    # cycle_df = cycle_df.set_index(common)
    # kernel_df = kernel_df.set_index(common)
    # inner_hw_df = pd.merge(kernel_df, cycle_df, how="inner", left_index=True, right_index=True)
    # inner_hw_df = pd.merge(kernel_df, cycle_df, how="inner")
    inner_hw_df = kernel_df.join(cycle_df, how="inner")
    # sort columns
    inner_hw_df = inner_hw_df.sort_index(axis=1)
    # , on=["Stream", "Context", "Device", "Kernel"])
    # inner_hw_df = pd.join(kernel_df, cycle_df, how="inner", on=["Stream", "Context", "Device", "Kernel"])
    # inner_hw_df = pd.concat([kernel_df, cycle_df])
    # , axis=1, join="outer")
    # , on=["Stream", "Context", "Device", "Kernel"])
    print("inner join shape", inner_hw_df.shape)
    
    # assert no nan values
    assert inner_hw_df.isna().any().sum() == 0
    return inner_hw_df

#for c in ["Start", "Duration"]:
    #    hw_kernel_df[c + "_max"] = grouped.max()[c]
    #    hw_kernel_df[c + "_min"] = grouped.min()[c]
    #    hw_kernel_df[c + "_stddev"] = grouped.std()[c]
    
    
# => per config, benchmark and input, plot bars for each simulator

metrics = [
    gpusims.plot.metrics.ExecutionTime,
]

for (config_name, config), (bench_name, bench) in itertools.product(
    selected_configs.items(),
    selected_benchmarks.items()
):
    print(config_name, bench_name)
    for inp in bench.inputs:
        plot_data = PlotData(benchmark=bench, config=config, inp=inp)
        for (sim_name, sim) in selected_simulators.items():
            if not bench.enabled(sim_name):
                continue
            # print(sim_name, config_name, bench_name)
            bench_config = sim(
                run_dir=run_dir / sim_name.lower(),
                benchmark=bench,
                config=config,
            )
            if not bench_config.input_path(inp).is_dir():
                print(f"WARN: {bench_config.input_path(inp)} does not exist")
                continue
            
            plot_data[sim_name] = bench_config.load_dataframe(inp)
        
        metric = metric_cls(plot_data)
        # for metric_name, metric in metrics.items():
            # print("######", metric_name)
        metric_df = metric.compute()
        metric_df["Benchmark"] = bench.name
        fig = plot_bars_exec_time(
            metric_cls=metric_cls,
            data=metric_df,
            config=config,
            title=f"{metric_cls.name} for {bench.name} {inp.args} ({config.name})",
        )
        # fig.show()
        filename = ["bar", metric_cls.name, bench.name, config.key, inp.sanitized_name()]
        filename = Path("./figs") / gpusims.utils.slugify("_".join(filename))
        filename = filename.with_suffix(".pdf")
        fig.write_image(filename, format='pdf')
        print("wrote", filename)
        # break
    # break

metric_df

all_bench_configs = []

for (sim_name, sim), (config_name, config), (bench_name, bench) in itertools.product(
    selected_simulators.items(),
    selected_configs.items(),
    selected_benchmarks.items()
):
    if not bench.enabled(sim):
        continue
    
    # for inp in bench:
    #all_plot_configs.append(BenchmarkPlot(bench_config=sim(
    #    run_dir=run_dir / sim_name.lower(),
    #    benchmark=bench,
    #    config=config,
    #)))

print(f"{len(all_bench_configs)} total benchmark configs")


for plot_config in all_bench_configs:
    for inp in plot_config.bench_config.benchmark.inputs:
        hw_df = plot.load_hardware_df(inp)
        hw_df = plot.load_hardware_df(inp)
        accel_sass_df = pd.read_csv(accel_sass_results / "results/stats.csv")
print("accel sass shape", accel_sass_df.shape)
accel_sass_df = accel_sass_df.pivot(index=["kernel", "kernel_id"], columns=["stat"])["value"]
print("accel sass shape", accel_sass_df.shape)
# pprint(accel_df.columns.tolist())
accel_sass_df.T
        break
    break
hw_df.T
# plot = all_bench_configs[0]

plot = all_bench_configs[0]
hw_df = plot.load_hardware_df()

hw_df = build_hw_df(
    cycle_csv_files=list((native_results / "results").rglob(r"result.cycles.csv.*")),
    kernel_csv_files=list((native_results / "results").rglob(r"result.csv.*")),
)
hw_df.T


# print the latex table
sim_line = [""]
conf_line = [""]

plot_sims = [s for s in selected_simulators if s != gpusims.NATIVE]
table_benchmarks = [
    ("babelstream", "BabelStream"),
    ("vectoradd", "VectorAdd"),
    ("cuda4-matrixmul", "MatrixMul"),
    ("cuda6-transpose", "Transpose"),
]

for bench_key, bench_name in table_benchmarks:
    # bench = benchmarks[bench_name]
    # bench
    # for si, sim in enumerate(plot_sims):
    # sim_line.append("\multicolumn{2}{c|}{%s}" % SIM_NAME_TEX[sim])
    sim_line.append("\multicolumn{2}{c|}{%s}" % bench_name)
    conf_line += [r"{\centering %s \par}" % configs[c].name.replace(" ", r"\newline ") for c in plot_configs]

print(" & ".join(sim_line) + r" \\")
print("%")
print(" & ".join(conf_line) + r" \\ \hline")
print("%")
# for metric_key, metric_name in [("corr", "Corr."), ("err", "Rel. Err"), ("nrmse", "NRMSE")]:
for si, sim in enumerate(plot_sims):
    line = [SIM_NAME_TEX[sim]]
    for bench_key, bench_name in table_benchmarks:
        bench = benchmarks[bench_key]
        for ci, conf in enumerate(plot_configs):
            # print(conf, sim, bench_key)
            matches = [
                e for e in metric_table_data
                if e["config"] == conf and e["sim"] == sim and e["bench"] == bench.name
            ]
            value = ""
            # assert len(matches) == 1
            if len(matches) == 1 and bench.enabled(sim):
                match = matches[0]
                # \\%
                value = "%.1f" % (match["min_rel_err"]*100)
                value += r"-\newline "
                value += "%.1f" % (match["max_rel_err"]*100)
                value += r"\%"
            line.append(value)
    # pprint(line)
    print(" & ".join(line) + r" \\")
    print("%")
# pprint(metric_table_data)


# metric_table_data
sim_line = [""]
conf_line = [""]
for si, sim in enumerate([s for s in selected_simulators_copy if s != gpusims.NATIVE]):
    sim_line.append("\multicolumn{2}{c|}{%s}" % SIM_NAME_TEX[sim])
    conf_line += [r"{\centering %s \par}" % configs[c].name for c in plot_configs]
print(" & ".join(sim_line) + r" \\")
print("%")
print(" & ".join(conf_line) + r" \\ \hline")
print("%")
for metric_key, metric_name in [("corr", "Corr."), ("err", "Rel. Err"), ("nrmse", "NRMSE")]:
    line = [metric_name]
    for si, sim in enumerate([s for s in selected_simulators_copy if s != gpusims.NATIVE]):
        # if sim == gpusims.NATIVE:
        #     continue
        for ci, conf in enumerate(plot_configs):
            matches = [e for e in table_data if e["config"] == conf and e["sim"] == sim]
            value = ""
            if len(matches) == 1:
                value = matches[0][metric_key]
            line.append(value)
    print(" & ".join(line) + r" \\")
    print("%")
    
# amd: #ee1f23
# nvidia: #76b900