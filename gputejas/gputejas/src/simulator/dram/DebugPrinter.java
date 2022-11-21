package dram;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DebugPrinter {

	public FileWriter logFileWriter;
	
	public DebugPrinter(String filename) {
		
		try {
			
			File logfile = new File(filename);
			logFileWriter = new FileWriter(logfile);
		}
		catch (IOException e)
		{
			//System.out.println("Harveenk: unable to create log file");
			//System.out.println("Exception: " + e);
		}

	}
	
	public void close()
	{
		try{
			logFileWriter.close();
		}
		catch(IOException e)
		{
		e.printStackTrace();
		}

	}
	
	public void print(String s)
	{
		try {
		logFileWriter.write(s);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
