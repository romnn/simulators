package main;
/*
/*****************************************************************************
				GPUTejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2014] [Indian Institute of Technology, Delhi]
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
------------------------------------------------------------------------------------------------------------

	Contributors:  Seep Goel, Geetika Malhotra, Harinder Pal
*****************************************************************************/ 
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import misc.Error;



public class readThread extends Thread
{
		public int javaTid;
		FileInputStream fis;
		DataInputStream dis;
		public int curPhase = 1;
		public long ins;
		public readThread(int tid)
		{
			javaTid=tid;
			start();
		}
		
		@Override
		public void run() 
		{	
			
				for(int j=0;j<ReadMain.totalNumKernels;j++){
					
					
						String inputFileName = ReadMain.traceFileFolder+"/"+javaTid+"_"+j+".txt";
						
						try {
							File inputTraceFile = new File(inputFileName);
							
							fis=new FileInputStream(inputTraceFile);
							dis=new DataInputStream(new BufferedInputStream(fis, 64*1024));
							try{
								while(true)
								{
									long tmp = dis.readLong();
									if(tmp==-3)
									{
										curPhase=0;
										continue;
									}
									else if(tmp==-4)
									{
										curPhase =1;
										continue;
									}
									if(tmp>=0 && curPhase==1)
										ins++;
								}
							}
							catch(EOFException e)
							{
								
							}
							} 
						catch (FileNotFoundException e) {
							
							Error.showErrorAndExit("FilePacket : no trace file found for JavaId = " + inputFileName);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
		

}
