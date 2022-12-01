use std::io;

/// More ergonomic API for `read_line` that returns
/// an `Option<std::io::Result<&mut String>>`.
///
/// `None` is returned if there are no more lines to read.
/// Otherwise, the mutable reference to the input buffer containing
/// the line is returned without an additional allocation.
pub trait BufReadLine {
    fn read_line<'buf>(&mut self, buffer: &'buf mut String)
        -> Option<io::Result<&'buf mut String>>;
}

impl<R> BufReadLine for R
where
    R: io::BufRead,
{
    fn read_line<'buf>(
        &mut self,
        buffer: &'buf mut String,
    ) -> Option<io::Result<&'buf mut String>> {
        buffer.clear();

        io::BufRead::read_line(self, buffer)
            .map(|u| if u == 0 { None } else { Some(buffer) })
            .transpose()
    }
}

//use std::io::{self, Read};

//pub trait BufReadLineReverse {
//    fn read_line_reverse<'buf>(
//        &mut self,
//        buffer: &'buf mut String,
//    ) -> Option<io::Result<&'buf mut String>>;
//}

//pub struct ReverseBufReader<R> {
//    reader: BufReader<R>,
//    reader_pos: u64,
//    buf_size: u64
//}

//impl<R> BufReadLineReverse for R
//where
//    R: io::BufRead + io::Seek,
//{
//    fn read_line_reverse<'buf>(
//        &mut self,
//        buffer: &'buf mut String,
//    ) -> Option<io::Result<&'buf mut String>> {
//        // buffer.clear();

//        static DEFAULT_SIZE: usize = 4096;
//        let size = DEFAULT_SIZE;
//        buffer.insert_bytes(0, vec![0; size as usize]);

//        static LF_BYTE: u8 = '\n' as u8;
//        static CR_BYTE: u8 = '\r' as u8;

//        self.seek(io::SeekFrom::Current(-(size as i64)));
//        //?;
//        // self.read_exact(&mut buf[0..(size as usize)]); // ?;

//        None

//        // todo: keep reading in reverse chunks until we find a new line
//        // io::BufRead::read_line(self, buffer)
//        //     .map(|u| if u == 0 { None } else { Some(buffer) })
//        //     .transpose()
//    }
//}
