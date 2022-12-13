import pandas as pd

hw_index_cols = ["Stream", "Context", "Device", "Kernel", "Correlation_ID"]

def build_hw_kernel_df(csv_files):
    hw_kernel_df = pd.concat([pd.read_csv(csv) for csv in csv_files], ignore_index=False)
    # print(hw_kernel_df)
    # remove the units
    hw_kernel_df = hw_kernel_df[~hw_kernel_df["Correlation_ID"].isnull()]
    # remove memcopies
    hw_kernel_df = hw_kernel_df[~hw_kernel_df["Name"].str.contains(r"\[CUDA memcpy .*\]")]
    # name refers to kernels now
    hw_kernel_df = hw_kernel_df.rename(columns={"Name": "Kernel"})
    # remove columns that are only relevant for memcopies
    # df = df.loc[:,df.notna().any(axis=0)]
    hw_kernel_df = hw_kernel_df.drop(columns=["Size", "Throughput", "SrcMemType", "DstMemType"])
    # set the correct dtypes
    hw_kernel_df = hw_kernel_df.astype({
        "Start": "float64",
        "Duration": "float64",
        "Static SMem": "float64",
        "Dynamic SMem": "float64",
        "Device": "string",
        "Kernel": "string",
    })
    hw_kernel_df = hw_kernel_df.set_index(hw_index_cols)
    
    # compute min, max, mean, stddev
    grouped = hw_kernel_df.groupby(level=hw_index_cols)
    hw_kernel_df = grouped.mean()
    hw_kernel_df_max = grouped.max()
    hw_kernel_df_max = hw_kernel_df_max.rename(columns={c: c + "_max" for c in hw_kernel_df_max.columns})
    hw_kernel_df_min = grouped.min()
    hw_kernel_df_min = hw_kernel_df_min.rename(columns={c: c + "_min" for c in hw_kernel_df_min.columns})
    hw_kernel_df_std = grouped.std()
    hw_kernel_df_std = hw_kernel_df_std.rename(columns={c: c + "_std" for c in hw_kernel_df_std.columns})
    return hw_kernel_df


def build_hw_cycles_df(csv_files):
    hw_cycle_df = pd.concat([pd.read_csv(csv) for csv in csv_files], ignore_index=False)
    # print(hw_cycle_df)
    # remove the units
    hw_cycle_df = hw_cycle_df[~hw_cycle_df["Correlation_ID"].isnull()]
    # remove textual utilization metrics (high, low, ...)
    hw_cycle_df = hw_cycle_df[hw_cycle_df.columns[~hw_cycle_df.columns.str.contains(pat='.*_utilization')]]
    hw_cycle_df = hw_cycle_df.set_index(hw_index_cols)
    # convert to numeric values
    hw_cycle_df = hw_cycle_df.convert_dtypes()
    hw_cycle_df = hw_cycle_df.apply(pd.to_numeric)
    
    # compute min, max, mean, stddev
    grouped = hw_cycle_df.groupby(level=hw_index_cols)
    hw_cycle_df = grouped.mean()
    hw_cycle_df_max = grouped.max()
    hw_cycle_df_max = hw_cycle_df_max.rename(columns={c: c + "_max" for c in hw_cycle_df_max.columns})
    hw_cycle_df_min = grouped.min()
    hw_cycle_df_min = hw_cycle_df_min.rename(columns={c: c + "_min" for c in hw_cycle_df_min.columns})
    hw_cycle_df_std = grouped.std()
    hw_cycle_df_std = hw_cycle_df_std.rename(columns={c: c + "_std" for c in hw_cycle_df_std.columns})
    
    hw_cycle_df = pd.concat([hw_cycle_df, hw_cycle_df_max, hw_cycle_df_min, hw_cycle_df_std], axis=1)
    return hw_cycle_df


def build_hw_df(kernel_csv_files, cycle_csv_files):
    # pprint(kernel_csv_files)
    # pprint(cycle_csv_files)
    kernel_df = build_hw_kernel_df(kernel_csv_files)
    print("kernels shape", kernel_df.shape)
    cycle_df = build_hw_cycles_df(cycle_csv_files)
    print("cycles shape", cycle_df.shape)
    
    # same number of repetitions
    assert kernel_df.shape[0] == cycle_df.shape[0]
    
    inner_hw_df = kernel_df.join(cycle_df, how="inner")
    # sort columns
    inner_hw_df = inner_hw_df.sort_index(axis=1)
    print("inner join shape", inner_hw_df.shape)
    
    # assert no nan values
    assert inner_hw_df.isna().any().sum() == 0
    return inner_hw_df