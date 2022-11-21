package pipeline;

///
///
//				GPUTejas Simulator
//------------------------------------------------------------------------------------------------------------
//
//   Copyright [2014] [Indian Institute of Technology, Delhi]
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//------------------------------------------------------------------------------------------------------------
//
//	Contributors:  Seep Goel, Geetika Malhotra, Harinder Pal
/// 

import java.io.FileWriter;
import java.io.IOException;

import main.ArchitecturalComponent;
import config.EnergyConfig;
import config.OperationLatencyConfig;
import generic.Event;
import generic.EventQueue;
import generic.GenericCircularQueue;
import generic.Instruction;
import generic.OperationType;
import generic.PortType;
import generic.RequestType;
import generic.SM;
import generic.SimulationElement;
import pipeline.StageLatch_MII;



//TODO this file is total bullshit No real scheduling happening in the code
// Totally wrong code in this file have to correct the entire thing
// Schedules after the warps are drained in the event Queue
public class ScheduleUnit extends SimulationElement{
	
	
	SM sm;
	GPUExecutionEngine containingExecutionEngine;
	
	public GenericCircularQueue<Instruction> inputToPipeline;
	public GenericCircularQueue<Instruction> completeWarpPipeline;
	public StageLatch_MII scheduleExecuteLatch;
	Instruction fetchedInstruction;
	boolean fetchedInstructionStatus;
///////////////////////////////////////////////////////////////////////
	int loadaccess;
	int storeaccess;
	int addressaccess;
	int intALUaccess;
	int intMULaccess;
	int intDIVaccess;
	int floatALUaccess;
	int floatMULaccess;
	int floatDIVaccess;
	int predicateaccess;
	int branchaccess;
	int callaccess;
	int returnaccess;
	int exitaccess;
	long numAccesses = 0;
	//////////////////////////////////////////////////
	public ScheduleUnit(SM sm, EventQueue eventQueue, GPUExecutionEngine execEngine) {
		super(PortType.Unlimited, -1, -1, eventQueue, -1, -1);
		this.sm = sm;
		this.containingExecutionEngine = execEngine;
		this. scheduleExecuteLatch = execEngine.getScheduleExecuteLatch();
		this.fetchedInstruction = null;
		this.fetchedInstructionStatus = false;
		this.completeWarpPipeline=new GenericCircularQueue<Instruction>(Instruction.class, 4000000);
		loadaccess=0;
		storeaccess=0;
		addressaccess=0;
		intALUaccess=0;
		intMULaccess=0;
		intDIVaccess=0;
		floatALUaccess=0;
		floatMULaccess=0;
		floatDIVaccess=0;
		predicateaccess=0;
		branchaccess=0;
		callaccess=0;
		returnaccess=0;
		exitaccess=0;
		
	}

	public void fetchInstruction()
	
	{

		 
		if(this.fetchedInstruction != null)
		{
//			System.out.println("not null"); 
			return;
		}
		
		Instruction newInstruction = completeWarpPipeline.pollFirst();
		if(newInstruction!=null){
		if(newInstruction.getOperationType() == OperationType.inValid) {
			this.fetchedInstruction = newInstruction;
			this.fetchedInstructionStatus = true;
		}
		else
		{
			this.fetchedInstruction =  newInstruction;
			this.fetchedInstructionStatus = true;
			containingExecutionEngine.gpuMemorySystem.issueRequestToInstrCache(newInstruction.getCISCProgramCounter());
		}}
		
	}
	public void performSchedule(GPUpipeline inorderPipeline)
	{
//		System.out.println("in schedule unit");
		if(scheduleExecuteLatch.isFull() == true)
		{
			System.out.println("is Full");
			return;
		}
//		System.out.println("Fetched instruction is"+this.fetchedInstructionStatus);
		if(!this.fetchedInstructionStatus)
		{
			containingExecutionEngine.incrementInstructionMemStall(1); 
//			System.out.println("Incremented Instruction Memory Stall");
		}
		else
		{
			Instruction newInstruction = this.fetchedInstruction;
			this.fetchedInstruction = null;
			this.fetchedInstructionStatus = false;
//			System.out.println(newInstruction.getOperationType()+"is the operation type here");
			long latency;
			if(newInstruction==null || newInstruction.getOperationType() == null) {
				this.fetchedInstruction = null;
				this.fetchedInstructionStatus = false;
				fetchInstruction();
				return;
			}

			switch(newInstruction.getOperationType())
			{
			
				case load:
					latency = OperationLatencyConfig.loadLatency;
					loadaccess++;
					break;
					
				case load_const:
					loadaccess++;
					for(int i =0; i < newInstruction.MemoryAddresses.length; i++)
					{
						boolean memReqIssued = false;
						try{
							if(newInstruction.MemoryAddresses[i] == null)
							{
								break;
							}
							else
							{
								memReqIssued = containingExecutionEngine.gpuMemorySystem.issueRequestToConstantCache(
								RequestType.Cache_Read,	newInstruction.MemoryAddresses[i]);
								if(!memReqIssued)
								{
									System.out.println("Memory request not issuesd");
									System.exit(0);
									
								}
								
							}
						}
							
							
						catch(Exception e)
						{
							e.printStackTrace();
							System.exit(0);
							return;
						}
						containingExecutionEngine.pendingLoads++;
						main.Main.runners[Integer.parseInt(Thread.currentThread().getName())].mem_flag = true;
					}
					latency = Long.MAX_VALUE;
					break;				
				case load_shared:
					loadaccess++;
					for(int i =0; i < newInstruction.MemoryAddresses.length; i++)
					{
						boolean memReqIssued = false;
						try{
							if(newInstruction.MemoryAddresses[i] == null)
							{
								break;
							}
							else
							{
								memReqIssued = containingExecutionEngine.gpuMemorySystem.issueRequestToSharedCache(
								RequestType.Cache_Read,
								newInstruction.MemoryAddresses[i]);
								if(!memReqIssued)
								{
									System.out.println("Memory request not issuesd");
									System.exit(0);
									
								}
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
							System.exit(0);
							return;
						}
						containingExecutionEngine.pendingLoads++;
						main.Main.runners[Integer.parseInt(Thread.currentThread().getName())].mem_flag = true;
					}
					latency = Long.MAX_VALUE;
					break;
				case store:
					storeaccess++;
					latency = OperationLatencyConfig.storeLatency;
					break;
				case store_const:
					storeaccess++;
					for(int i =0; i < newInstruction.MemoryAddresses.length; i++)
					{
						boolean memReqIssued = false;
						try{
							if(newInstruction.MemoryAddresses[i] == null)
							{
								break;
							}
							else
							{
								memReqIssued = containingExecutionEngine.gpuMemorySystem.issueRequestToConstantCache(
								RequestType.Cache_Write,
								newInstruction.MemoryAddresses[i]);
								if(!memReqIssued)
								{
									System.out.println("Memory request not issuesd");
									System.exit(0);
									
								}
							}
						}
						catch(Exception e)
						{e.printStackTrace();
							System.exit(0);
							return;
						}
					}
					latency = OperationLatencyConfig.storeLatency;
					break;
				case store_shared:
					storeaccess++;
					for(int i =0; i < newInstruction.MemoryAddresses.length; i++)
					{
						boolean memReqIssued = false;
						try{
							if(newInstruction.MemoryAddresses[i] == null)
							{
								break;
							}
							else
							{
								memReqIssued = containingExecutionEngine.gpuMemorySystem.issueRequestToSharedCache(
								RequestType.Cache_Write,
								newInstruction.MemoryAddresses[i]);
								System.out.println("Memory Request Issued");
								if(!memReqIssued)
								{
									System.out.println("Memory request not issuesd");
									System.exit(0);
									
								}
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
							System.exit(0);
							return;
						}
					}
					latency = OperationLatencyConfig.storeLatency;
					break;
				case address:
					addressaccess++;
					latency = OperationLatencyConfig.addressLatency;
					break;
				case integerALU:
					intALUaccess++;
					latency = OperationLatencyConfig.intALULatency;
					break;
				case integerMul:
					intMULaccess++;
					latency = OperationLatencyConfig.intMULLatency;
					break;
				case integerDiv:
					intDIVaccess++;
					latency = OperationLatencyConfig.intDIVLatency;
					break;
				case floatALU:
					floatALUaccess++;
					latency = OperationLatencyConfig.floatALULatency;
					break;
				case floatMul:
					floatMULaccess++;
					latency = OperationLatencyConfig.floatMULLatency;
					break;
				case floatDiv:
					floatDIVaccess++;
					latency = OperationLatencyConfig.floatDIVLatency;
					break;
				case predicate:
					predicateaccess++;
					latency = OperationLatencyConfig.predicateLatency;
					break;
				case branch:
					branchaccess++;
					latency = OperationLatencyConfig.branchLatency;
					break;
				case call:
					callaccess++;
					latency = OperationLatencyConfig.callLatency;
					break;
				case Return:
					returnaccess++;
					latency = OperationLatencyConfig.returnLatency;
					break;
				case exit:
					exitaccess++;
					latency = OperationLatencyConfig.exitLatency;
					break;
				default:
					latency = 1;
					break;
			}
			this.scheduleExecuteLatch.add(newInstruction, ArchitecturalComponent.getCores()[this.containingExecutionEngine.containingCore.getTPC_number()][this.containingExecutionEngine.containingCore.getSM_number()].clock.getCurrentTime() + latency);
//		System.out.println("Instruction Scheduled");
		}
		fetchInstruction();
	}
	public GenericCircularQueue<Instruction> getInputToPipeline(){
		return this.inputToPipeline;
	}
	public void setInputToPipeline(GenericCircularQueue<Instruction> inpList){
		this.inputToPipeline = inpList;
	}
	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		
	}

	public void processCompletionOfMemRequest(long requestedAddress) {
	//	System.out.println(this.fetchedInstruction.getCISCProgramCounter() == requestedAddress);
			if(this.fetchedInstruction != null && 
					this.fetchedInstruction.getCISCProgramCounter() == requestedAddress && 
					this.fetchedInstructionStatus==false){
				this.fetchedInstructionStatus=true;
			}
		}

	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{ // Actually Latch Decode Power 
		EnergyConfig decodepower = new EnergyConfig(containingExecutionEngine.getContainingCore().getDecodePower(), numAccesses);
		decodepower.printEnergyStats(outputFileWriter, componentName+".decoder");
		EnergyConfig totalfupower = new EnergyConfig(0, 0);
		EnergyConfig intSppower= new EnergyConfig(0,0);
		// TODO add values here
		EnergyConfig floatSpPower= new EnergyConfig(0,0);
		EnergyConfig ComplexSpPower= new EnergyConfig(0,0);
		totalfupower.add(intSppower);
		totalfupower.printEnergyStats(outputFileWriter, componentName+".totalalu");
		return totalfupower;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
