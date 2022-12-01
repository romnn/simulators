#![allow(warnings)]

use anyhow::Result;
use clap::Parser;
use indicatif::{HumanBytes, HumanDuration};
use lazy_static::lazy_static;
use gpgpusims::read::BufReadLine;
use regex::Regex;
use std::{
    collections::{HashMap, HashSet},
    fs,
    io::{self, Seek},
    path::{Path, PathBuf},
    time::{Duration, Instant},
};

macro_rules! stat {
    ($name:literal, $kind:expr, $regex:literal) => {
        ($name.to_string(), ($kind, Regex::new($regex).unwrap()))
    };
}

#[derive(Copy, Clone, Debug, PartialEq, Eq, Hash)]
enum StatKind {
    Aggregate,
    Abs,
    Rate,
}

// -R -K -k -B rodinia_2.0-ft -C QV100-PTX
#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Options {
    #[arg(short = 'i', long = "input")]
    input: PathBuf,

    #[arg(short = 'k', long = "per-kernel")]
    per_kernel: bool,

    #[arg(short = 'K', long = "kernel-instance")]
    kernel_instance: bool,

    #[arg(short = 'R', long = "configs-as-rows")]
    configs_as_rows: bool,

    #[arg(short = 'C', long = "config-list")]
    config_list: Option<String>,

    #[arg(short = 'B', long = "benchmark-list")]
    benchmark_list: Option<String>,

    #[arg(short = 'N', long = "sim-name")]
    sim_name: Option<String>,

    #[arg(long = "strict")]
    strict: bool,
}

/// Does a quick 100-line pass to get the GPGPU-Sim Version number.
///
/// Assumes the reader is seeked to the beginning of the file.
fn get_version(mut f: impl reader::BufReadLine + io::Seek) -> Option<String> {
    static MAX_LINES: usize = 100;
    let mut buffer = String::new();
    let mut lines = 0;
    while let Some(Ok(line)) = f.read_line(&mut buffer) {
        if lines >= MAX_LINES {
            break;
        }

        lazy_static! {
            pub static ref GPGPUSIM_BUILD_REGEX: Regex =
                Regex::new(r".*GPGPU-Sim.*\[build\s+(.*)\].*").unwrap();
            pub static ref ACCELSIM_BUILD_REGEX: Regex =
                Regex::new(r".*Accel-Sim.*\[build\s+(.*)\].*").unwrap();
        }
        if let Some(build) = GPGPUSIM_BUILD_REGEX
            .captures(&line)
            .and_then(|c| c.get(1))
            .map(|m| m.as_str().trim().to_string())
        {
            return Some(build);
        }

        if let Some(build) = ACCELSIM_BUILD_REGEX
            .captures(&line)
            .and_then(|c| c.get(1))
            .map(|m| m.as_str().trim().to_string())
        {
            return Some(build);
        }

        lines += 1;
    }
    None
}

/// Performs a quick 10000-line reverse pass,
/// to make sure the simualtion thread finished.
///
/// Assumes the reader is seeked to the end of the file and
/// reading lines in reverse.
fn check_finished(mut f: impl reader::BufReadLine + io::Seek) -> bool {
    static MAX_LINES: usize = 10_000;
    let mut buffer = String::new();
    let mut lines = 0;

    while let Some(Ok(line)) = f.read_line(&mut buffer) {
        if lines >= MAX_LINES {
            break;
        }
        lazy_static! {
            pub static ref EXIT_REGEX: Regex =
                Regex::new(r"GPGPU-Sim: \*\*\* exit detected \*\*\*").unwrap();
        }
        if EXIT_REGEX.captures(&line).is_some() {
            return true;
        }

        lines += 1;
    }
    false
}

fn main() -> Result<()> {
    let start = Instant::now();
    let options = Options::parse();
    println!("options: {:#?}", &options);

    if !options.input.is_file() {
        anyhow::bail!("{} is not a file", options.input.display());
    }
    let mut bytes_parsed = 0;
    let mut files_parsed = 0;

    let finished = {
        let mut file = fs::OpenOptions::new().read(true).open(&options.input)?;
        let mut reader = rev_buf_reader::RevBufReader::new(file);
        check_finished(&mut reader)
    };

    // let version = get_version(&mut reader);

    let aggregate_stats = vec![
        stat!(
            "gpu_total_instructions",
            StatKind::Aggregate,
            r"gpu_tot_sim_insn\s*=\s*(.*)"
        ),
        stat!(
            "gpgpu_simulation_time_sec",
            StatKind::Aggregate,
            r"gpgpu_simulation_time\s*=.*\(([0-9]+) sec\).*"
        ),
        stat!(
            "gpu_tot_sim_cycle",
            StatKind::Aggregate,
            r"gpu_tot_sim_cycle\s*=\s*(.*)"
        ),
        stat!(
            "l2_cache_read_hit",
            StatKind::Aggregate,
            r"\s+L2_cache_stats_breakdown\[GLOBAL_ACC_R\]\[HIT\]\s*=\s*(.*)"
        ),
        stat!(
            "l2_cache_read_total",
            StatKind::Aggregate,
            r"\s+L2_cache_stats_breakdown\[GLOBAL_ACC_R\]\[TOTAL_ACCESS\]\s*=\s*(.*)"
        ),
        stat!(
            "l2_cache_write_hit",
            StatKind::Aggregate,
            r"\s+L2_cache_stats_breakdown\[GLOBAL_ACC_W\]\[HIT\]\s*=\s*(.*)"
        ),
        stat!(
            "l2_cache_write_total",
            StatKind::Aggregate,
            r"\s+L2_cache_stats_breakdown\[GLOBAL_ACC_W\]\[TOTAL_ACCESS\]\s*=\s*(.*)"
        ),
        stat!(
            "total_core_cache_read_total",
            StatKind::Aggregate,
            r"\s+Total_core_cache_stats_breakdown\[GLOBAL_ACC_R\]\[TOTAL_ACCESS\]\s*=\s*(.*)"
        ),
        stat!(
            "total_core_cache_read_hit",
            StatKind::Aggregate,
            r"\s+Total_core_cache_stats_breakdown\[GLOBAL_ACC_R\]\[HIT\]\s*=\s*(.*)"
        ),
        stat!(
            "total_core_cache_write_hit",
            StatKind::Aggregate,
            r"\s+Total_core_cache_stats_breakdown\[GLOBAL_ACC_W\]\[HIT\]\s*=\s*(.*)"
        ),
        stat!(
            "total_core_cache_write_total",
            StatKind::Aggregate,
            r"\s+Total_core_cache_stats_breakdown\[GLOBAL_ACC_W\]\[TOTAL_ACCESS\]\s*=\s*(.*)"
        ),
        stat!(
            "total_core_cache_read_mshr_hit",
            StatKind::Aggregate,
            r"\s+Total_core_cache_stats_breakdown\[GLOBAL_ACC_R\]\[MSHR_HIT\]\s*=\s*(.*)"
        ),
        stat!(
            "gpgpu_n_tot_w_icount",
            StatKind::Aggregate,
            r"gpgpu_n_tot_w_icount\s*=\s*(.*)"
        ),
        stat!(
            "total_dram_reads",
            StatKind::Aggregate,
            r"total dram reads\s*=\s*(.*)"
        ),
        stat!(
            "total_dram_writes",
            StatKind::Aggregate,
            r"total dram writes\s*=\s*(.*)"
        ),
        stat!(
            "kernel_launch_uid",
            StatKind::Aggregate,
            r"kernel_launch_uid\s*=\s*(.*)"
        ),
    ];

    // These stats are reset each kernel and should not be diff'd
    // They cannot be used is only collecting the final_kernel stats
    let abs_stats = vec![
        stat!("gpu_ipc", StatKind::Abs, r"gpu_ipc\s*=\s*(.*)"),
        stat!("gpu_occupancy", StatKind::Abs, r"gpu_occupancy\s*=\s*(.*)%"),
        stat!(
            "l2_bandwidth_gbps",
            StatKind::Abs,
            r"L2_BW\s*=\s*(.*)+GB/Sec"
        ),
    ];

    // These stats are rates that aggregate - but cannot be diff'd
    // Only valid as a snapshot and most useful for the final kernel launch
    let rate_stats = vec![
        stat!(
            "gpgpu_simulation_rate",
            StatKind::Rate,
            r"gpgpu_simulation_rate\s+=\s+(.*)\s+\(inst/sec\)"
        ),
        stat!(
            "gpgpu_simulation_rate",
            StatKind::Rate,
            r"gpgpu_simulation_rate\s+=\s+(.*)\s+\(cycle/sec\)"
        ),
        stat!(
            "gpgpu_silicon_slowdown",
            StatKind::Rate,
            r"gpgpu_silicon_slowdown\s*=\s*(.*)x"
        ),
        stat!("gpu_tot_ipc", StatKind::Rate, r"gpu_tot_ipc\s*=\s*(.*)"),
    ];

    let stats: HashMap<String, (StatKind, Regex)> = HashMap::from_iter(
        aggregate_stats
            .into_iter()
            .chain(abs_stats)
            .chain(rate_stats),
    );
    // println!("stats to collect: {:#?}", &stats);

    if !finished {
        if options.strict {
            anyhow::bail!(
                "{} is invalid: termination message from GPGPU-Sim not found",
                options.input.display()
            );
        } else {
            eprintln!(
                "{} is invalid: termination message from GPGPU-Sim not found",
                options.input.display()
            );
        }
    }

    // let mut all_named_kernels: HashMap<String, usize> = HashMap::new();
    let mut all_named_kernels: HashSet<String> = HashSet::new();
    let prefix = ""; // this would be app_args + config
    files_parsed += 1;

    let mut stat_map: HashMap<String, f64> = HashMap::new();
    let mut stat_found: HashSet<String> = HashSet::new();

    let mut file = fs::OpenOptions::new().read(true).open(&options.input)?;

    if options.per_kernel {
        let mut current_kernel = "".to_string();
        let mut last_kernel = "".to_string();
        let mut raw_last: HashMap<String, f64> = HashMap::new();
        let mut running_kcount = HashMap::new();

        let mut reader = io::BufReader::new(file);
        let mut buffer = String::new();

        while let Some(Ok(line)) = reader.read_line(&mut buffer) {
            // was simulation aborted due to too many instructions?
            // then ignore the last kernel launch, as it is no complete
            // (only appies if we are doing kernel-by-kernel stats)

            lazy_static! {
                pub static ref LAST_KERNEL_BREAK_REGEX: Regex =
                Regex::new(r"GPGPU-Sim: \*\* break due to reaching the maximum cycles \(or instructions\) \*\*").unwrap();
            }
            if LAST_KERNEL_BREAK_REGEX.captures(&line).is_some() {
                eprintln!(
                    "{}: found max instructions - ignoring last kernel",
                    options.input.display()
                );
                // remove
                for stat_name in stats.keys() {
                    stat_map.remove(&format!("{}-{}", current_kernel, stat_name));
                }
            }

            lazy_static! {
                pub static ref KERNEL_NAME_REGEX: Regex =
                    Regex::new(r"kernel_name\s+=\s+(.*)").unwrap();
            }
            if let Some(kernel_name) = KERNEL_NAME_REGEX
                .captures(&line)
                .and_then(|c| c.get(1))
                .map(|m| m.as_str().trim().to_string())
            {
                last_kernel = current_kernel;
                current_kernel = kernel_name;

                if options.kernel_instance {
                    if !running_kcount.contains_key(&current_kernel) {
                        running_kcount.insert(current_kernel.clone(), 0);
                    } else {
                        running_kcount.get_mut(&current_kernel).map(|c| *c += 1);
                    }
                    current_kernel += &format!("--{}", running_kcount[&current_kernel]);
                }

                // println!(
                //     "current kernel is now {} (was {})",
                //     current_kernel, last_kernel
                // );

                all_named_kernels.insert(current_kernel.clone());

                let mut k_count = stat_map
                    .entry(format!("{}-k-count", &current_kernel))
                    .or_insert(0.0);
                *k_count += 1.0;
                continue;
            }

            for (stat_name, (stat_kind, stat_regex)) in &stats {
                if let Some(value) = stat_regex
                    .captures(&line)
                    .and_then(|c| c.get(1))
                    .and_then(|m| m.as_str().trim().parse::<f64>().ok())
                {
                    stat_found.insert(stat_name.clone());
                    let key = format!("{}-{}", current_kernel, stat_name);
                    if stat_kind != &StatKind::Aggregate {
                        stat_map.insert(key.clone(), value);
                    } else if stat_map.contains_key(&key) {
                        // println!("contains key {}", key);
                        let stat_last_kernel = raw_last.get(stat_name).cloned().unwrap_or(0.0);
                        raw_last.insert(stat_name.clone(), value);
                        stat_map
                            .get_mut(&key)
                            .map(|v| *v += value - stat_last_kernel);
                    } else {
                        // println!("does not contain key {}", key);
                        let last_kernel_key = format!("{}-{}", last_kernel, stat_name);
                        let stat_last_kernel = if stat_map.contains_key(&last_kernel_key) {
                            raw_last[stat_name]
                        } else {
                            0.0
                        };
                        raw_last.insert(stat_name.clone(), value);
                        stat_map.insert(key, value - stat_last_kernel);
                    }
                }
            }
        }

        bytes_parsed += reader.stream_position().unwrap_or(0);
    } else {
        // not per kernel
        // if all_named_kernels.is_empty() {
        all_named_kernels.insert("final_kernel".to_string());
        // }

        let mut reader = rev_buf_reader::RevBufReader::new(file);
        // let bytes_to_read = 250 * 1024 * 1024;
        // reader.seek(io::SeekFrom::End(-(file_size.min(bytes_to_read) as i64)));
        // bytes_parsed += file_size.min(bytes_to_read as u64);

        let mut buffer = String::new();
        while let Some(Ok(line)) = reader.read_line(&mut buffer) {
            // println!("line: {:?}", &line);
            for (stat_name, (stat_kind, stat_regex)) in &stats {
                if stat_found.contains(stat_name) {
                    // println!("stat {} already found", &stat_name);
                    continue;
                }
                if let Some(value) = stat_regex
                    .captures(&line)
                    .and_then(|c| c.get(1))
                    .and_then(|m| m.as_str().trim().parse::<f64>().ok())
                {
                    stat_found.insert(stat_name.clone());
                    stat_map.insert(format!("final_kernel-{}", stat_name), value);
                }

                if stat_found.len() == stats.len() {
                    break;
                }
            }
        }
        let current = reader.stream_position().unwrap_or(0);
        let file_len = reader.seek(io::SeekFrom::End(0)).unwrap_or(0);
        bytes_parsed += file_len - current;
    }

    println!("stats: {:#?}", &stat_map);

    let files_parsed = 1;
    let duration = start.elapsed();
    println!(
        "done in {:?}: {} files and {} parsed ({}/s)",
        duration,
        files_parsed,
        HumanBytes(bytes_parsed),
        HumanBytes((bytes_parsed as f64 / (duration.as_secs() as f64)).floor() as u64),
    );
    Ok(())
}

/*
collect_aggregate:
    - 'gpu_tot_sim_insn\s*=\s*(.*)'
    - 'gpgpu_simulation_time\s*=.*\(([0-9]+) sec\).*'
    - 'gpu_tot_sim_cycle\s*=\s*(.*)'
    - '\s+L2_cache_stats_breakdown\[GLOBAL_ACC_R\]\[HIT\]\s*=\s*(.*)'
    - '\s+L2_cache_stats_breakdown\[GLOBAL_ACC_R\]\[TOTAL_ACCESS\]\s*=\s*(.*)'
    - '\s+L2_cache_stats_breakdown\[GLOBAL_ACC_W\]\[HIT\]\s*=\s*(.*)'
    - '\s+L2_cache_stats_breakdown\[GLOBAL_ACC_W\]\[TOTAL_ACCESS\]\s*=\s*(.*)'
    - '\s+Total_core_cache_stats_breakdown\[GLOBAL_ACC_R\]\[TOTAL_ACCESS\]\s*=\s*(.*)'
    - '\s+Total_core_cache_stats_breakdown\[GLOBAL_ACC_R\]\[HIT\]\s*=\s*(.*)'
    - '\s+Total_core_cache_stats_breakdown\[GLOBAL_ACC_W\]\[HIT\]\s*=\s*(.*)'
    - '\s+Total_core_cache_stats_breakdown\[GLOBAL_ACC_W\]\[TOTAL_ACCESS\]\s*=\s*(.*)'
    - '\s+Total_core_cache_stats_breakdown\[GLOBAL_ACC_R\]\[MSHR_HIT\]\s*=\s*(.*)'
    - 'gpgpu_n_tot_w_icount\s*=\s*(.*)'
    - 'total dram reads\s*=\s*(.*)'
    - 'total dram writes\s*=\s*(.*)'
    - 'kernel_launch_uid\s*=\s*(.*)'


# These stats are reset each kernel and should not be diff'd
# They cannot be used is only collecting the final_kernel stats
collect_abs:
    - 'gpu_ipc\s*=\s*(.*)'
    - 'gpu_occupancy\s*=\s*(.*)%'
    - 'L2_BW\s*=\s*(.*)+GB\/Sec'

# These stats are rates that aggregate - but cannot be diff'd
# Only valid as a snapshot and most useful for the final kernel launch
collect_rates:
    - 'gpgpu_simulation_rate\s+=\s+(.*)\s+\(inst\/sec\)'
    - 'gpgpu_simulation_rate\s+=\s+(.*)\s+\(cycle\/sec\)'
    - 'gpgpu_silicon_slowdown\s*=\s*(.*)x'
    - 'gpu_tot_ipc\s*=\s*(.*)'
*/
