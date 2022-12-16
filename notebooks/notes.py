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