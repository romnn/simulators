import pandas as pd

def hw_stats(csv_files):
    # read hw stats for vectoradd
    # parsed_dir = root_path / "analyze/parsed/"
    # hw_cycle_csvs = list(parsed_dir.glob("bfs-rodinia-1080ti.csv.cycle*"))
    # pprint(hw_cycle_csvs)

    hw_cycle_df = pd.read_csv(str(csv_files))
    # hw_cycle_df = pd.concat([pd.read_csv(csv) for csv in csv_files], ignore_index=False)
    # remove the units
    hw_cycle_df = hw_cycle_df[~hw_cycle_df["Correlation_ID"].isnull()]
    # remove memcopies
    hw_cycle_df = hw_cycle_df[~hw_cycle_df["Name"].str.contains(r"\[CUDA memcpy .*\]")]
    # name refers to kernels now
    hw_cycle_df = hw_cycle_df.rename(columns={"Name": "Kernel"})
    # remove columns that are only relevant for memcopies
    # df = df.loc[:,df.notna().any(axis=0)]
    hw_cycle_df = hw_cycle_df.drop(columns=["Size", "Throughput", "SrcMemType", "DstMemType"])
    # set the correct dtypes
    hw_cycle_df = hw_cycle_df.astype({
        "Start": "float64",
        "Duration": "float64",
        "Static SMem": "float64",
        "Dynamic SMem": "float64",
        "Device": "string",
        "Kernel": "string",
    })
    print(hw_cycle_df.dtypes)
    print(hw_cycle_df.shape)
    return hw_cycle_df