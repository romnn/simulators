use std::path::Path;
use std::{fs, io};

pub fn create_dir(path: impl AsRef<Path>) -> io::Result<()> {
    path.as_ref().parent().map(fs::create_dir_all).transpose()?;
    Ok(())
}
