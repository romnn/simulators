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

package emulatorinterface;


import java.io.FileInputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.Hashtable;

import config.SimulationConfig;
import config.SmConfig;
import config.SystemConfig;
import config.TpcConfig;

import main.ArchitecturalComponent;
import main.Main;
import memorysystem.Cache;
import pipeline.GPUpipeline;
import emulatorinterface.ThreadBlockState.blockState;
import emulatorinterface.communication.Encoding;
import emulatorinterface.communication.IpcBase;
import emulatorinterface.communication.Packet;
import emulatorinterface.communication.filePacket.SimplerFilePacket;
import emulatorinterface.translator.x86.instruction.InstructionClass;
import emulatorinterface.translator.x86.instruction.FullInstructionClass;

import emulatorinterface.translator.x86.objparser.ObjParser;
import generic.*;

import java.util.concurrent.*;

import dram.MainMemoryDRAMController;
//import jsr166y.Phaser;

/* MaxNumThreads threads are created from this class. Each thread
 * continuously keeps reading from the shared memory segment according
 * to its index(taken care in the jni C file).
 */
public class SimplerRunnableThread implements Runnable,Encoding {
	
	int currBlock = 0;

	public long pipe_time = 0;
	public long mem_time = 0;
	public long barier_wait=0;
	public long phaser_wait=0;
	public boolean mem_flag = false;
	//static EmulatorThreadState[] emulatorThreadState;// = new EmulatorThreadState[EMUTHREADS];
	static ThreadBlockState[] threadBlockState;//=new ThreadBlockState[EMUTHREADS];
	public int javaTid;
	public ObjParser myParser;
	public int TOTALBLOCKS, blocksExecuted=0;
	FileInputStream fos = null;
	ObjectInputStream is = null;

	public Hashtable<Long, FullInstructionClass> kernelInstructionsTable;
	public void initialize() throws IOException
	{
		currBlock = 0;
		blocksExecuted=0;
		epochCount=0;
		TOTALBLOCKS=0;
		if(ipcBase.kernelLeft())
		{	
			TOTALBLOCKS= Main.totalBlocks[ipcBase.kernelExecuted] / SimulationConfig.MaxNumJavaThreads;
			if(javaTid < (Main.totalBlocks[ipcBase.kernelExecuted] % SimulationConfig.MaxNumJavaThreads))
			{
				TOTALBLOCKS++; 
			}
		}
		if(TOTALBLOCKS==0)
		{
			return;
		}
		blockState = new BlockState[TOTALBLOCKS];
		inputToPipeline.clear();
		kernelInstructionsTable=main.Main.kernelInstructionsTables[ipcBase.kernelExecuted];
		ipcBase.openNextFile(ipcBase.kernelExecuted);
		
		
		maxCoreAssign = 0; 
		for(int i=0; i<TOTALBLOCKS; i++)
		{
			blockState[i] = new BlockState();
		}

	}
	

	int maxCoreAssign = 0;      //the maximum core id assigned 	
	public BlockState[] blockState;

	GenericCircularQueue<Instruction> inputToPipeline;
	GPUpipeline pipelineInterfaces;
	SM assignedSM ;
	public SimplerFilePacket ipcBase;

	void iFinished() {
			epochEnd.arriveAndDeregister();
	}


    final int ipcCount = 1000;
	
	public void run() {	
			try {
				main.Main.runners[javaTid].initialize();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			ipcBase.javaThreadStarted = true;
		boolean blockEndSeen=false;
		while(ipcBase.kernelLeft())
		{
			if(TOTALBLOCKS==0){
				ipcBase.kernelExecuted++;
				try {
					main.Main.runners[javaTid].initialize();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			// create pool for emulator packets
			CircularPacketQueue fromEmulator = new CircularPacketQueue(ipcCount);
			boolean allover = false;
			BlockState threadParam;
			
			Packet pnew = new Packet();
			System.out.println(TOTALBLOCKS);
			while(true)
			{   
//				System.out.println("block being executed is "+currBlock);
				threadParam = blockState[currBlock];
				int numReads = 0;
				
				if(threadParam.isFirstPacket)
				{
					pipelineInterfaces.setBlockId(currBlock);
					if(pipelineInterfaces.isExecutionComplete() == true)
					{
						pipelineInterfaces.setExecutionComplete(false);
					}
				}
				// add outstanding micro-operations to input to pipeline
				if (threadParam.outstandingMicroOps.isEmpty() == false) {
					if(threadParam.outstandingMicroOps.size()<inputToPipeline.spaceLeft() && !pipelineInterfaces.containingExecutionEngine.getScheduleUnit().scheduleExecuteLatch.isFull() ) {
						while(threadParam.outstandingMicroOps.isEmpty() == false) {
							Instruction tmp=threadParam.outstandingMicroOps.pollFirst();
							tmp.setBlockID(currBlock);
							inputToPipeline.enqueue(tmp);
						}
					} else {
						// there is no space in pipelineBuffer. So don't fetch any more instructions
						continue;
					}
				}
				
				numReads = ipcBase.fetchManyPackets(this, currBlock, fromEmulator, threadParam);
				if (fromEmulator.size() == 0) {
					continue;
				}

				// update the number of read packets
				threadParam.totalRead += numReads;
				
				// If java thread itself is terminated then break out from this
				// for loop. also update the variable all-over so that I can
				// break from the outer while loop also.
				if (ipcBase.javaThreadTermination == true) {
					allover = true;
					break;
				}
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//				this part in simple runnable thread has to be corrected
				threadParam.checkStarted();
				FullInstructionClass type;
//				int i=0;
				// Process all the packets read from the communication channel
				while(fromEmulator.isEmpty() == false) {
					
					pnew = fromEmulator.dequeue();
					type=pnew.insClass;
					if(type.instructionclass==InstructionClass.TYPE_BLOCK_END)
					{
						Instruction tmp=Instruction.getInvalidInstruction();
						tmp.setBlockID(currBlock);
						this.inputToPipeline.enqueue(tmp);
						threadParam.finished=true;
						threadParam.isFirstPacket=true;
						blockEndSeen=true;	
					}
					if(type.instructionclass==InstructionClass.TYPE_KERNEL_END)
					{
						System.out.println("EOF from Java "+javaTid+" with totblock :"+TOTALBLOCKS);
						continue;
						
						
					}
					
					boolean ret = processPacket(threadParam, pnew, currBlock);
//					System.out.println("here we are");
//					i++;
					if(ret==false) {
						System.out.println(ret);
						// There is not enough space in pipeline buffer. 
						// So don't process any more packets.
						break;
					}
				}
//				change this to assigned SM
				runPipelines();
//				System.out.println("where stuck run ");
				if(blockEndSeen)
				{
					
					currBlock=(currBlock+1);
//					System.out.println("size of warp table for java thread"+javaTid+"is"+pipelineInterfaces.containingExecutionEngine.WarpTable.size());
					if(currBlock==TOTALBLOCKS){allover=true;currBlock--;}
					blockEndSeen=false;
				}
				if (ipcBase.javaThreadTermination == true) {  //check if java thread is finished
					allover = true;
					break;
				}
				if(allover) {
//					System.out.println("terminated and java Thread is finished"+javaTid);
					ipcBase.javaThreadTermination = true;
					break;
			    }
			}
			System.gc();
			finishAllPipelines();
			Statistics.calculateCyclesKernel(javaTid);
			ipcBase.kernelExecuted++;
			System.out.println("Simulated " + ipcBase.kernelExecuted + " kernels on " + javaTid);
			ipcBase.javaThreadTermination = false;
			
			blocksExecuted+=currBlock+1;
		    System.out.println("Blocks are executed"+blocksExecuted+"for the thread"+javaTid);
			
				try {
					main.Main.runners[javaTid].initialize();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
		}
		
		ipcBase.javaThreadTermination = true;
		iFinished();
		ipcBase.free.release();
		return;
	}
	

	public Thread t;
    public Phaser kernelEnd, epochEnd;
	// initialise a reader thread with the correct thread id and the buffer to
	// write the results in.
	@SuppressWarnings("unchecked")
	public SimplerRunnableThread(String threadName, int javaTid, SimplerFilePacket ipcBase, 
			SM sm, Phaser epochEnd) {

		this.ipcBase = ipcBase;		
		this.javaTid = javaTid;
	    myParser = new ObjParser();
		this.epochEnd=epochEnd;
		System.out.println("--  starting java thread"+this.javaTid);
		inputToPipeline = new GenericCircularQueue<Instruction>(Instruction.class, 4000000);
		assignedSM=sm;
		pipelineInterfaces=assignedSM.getPipelineInterface();
		pipelineInterfaces.setcoreStepSize(assignedSM.getStepSize());
		GenericCircularQueue<Instruction>[] toBeSet =(GenericCircularQueue<Instruction>[])Array.newInstance(GenericCircularQueue.class, 1);
		toBeSet[0] = inputToPipeline;
		pipelineInterfaces.setInputToPipeline(toBeSet);
		if(pipelineInterfaces.isExecutionComplete() == true)
		{
			pipelineInterfaces.setExecutionComplete(false);
		}
		// Special case must be made for RunnableFromFile
		if(this.ipcBase != null) {
			t=(new Thread(this, threadName));
		}
		
		

	}

	protected void runPipelines() {
		int minN = Integer.MAX_VALUE;

		boolean RAMcyclerun = false;

		if(maxCoreAssign>0) {
			
			int m = inputToPipeline.size(); 
			int n= pipelineInterfaces.containingExecutionEngine.WarpTable.size();	
			if (n < minN && n != 0)
				minN = n;
			}
		minN = (minN == Integer.MAX_VALUE) ? 0 : minN;

		
		for (int i1 = 0; i1 < minN; i1++) {
//			for(int k=0;k<SystemConfig.mainMemoryConfig.numChans;k++){
//			ArchitecturalComponent.getMainMemoryDRAMController(null,k).oneCycleOperation();}
			pipelineInterfaces.oneCycleOperation();
				AddToSetAndIncrementClock(); 
//				for(int k=0;k<SystemConfig.mainMemoryConfig.numChans;k++){
//				ArchitecturalComponent.getMainMemoryDRAMController(null,k).enqueueToCommandQ();		}
	}
	
	}


private void AddToSetAndIncrementClock() {

	
		ArchitecturalComponent.getCores()[pipelineInterfaces.getCore().getTPC_number()][pipelineInterfaces.getCore().getSM_number()].clock.incrementClock();
		if(GlobalClock.getCurrentTime()<ArchitecturalComponent.getCores()[pipelineInterfaces.getCore().getTPC_number()][pipelineInterfaces.getCore().getSM_number()].clock.getCurrentTime())
			GlobalClock.setCurrentTime(ArchitecturalComponent.getCores()[pipelineInterfaces.getCore().getTPC_number()][pipelineInterfaces.getCore().getSM_number()].clock.getCurrentTime());
		blockState[currBlock].tot_cycles++;
		
		epochCount++;
	if (epochCount % main.Main.SynchClockDomainsCycles ==0) {
//		System.out.println("Synchronizing - Waiting at Phaser");
		int phase = epochEnd.getPhase(); 
		if (phase==previousPhase) {
			long g=System.currentTimeMillis();
			epochEnd.awaitAdvance(phase);
		phaser_wait+=(System.currentTimeMillis()-g);
			}
			previousPhase = epochEnd.arrive();
//			System.out.println("phaser wait for java thread"+javaTid+"is"+phaser_wait);
			}
}

	public int epochCount,previousPhase=-1;
	@SuppressWarnings("unused")
	protected boolean processPacket(BlockState thread, Packet pnew, int Blocktid) {

		
		boolean isSpaceInPipelineBuffer = true;
		
		int GlobalId = javaTid * TOTALBLOCKS + Blocktid;
	
		if (thread.isFirstPacket) 
		{
			
			this.pipelineInterfaces.getCore().getExecEngine().setExecutionComplete(false);
			
			if(Blocktid>=maxCoreAssign)
				maxCoreAssign = Blocktid+1;

			thread.packetList.add(pnew);
			thread.isFirstPacket=false;
			return isSpaceInPipelineBuffer;
			
		}
		else 
		{
			
			int oldLength = inputToPipeline.size();
			
			long numHandledInsn = 0;
			int numMicroOpsBefore = thread.outstandingMicroOps.size();
			
			myParser.fuseInstruction( thread.packetList.get(0).ip, thread.packetList, thread.outstandingMicroOps);
			int numMicroOpsAfter = thread.outstandingMicroOps.size();

			if(numMicroOpsAfter>numMicroOpsBefore) {
				numHandledInsn = 1;
			} else {
				numHandledInsn = 0;
				
			}

			for(int i=numMicroOpsBefore; i<numMicroOpsAfter; i++) {
				if(i==numMicroOpsBefore) {
					thread.outstandingMicroOps.peek(i).setCISCProgramCounter(thread.packetList.get(0).ip);
				} else {
					thread.outstandingMicroOps.peek(i).setCISCProgramCounter(-1);
				}
				
			}
			
			// Either add all outstanding micro-ops or none.
			if(thread.outstandingMicroOps.size()<this.inputToPipeline.spaceLeft()) {
				// add outstanding micro-operations to input to pipeline
				while(thread.outstandingMicroOps.isEmpty() == false) {
					Instruction tmp=thread.outstandingMicroOps.dequeue();
					tmp.setBlockID(Blocktid);
					this.inputToPipeline.enqueue(tmp);
				}
			} else {
				isSpaceInPipelineBuffer = false;
			}
				

			
			thread.packetList.clear();
			thread.packetList.add(pnew);
			int newLength = inputToPipeline.size();
		}

		return isSpaceInPipelineBuffer;
	}

	

	private void checkForBlockingPacket(long value,int TidApp) {
		// TODO Auto-generated method stub
		int val=(int)value;
		switch(val)
		{
		case LOCK:
		case JOIN:
		case CONDWAIT:
		case BARRIERWAIT:threadBlockState[TidApp].gotBlockingPacket(val);
		
		}
	}
	
	private void checkForUnBlockingPacket(long value,int TidApp) {
		// TODO Auto-generated method stub
		int val=(int)value;
		switch(val)
		{
		case LOCK+1:
		case JOIN+1:
		case CONDWAIT+1:
		case BARRIERWAIT+1:threadBlockState[TidApp].gotUnBlockingPacket();
		
		}
	}
	@SuppressWarnings("unused")
	public void finishAllPipelines() {
		while(true)
		{
			
			if(maxCoreAssign>0) {
//				for(int k=0;k<SystemConfig.mainMemoryConfig.numChans;k++){
//					ArchitecturalComponent.getMainMemoryDRAMController(null,k).oneCycleOperation();}
					pipelineInterfaces.oneCycleOperation();
						AddToSetAndIncrementClock(); 
//						for(int k=0;k<SystemConfig.mainMemoryConfig.numChans;k++){
//						ArchitecturalComponent.getMainMemoryDRAMController(null,k).enqueueToCommandQ();		}
//						System.out.flush();
		}
			
	
			if(pipelineInterfaces.containingExecutionEngine.WarpTable.size()==0)
			{
				break;
			}
			
		//		System.out.println("pipeline size left is "+pipelineInterfaces.containingExecutionEngine.WarpTable.size());
		}
		
		
		
	}
}