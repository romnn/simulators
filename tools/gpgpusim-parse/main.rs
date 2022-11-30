#![allow(warnings)]

use anyhow::Result;
use clap::Parser;
use lazy_static::lazy_static;
use regex::Regex;
use std::path::{Path, PathBuf};
use std::{
    fs,
    io::{self, Seek},
};

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Options {
    #[arg(short = 'i', long = "input")]
    input: PathBuf,

    #[arg(default_value_t = 2020)]
    port: u16,
}

// Do a quick 100-line pass to get the GPGPU-Sim Version number
// fn get_version(path: impl AsRef<Path>) {
// fn get_version(f: &mut fs::File) {
// fn get_version(f: &mut io::Read) {
fn get_version(mut f: impl io::BufRead + io::Seek) -> Option<String> {
    // seek to the beginning of the file
    f.seek(io::SeekFrom::Start(0));

    static MAX_LINES: usize = 100;
    let mut buffer = String::new();
    let mut line = 0;
    while let Ok(read) = f.read_line(&mut buffer)
    // .map(|u| if u == 0 { None } else { Some(buffer) })
    // .transpose()
    {
        if read == 0 {
            break;
        }
        if line >= MAX_LINES {
            break;
        }
        line += 1;
        // println!("{}", count);
        // println!("{}", buffer);

        lazy_static! {
            pub static ref GPGPUSIM_BUILD_REGEX: Regex =
                Regex::new(r".*GPGPU-Sim.*\[build\s+(.*)\].*").unwrap();
            pub static ref ACCELSIM_BUILD_REGEX: Regex =
                Regex::new(r".*Accel-Sim.*\[build\s+(.*)\].*").unwrap();
        }
        if let Some(build) = GPGPUSIM_BUILD_REGEX
            .captures(&buffer)
            .and_then(|c| c.get(1))
            .map(|m| m.as_str().to_string())
        {
            println!("line {} {:?}", line, build);
            return Some(build);
        }

        if let Some(build) = ACCELSIM_BUILD_REGEX
            .captures(&buffer)
            .and_then(|c| c.get(1))
            .map(|m| m.as_str().to_string())
        {
            println!("line {} {:?}", line, build);
            return Some(build);
        }

        // .ok_or_else(|| Error::InvalidHex(hex.clone()))?;
        // gpgpu_build_match = re.match(".*GPGPU-Sim.*\[build\s+(.*)\].*", line)
        // println!("{}", line?.trim());
        buffer.clear();
    }
    None

    // let reader = BufReader::new(file);
    // for line in reader.lines() {
    //     println!("{}", line?);
    // }

    /*
    MAX_LINES = 100
    count = 0
    f = open(outfile)
    for line in f:
        count += 1
        if count >= MAX_LINES:
            break
        gpgpu_build_match = re.match(".*GPGPU-Sim.*\[build\s+(.*)\].*", line)
        if gpgpu_build_match:
            stat_map["all_kernels" + app_and_args + config + "GPGPU-Sim-build"] = gpgpu_build_match.group(1)
            break
        accelsim_build_match = re.match("Accel-Sim.*\[build\s+(.*)\].*", line)
        if accelsim_build_match:
            stat_map["all_kernels" + app_and_args + config + "Accel-Sim-build"] = accelsim_build_match.group(1)
    f.close()
    */
}

/// Performs a quick 10000-line reverse pass,
/// to make sure the simualtion thread finished.
fn check_finished(mut f: impl io::BufRead + io::Seek) -> Option<String> {
    // seek to the beginning of the file
    f.seek(io::SeekFrom::End(0));

    static MAX_LINES: usize = 10_000;
    let mut buffer = String::new();
    let mut line = 0;
    while let Ok(read) = f.read_line(&mut buffer) {
        if read == 0 {
            break;
        }
        buffer.clear();
    }
    false
}

fn main() -> Result<()> {
    let options = Options::parse();
    println!("options: {:?}", &options);

    if !options.input.is_file() {
        anyhow::bail!("{} is not a file", options.input.display());
    }
    let mut file = fs::File::open(&options.input)?;
    let mut reader = io::BufReader::new(file);
    let version = get_version(&mut reader);
    // get_version(&mut reader);
    println!("done");
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
