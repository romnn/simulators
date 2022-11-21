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
package emulatorinterface.communication.filePacket;

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

	Contributors:  Geetika Malhotra, Seep Goel
*****************************************************************************/

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import main.Main;
import misc.Error;

import config.SimulationConfig;


import emulatorinterface.BlockState;

import emulatorinterface.SimplerRunnableThread;
import emulatorinterface.communication.IpcBase;

import emulatorinterface.translator.x86.instruction.InstructionClass;
import emulatorinterface.translator.x86.instruction.FullInstructionClass;

import generic.CircularPacketQueue;


public class SimplerFilePacket extends IpcBase{


	public final int TYPE_BRANCH = 0;
	public final int TYPE_MEM = 1;
	public final int TYPE_IP = 2;
	public final  int TYPE_BLOCK_END = -1;
	public final  int TYPE_KERNEL_END = -2;
	public final  int MEM_START=-3;
	public final  int MEM_END=-4;
	FileInputStream fis=null;
	DataInputStream dis=null;
	
	long totalFetchedAssemblyPackets = 0;
	public int javaTid;
	public SimplerFilePacket(int javaTid) {
			this. javaTid=javaTid;
	}
	
	int blocks = 0;
	
	public void openNextFile(int kernelNumber)
	{
		blocks = 0;
		String inputFileName = Main.getTraceFileFolder()+"/"+javaTid+"_"+kernelNumber+".txt";
		
		try {
			if(dis!=null)
			{
				dis.close();
				fis.close();
			}
			File inputTraceFile = new File(inputFileName);

			fis=new FileInputStream(inputTraceFile);
			dis=new DataInputStream(new BufferedInputStream(fis, 64*1024));
			} 
		catch (Exception e) {
			
			Error.showErrorAndExit("FilePacket : no trace file found for JavaId = " + inputFileName);
			e.printStackTrace();
			System.exit(0);
		}

	}
	
	
	public int kernelExecuted=0,totalKernels=Main.totalNumKernels;

	public boolean kernelLeft()
	{

		if(kernelExecuted<totalKernels)
			return true;
		else{
			return false;}
	}

		public long ins=0;
	public int fetchManyPackets(SimplerRunnableThread runner, int currBlock, CircularPacketQueue fromEmulator, BlockState blockParam) {

		if(dis==null) {
			return 0;
		}


		int maxSize = fromEmulator.spaceLeft();
		
		try{
			
			
			for(int i=0; i<maxSize; i++)
			{
				Integer tmp = null;
				try
				{
					 tmp = dis.readInt();
				}
				catch(EOFException e)
				{
					System.out.println("cannot read "+dis+"  "+i);
					System.exit(0);
//					maxSize=-1;
				}
				if(tmp!=null)
				{
					if(tmp==TYPE_KERNEL_END)
					{
						tmp = dis.readInt();//this is the number of blocks in this kernel, hence neglected.
						continue;
					}
					else if(tmp==TYPE_BLOCK_END)
					{
						fromEmulator.enqueue(new FullInstructionClass(InstructionClass.TYPE_BLOCK_END,null), -1,  null);
						blocks++;
						return i;
					}
					else
					{
						Integer ip=tmp;
						FullInstructionClass packetInHashTable = runner.kernelInstructionsTable.get(ip);
						if(packetInHashTable == null)
						{
							misc.Error.showErrorAndExit("did not find the packet for ip : "+ip);
							
						}
				
						if(packetInHashTable.instructionclass == InstructionClass.INTEGER_LOAD_CONSTANT||
												packetInHashTable.instructionclass == InstructionClass.INTEGER_STORE_CONSTANT||
														packetInHashTable.instructionclass == InstructionClass.INTEGER_LOAD_SHARED||
																packetInHashTable.instructionclass == InstructionClass.INTEGER_STORE_SHARED||packetInHashTable.instructionclass == InstructionClass.FLOATING_POINT_LOAD_CONSTANT||
																								packetInHashTable.instructionclass == InstructionClass.FLOATING_POINT_STORE_CONSTANT|| 
																										packetInHashTable.instructionclass == InstructionClass.FLOATING_POINT_LOAD_SHARED||
																												packetInHashTable.instructionclass == InstructionClass.FLOATING_POINT_STORE_SHARED) 

						{
							Long ftmp,MemoryAddresses[] = new Long[SimulationConfig.ThreadsPerCTA];
							int memAddressRead = 0;
							if((tmp = dis.readInt())==MEM_START)
							{
								while((ftmp = dis.readLong())!=MEM_END)//read all the addresses
								{
									MemoryAddresses[memAddressRead++] = ftmp;
								}
								fromEmulator.enqueue( packetInHashTable, ip,  MemoryAddresses);
							}
							else
							{
								misc.Error.showErrorAndExit("This should be a mem packet but MEM_START not found : "+ip);
							}
						}
						else
						{
							fromEmulator.enqueue( packetInHashTable, ip, null );
						}
						ins++;
						
					}
				}
				else
				{
//					fromEmulator.enqueue(InstructionClass.TYPE_KERNEL_END, -2, null);
				}
				
			}
		}
		catch(EOFException e)
		{
			e.printStackTrace();
			System.exit(0);
		
			fromEmulator.enqueue(new FullInstructionClass(InstructionClass.TYPE_BLOCK_END,null), -1, null);
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
		}

		return maxSize;
	}

	public void errorCheck(int tidApp, long totalReads) {
		// we do not do any error checking for filePacket interface
	}
	
	

	@Override
	public void initIpc() {
		// TODO Auto-generated method stub
		
	}

	
}
