// #![allow(warnings)]

use anyhow::Result;
use clap::Parser;
use lazy_static::lazy_static;
use regex::Regex;
use std::{fs, path::PathBuf, time::Instant};

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Options {
    #[arg(short = 'i', long = "input")]
    input: PathBuf,

    #[arg(short = 'o', long = "output")]
    output: PathBuf,
}

fn parse_hw_csv_2(_options: &Options) -> Result<()> {
    Ok(())
}

fn parse_hw_csv(options: &Options) -> Result<()> {
    let input_file = fs::OpenOptions::new().read(true).open(&options.input)?;
    gpgpusims::fs::create_dir(&options.output)?;
    let output_file = fs::OpenOptions::new()
        .write(true)
        .create(true)
        .truncate(true)
        .open(&options.output)?;

    let mut csv_reader = csv::ReaderBuilder::new()
        .flexible(true)
        .from_reader(input_file);
    let mut csv_writer = csv::WriterBuilder::new()
        .flexible(false)
        .from_writer(output_file);

    // search for line that indicates the beginning of the profile dump
    let mut records = csv_reader.records();
    for row in &mut records {
        lazy_static! {
            pub static ref PROFILE_RESULT_REGEX: Regex =
                Regex::new(r"^==\d*==\s*Profiling result:\s*$").unwrap();
        }
        lazy_static! {
            pub static ref PROFILER_DISCONNECTED_REGEX: Regex =
                Regex::new(r"^==PROF== Disconnected\s*$").unwrap();
        }

        // println!("row: {:#?}", row);
        match row {
            Ok(row) => {
                if row.len() == 1 && PROFILE_RESULT_REGEX.is_match(&row[0]) {
                    break;
                }
            }
            Err(err) => return Err(err.into()),
        }
    }

    // return Ok(());
    // let mut csv_reader = csv::Reader::from_reader(file);
    let mut limit = 1_000_000;
    for row in &mut records {
        if limit <= 0 {
            break;
        }
        let mut row = row.unwrap();
        row.trim();
        csv_writer.write_record(&row)?;
        // println!(
        //     "{:#?}",
        //     row.iter()
        //         .map(str::to_string)
        //         .collect::<Vec<String>>()
        // );
        limit -= 1;
    }
    Ok(())
}

fn main() -> Result<()> {
    let start = Instant::now();
    let options = Options::parse();
    println!("options: {:#?}", &options);

    if !options.input.is_file() {
        anyhow::bail!("{} is not a file", options.input.display());
    }

    if options
        .input
        .file_name()
        .and_then(|name| name.to_str())
        .map(|name| name.contains(&"gpc__cycles_elapsed"))
        .unwrap_or_default()
    {
        parse_hw_csv_2(&options)
    } else {
        parse_hw_csv(&options)
    }?;
    let duration = start.elapsed();
    println!("done after {:?}", duration,);
    Ok(())
}
