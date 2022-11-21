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
package generic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Vector;

import config.EnergyConfig;
import config.SmConfig;

import dram.MainMemoryDRAMController;

import main.ArchitecturalComponent;
import memorysystem.AddressCarryingEvent;
import memorysystem.MemorySystem;
import pipeline.GPUExecutionEngine;
import pipeline.GPUpipeline;
//import pipeline.multi_issue_inorder.MultiIssueInorderExecutionEngine;

/**
 * represents a single core
 * has it's own clock, and comprises of an execution engine and an event queue
 * all core parameters are defined here
 */

public class SM extends SimulationElement{
	
	public int supported_blocks = 1;
	
	Port port;
	GPUExecutionEngine execEngine;
	public EventQueue eventQueue;
	public int currentBlocks;
	private int tpc_number;
	private int sm_number;
	private long coreCyclesTaken;
	private long noOfInstructionsExecuted;
	//TODO

	private pipeline.GPUpipeline pipelineInterface;

//	
	public LocalClockperSm clock;
	public SM(int tpc_number, int sm_number)
	{
		super(PortType.Unlimited, -1, -1, -1);	
		this.port = new Port(PortType.Unlimited, -1, -1);
		this.eventQueue = new EventQueue(tpc_number,sm_number);
		this.tpc_number = tpc_number;
		this.sm_number = sm_number;
		this.clock=new LocalClockperSm(tpc_number,sm_number);
		this.clock.setStepSize(1);
		this.currentBlocks =0;
		this.execEngine = new GPUExecutionEngine(this);
		this.pipelineInterface = new GPUpipeline(this, eventQueue);
		this.noOfInstructionsExecuted = 0;
		
	}
		
	
	public EventQueue getEventQueue() {
		return eventQueue;
	}
	
	public void setEventQueue(EventQueue _eventQueue) {
		eventQueue = _eventQueue;
	}

	public GPUExecutionEngine getExecEngine() {
		return execEngine;
	}

	
	
	public int getTPC_number() {
		return tpc_number;
	}

	public int getSM_number() {
		return sm_number;
	}
	
	public long getNoOfInstructionsExecuted() {
		return noOfInstructionsExecuted;
	}

	public void setNoOfInstructionsExecuted(long noOfInstructionsExecuted) {
		this.noOfInstructionsExecuted = noOfInstructionsExecuted;
	}
	
	public void incrementNoOfInstructionsExecuted()
	{
		this.noOfInstructionsExecuted++;
	}

	public pipeline.GPUpipeline getPipelineInterface() {
		return pipelineInterface;
	}
	
	public void setInputToPipeline(GenericCircularQueue<Instruction>[] inputsToPipeline)
	{
		this.getExecEngine().setInputToPipeline(inputsToPipeline);
	}
	
	public void setStepSize(int stepSize)
	{
		this.stepSize = stepSize;
		
	}

	public long getCoreCyclesTaken() {
		return coreCyclesTaken;
	}

	public void setCoreCyclesTaken(long coreCyclesTaken) {
		this.coreCyclesTaken = coreCyclesTaken;
	}
	
	
	public int getStepSize()
	{
		return stepSize;
	}
	@Override
	public Port getPort() {
		// TODO Auto-generated method stub
		return port;
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) 
	{
		
		System.out.println("Handled by SM");
		
		if (event.getRequestType() == RequestType.Main_Mem_Read ||  event.getRequestType() == RequestType.Main_Mem_Write )
		{
			
			this.handleMemoryReadWrite(eventQ,event);
			
		}
		else if (event.getRequestType() == RequestType.Main_Mem_Response )
		{
			//System.out.println("inside sm handle event");
			handleMainMemoryResponse(eventQ, event);
		}
		else 
		{
			System.err.println(event.getRequestType());
			misc.Error.showErrorAndExit(" unexpected request came to cache bank");
		}
	}	
	
	
// FOR Main Mem Read and Write Requests Transferred to MainMemoryDRAMController
	
	
	protected void handleMemoryReadWrite(EventQueue eventQ, Event event) 
    {
		System.out.println("Handled by SM - In Function handleMemoryReadWrite");
		//System.out.println(((AddressCarryingEvent)event).getDestinationBankId() + ""+ ((AddressCarryingEvent)event).getSourceBankId());
		AddressCarryingEvent addrEvent = (AddressCarryingEvent) event;
		Vector<Integer> sourceId = addrEvent.getSourceId(); 
		Vector<Integer> destinationId = ((AddressCarryingEvent)event).getDestinationId();
		RequestType requestType = event.getRequestType();
		
		ArchitecturalComponent.memoryControllers.get(0).getPort().put(((AddressCarryingEvent)event).updateEvent(eventQ,ArchitecturalComponent.memoryControllers.get(0).getLatencyDelay(), this, ArchitecturalComponent.memoryControllers.get(0), requestType, sourceId, destinationId));

	}
	@SuppressWarnings("unused")
	protected void handleMainMemoryResponse(EventQueue eventQ, Event event) 
	{
		AddressCarryingEvent addrEvent = (AddressCarryingEvent) event;

		

	}
	
	public void activatePipeline(){
		this.pipelineInterface.resumePipeline();
	}

	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig totalPower = new EnergyConfig(0, 0);
		
		if(coreCyclesTaken == 0)
		{
			outputFileWriter.write("coreCyclesTaken is shown to zero\n");
		}
		
		outputFileWriter.write("\n\n");
		
		// --------- Core Memory System -------------------------
		EnergyConfig iCachePower =  this.execEngine.getCoreMemorySystem().getiCache().calculateAndPrintEnergy(outputFileWriter, componentName + ".iCache");
		totalPower.add(totalPower, iCachePower);
			
		EnergyConfig dCachePower =  this.execEngine.getCoreMemorySystem().getConstantCache().calculateAndPrintEnergy(outputFileWriter, componentName + ".dCache");
		totalPower.add(totalPower, dCachePower);
			// -------- Pipeline -----------------------------------
		EnergyConfig pipelinePower =  this.execEngine.calculateAndPrintEnergy(outputFileWriter, componentName + ".pipeline");
		totalPower.add(totalPower, pipelinePower);
		
		totalPower.printEnergyStats(outputFileWriter, componentName + ".total");
		
		return totalPower;
	}
	public void sleepPipeline() {
		// TODO Auto-generated method stub
		//((MultiIssueInorderExecutionEngine)this.getExecEngine()).getFetchUnitIn().inputToPipeline.enqueue(Instruction.getSyncInstruction());
	}


	public EnergyConfig getDecodePower() {
		return SmConfig.decodePower;
	}

		public EnergyConfig getResultsBroadcastBusPower() {
		return SmConfig.resultsBroadcastBusPower;
	}


		public EnergyConfig getAllocPower() {
			// TODO Auto-generated method stub
			return SmConfig.AllocDeallocPower;
		}


		public EnergyConfig getArbiterPower() {
			// TODO Auto-generated method stub
			return SmConfig.ArbiterPower;
		}


		public EnergyConfig getCollectorPower() {
			// TODO Auto-generated method stub
			return SmConfig.CollectorUnitsDecodePower;
		}


		public EnergyConfig getDispatchPower() {
			// TODO Auto-generated method stub
			return SmConfig.DispatchPower;
		}


		public EnergyConfig getScoreboardPower() {
			// TODO Auto-generated method stub
			return SmConfig.ScoreBoardPower;
		}


		public EnergyConfig getBankPower() {
			// TODO Auto-generated method stub
			return SmConfig.BankPower;
		}

	


	}

