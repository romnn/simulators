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
    