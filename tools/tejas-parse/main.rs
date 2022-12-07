#![allow(warnings)]

use anyhow::Result;
use clap::Parser;
use lazy_static::lazy_static;
use regex::{Regex, RegexBuilder};
use std::time::{Duration, Instant};
use std::{fs, io, path::PathBuf};

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Options {
    #[arg(short = 'i', long = "input")]
    input: PathBuf,

    #[arg(short = 'o', long = "output")]
    output: PathBuf,
}

#[derive(Debug, Default)]
struct Stats {
    emulator_type: Option<String>,
    created: Option<chrono::DateTime<chrono::FixedOffset>>,
    sim_time: Option<Duration>,
    total_inst_count: Option<u64>,
    total_cycle_count: Option<u64>,
    kips: Option<f64>,
    total_ipc: Option<f64>,
}

fn main() -> Result<()> {
    let start = Instant::now();
    let options = Options::parse();
    println!("options: {:#?}", &options);

    if !options.input.is_file() {
        anyhow::bail!("{} is not a file", options.input.display());
    }

    let input_file = fs::OpenOptions::new().read(true).open(&options.input)?;
    let mut reader = io::BufReader::new(input_file);
    gpgpusims::fs::create_dir(&options.output)?;
    let output_file = fs::OpenOptions::new()
        .write(true)
        .create(true)
        .truncate(true)
        .open(&options.output)?;

    let mut csv_writer = csv::WriterBuilder::new()
        .flexible(false)
        .from_writer(output_file);

    // read the entire stat file to memory and use regex for extracting values
    let raw_stats = io::read_to_string(&mut reader)?;
    /*
     * [Simulator Time]
     * Time Taken       =   0 : 3 minutes
     * Total Instructions executed : 2256
     * Instructions per Second  =   0.6734328358208955 KIPS
     * *************************************************************************
     * TOTAL INSTRUCTIONS = 2256
     * TOTAL CYCLES = 7777
     * TOTAL INSTRUCTIONS PER CYCLE = 0.2900861514722901
     * *************************************************************************
     */
    macro_rules! multiline_regex {
        ($pattern:literal) => {
            RegexBuilder::new($pattern)
                .multi_line(true)
                .build()
                .unwrap()
        };
    }

    lazy_static! {
        pub static ref SIM_TIME_REGEX: Regex =
            multiline_regex!(r"\[Simulator Time\][\s\S]*Time Taken\s*=\s*([ \S]+)$");
        pub static ref CREATED_REGEX: Regex =
            multiline_regex!(r"\[Configuration\][\s\S]*Schedule\s*:\s*([ \S]+)$");
        pub static ref EMULATOR_TYPE_REGEX: Regex =
            multiline_regex!(r"\[Configuration\][\s\S]*EmulatorType\s*:\s*(\w+)\s*$");
        pub static ref KIPS_REGEX: Regex = multiline_regex!(
            r"\[Simulator Time\][\s\S]*Instructions per Second\s*=\s*(\d+\.?\d*)\s*KIPS\s*$"
        );
        pub static ref TOTAL_INST_REGEX: Regex = multiline_regex!(
            r"\[Simulator Time\][\s\S]*Total Instructions executed\s*:\s*(\d+\.?\d*)\s*$"
        );
        pub static ref TOTAL_CYCLE_REGEX: Regex =
            multiline_regex!(r"\[Simulator Time\][\s\S]*TOTAL CYCLES\s*=\s*(\d+\.?\d*)\s*$");
        pub static ref TOTAL_IPC_REGEX: Regex = multiline_regex!(
            r"\[Simulator Time\][\s\S]*TOTAL INSTRUCTIONS PER CYCLE\s*=\s*(\d+\.?\d*)\s*$"
        );
    }

    let kips = extract("KIPS", &KIPS_REGEX, &raw_stats);
    let total_inst_count = extract("total_inst_count", &TOTAL_INST_REGEX, &raw_stats);
    let sim_time = match (kips, total_inst_count) {
        (Some(kips), Some(total_inst_count)) => Some(Duration::from_secs_f64(
            (total_inst_count as f64 / 1000.0) / kips,
        )),
        _ => None,
    };
    let stats = Stats {
        sim_time,
        kips,
        total_inst_count,
        total_cycle_count: extract("total_cycle_count", &TOTAL_CYCLE_REGEX, &raw_stats),
        total_ipc: extract("total_ipc", &TOTAL_IPC_REGEX, &raw_stats),
        emulator_type: extract("emulator_type", &EMULATOR_TYPE_REGEX, &raw_stats),
        created: extract("created", &CREATED_REGEX, &raw_stats).and_then(|s: String| {
            // Thu Dec 01 18:47:48 UTC 2022
            chrono::DateTime::parse_from_str(&s, "%a %b %d %H:%M:%S %Z %Y").ok()
        }),
    };
    println!("stats: {:#?}", &stats);

    let duration = start.elapsed();
    println!("done after {:?}", duration,);
    Ok(())
}

fn extract<T>(name: impl AsRef<str>, reg: &Regex, s: impl AsRef<str>) -> Option<T>
where
    T: std::str::FromStr + std::fmt::Display,
    <T as std::str::FromStr>::Err: std::fmt::Debug,
{
    match reg
        .captures(s.as_ref())
        .and_then(|c| c.get(1))
        .map(|m| m.as_str().trim()) // .to_string())
        .map(|m| m.parse::<T>())
    {
        Some(Err(err)) => {
            eprintln!("failed to parse {}: {:?}", name.as_ref(), &err);
            None
        }
        Some(Ok(value)) => {
            // println!("{}: {}", name.as_ref(), &value);
            Some(value)
        }
        None => {
            eprintln!("{} not found", name.as_ref());
            None
        }
    }
}
