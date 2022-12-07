#![allow(warnings)]

use anyhow::Result;
use clap::Parser;
use ini::Ini;
use lazy_static::lazy_static;
use regex::{Regex, RegexBuilder};
use std::collections::HashMap;
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

fn main() -> Result<()> {
    let start = Instant::now();
    let options = Options::parse();
    println!("options: {:#?}", &options);

    if !options.input.is_file() {
        anyhow::bail!("{} is not a file", options.input.display());
    }

    let input_file = fs::OpenOptions::new().read(true).open(&options.input)?;
    let mut reader = io::BufReader::new(input_file);
    let raw_stats = Ini::read_from(&mut reader)?;
    let mut stats: HashMap<(String, String), f64> = HashMap::new();
    for (sec, prop) in &raw_stats {
        println!("Section: {:?}", sec);
        for (key, value) in prop.iter() {
            println!("{:?}:{:?}", key, value);
            stats.insert(
                (sec.unwrap_or_default().trim().to_string(), key.to_string()),
                value.parse()?,
            );
        }
    }
    println!("stats: {:#?}", &stats);

    // shouldnt this be a JSON?
    gpgpusims::fs::create_dir(&options.output)?;
    let output_file = fs::OpenOptions::new()
        .write(true)
        .create(true)
        .truncate(true)
        .open(&options.output)?;

    let mut csv_writer = csv::WriterBuilder::new()
        .flexible(false)
        .from_writer(output_file);

    csv_writer.write_record(&["Section", "Stat", "Value"])?;
    for ((section, stat), value) in &stats {
        csv_writer.write_record(&[section, stat, &value.to_string()])?;
    }

    let duration = start.elapsed();
    println!("done after {:?}", duration,);
    Ok(())
}
