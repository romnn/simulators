#![allow(warnings)]

use anyhow::Result;
use clap::Parser;
use lazy_static::lazy_static;
use reader::BufReadLine;
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

mod reader {
    use std::{
        fs,
        io::{self, BufRead},
        path::Path,
    };

    pub trait BufReadLine {
        fn read_line<'buf>(
            &mut self,
            buffer: &'buf mut String,
        ) -> Option<io::Result<&'buf mut String>>;
    }

    // pub struct BufReader {
    //     inner: io::BufReader<fs::File>,
    // }

    // impl std::ops::Deref for BufReader {
    //     type Target = io::BufReader<fs::File>;

    //     fn deref(&self) -> &Self::Target {
    //         &self.inner
    //     }
    // }

    // impl BufReader {
    //     pub fn new(file: fs::File) -> Self {
    //         let inner = io::BufReader::new(file);

    //         Self { inner }
    //     }

    //     pub fn open(path: impl AsRef<Path>) -> io::Result<Self> {
    //         let file = fs::File::open(path)?;
    //         Ok(Self::new(file))
    //     }
    // }

    // impl BufReadLine for BufReader {
    // impl<T> BufReadLine for io::BufReader<T>
    impl<R> BufReadLine for R
    // io::BufReader<T>
    where
        R: io::BufRead,
        // T: io::Read,
    {
        fn read_line<'buf>(
            &mut self,
            buffer: &'buf mut String,
        ) -> Option<io::Result<&'buf mut String>> {
            buffer.clear();

            // self.reader
            //// self.read_line(buffer)
            io::BufRead::read_line(self, buffer)
                .map(|u| if u == 0 { None } else { Some(buffer) })
                .transpose()
        }
    }
    // impl<'a, R> BufReadLine for R where &'a R: BufReadLine {}
}

// Do a quick 100-line pass to get the GPGPU-Sim Version number
// fn get_version(path: impl AsRef<Path>) {
// fn get_version(f: &mut fs::File) {
// fn get_version(f: &mut io::Read) {
fn get_version(mut f: impl reader::BufReadLine + io::Seek) -> Option<String> {
    // seek to the beginning of the file
    f.seek(io::SeekFrom::Start(0));

    static MAX_LINES: usize = 100;
    let mut buffer = String::new();
    let mut lines = 0;
    while let Some(Ok(line)) = f.read_line(&mut buffer)
    // .map(|u| if u == 0 { None } else { Some(buffer) })
    // .transpose()
    {
        // if read == 0 {
        //     break;
        // }
        if lines >= MAX_LINES {
            break;
        }
        lines += 1;
        // println!("{}", count);
        // println!("{}", buffer);

        lazy_static! {
            pub static ref GPGPUSIM_BUILD_REGEX: Regex =
                Regex::new(r".*GPGPU-Sim.*\[build\s+(.*)\].*").unwrap();
            pub static ref ACCELSIM_BUILD_REGEX: Regex =
                Regex::new(r".*Accel-Sim.*\[build\s+(.*)\].*").unwrap();
        }
        if let Some(build) = GPGPUSIM_BUILD_REGEX
            .captures(&line)
            .and_then(|c| c.get(1))
            .map(|m| m.as_str().to_string())
        {
            println!("line {} {:?}", line, build);
            return Some(build);
        }

        if let Some(build) = ACCELSIM_BUILD_REGEX
            .captures(&line)
            .and_then(|c| c.get(1))
            .map(|m| m.as_str().to_string())
        {
            println!("line {} {:?}", line, build);
            return Some(build);
        }

        // .ok_or_else(|| Error::InvalidHex(hex.clone()))?;
        // gpgpu_build_match = re.match(".*GPGPU-Sim.*\[build\s+(.*)\].*", line)
        // println!("{}", line?.trim());
        // buffer.clear();
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
fn check_finished(mut f: impl reader::BufReadLine + io::Seek) -> bool {
    use reader::BufReadLine;
    // seek to the beginning of the file
    // 250MB
    // size of file in bytes
    // assume its fine to seek before start (0)
    // let seek_back_bytes = f.metadata().unwrap().len().min(-250 * 1024 * 1024);
    let seek_back_bytes = -250 * 1024 * 1024;
    f.seek(io::SeekFrom::End(seek_back_bytes));
    // todo: write our own version of this
    // https://github.com/mjc-gh/rev_lines/blob/master/src/lib.rs
    // let rev_reader = rev_lines::RevLines::new(f);

    static MAX_LINES: usize = 10_000;
    let mut buffer = String::new();
    let mut lines = 0;

    /*
    SIM_EXIT_STRING = "GPGPU-Sim: \*\*\* exit detected \*\*\*"
    exit_success = False
    MAX_LINES = 10000
    BYTES_TO_READ = int(250 * 1024 * 1024)
    count = 0
    f = open(outfile)
    fsize = int(os.stat(outfile).st_size)
    if fsize > BYTES_TO_READ:
        f.seek(0, os.SEEK_END)
        f.seek(f.tell() - BYTES_TO_READ, os.SEEK_SET)
    lines = f.readlines()
    for line in reversed(lines):
        count += 1
        if count >= MAX_LINES:
            break
        exit_match = re.match(SIM_EXIT_STRING, line)
        if exit_match:
            exit_success = True
            break
    del lines
    f.close()
    */

    // for line in rev_reader {
    //     println!("{}", line);
    //     if lines >= MAX_LINES {
    //         break;
    //     }
    //     lines += 1;
    // }
    while let Some(Ok(line)) = f.read_line(&mut buffer) {
        if lines >= MAX_LINES {
            break;
        }
        // println!("{}", line);
        lazy_static! {
            pub static ref GPGPUSIM_EXIT_REGEX: Regex =
                Regex::new(r"GPGPU-Sim: \*\*\* exit detected \*\*\*").unwrap();
        }
        if GPGPUSIM_EXIT_REGEX.captures(&line).is_some() {
            // if let Some(exit) = GPGPUSIM_EXIT_REGEX
            //     .captures(&line)
            //     .and_then(|c| c.get(1))
            //     .map(|m| m.as_str().to_string())
            // {
            println!("found exit in line {}", lines);
            return true;
        }

        lines += 1;
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
    // let mut reader = reader::BufReader::new(file);
    let version = get_version(&mut reader);
    let finished = check_finished(&mut reader);
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
