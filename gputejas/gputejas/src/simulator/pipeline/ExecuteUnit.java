package pipeline;

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
import java.io.FileWriter;
import java.io.IOException;

import config.EnergyConfig;
import main.ArchitecturalComponent;
import generic.Event;
import generic.EventQueue;
import generic.Instruction;
import generic.OperationType;
import generic.PortType;
import generic.SM;
import generic.SimulationElement;

public class ExecuteUnit extends SimulationElement{
	SM sm;
	GPUExecutionEngine containingExecutionEngine;
	
	StageLatch_MII scheduleExecuteLatch;

	public ExecuteUnit(SM sm, EventQueue eventQueue, GPUExecutionEngine execEngine) {
		super(PortType.Unlimited, -1, -1, eventQueue, -1, -1);
		this.sm = sm;
		this.containingExecutionEngine = execEngine;
		this. scheduleExecuteLatch = execEngine.getScheduleExecuteLatch();
	}

	public void performExecute(GPUpipeline inorderPipeline)
	{
//		System.out.println(scheduleExecuteLatch.isEmpty()+"execution is empty");
		if(scheduleExecuteLatch.isEmpty() == true)
			return;
		Instruction ins = scheduleExecuteLatch.peek(0);
		if(ins == null)
		{
			return;
		}
		scheduleExecuteLatch.poll();
		if(ins.type == OperationType.load_const || ins.type == OperationType.load_shared || 
				ins.type == OperationType.store_const || ins.type == OperationType.store_shared )
		{
			main.Main.runners[Integer.parseInt(Thread.currentThread().getName())].mem_flag = false;
		}
		if(ins.type == OperationType.inValid)
		{
			containingExecutionEngine.setExecutionComplete(true);
			
		}
		
		
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		
	}
	
	public void processCompletionOfMemRequest(long requestedAddress) {
		containingExecutionEngine.pendingLoads--;
		if(this.scheduleExecuteLatch != null &&  this.scheduleExecuteLatch.instructions[0] != null &&
				this.scheduleExecuteLatch.instructionCompletesAt[0] > ArchitecturalComponent.getCores()[sm.getTPC_number()][sm.getSM_number()].clock.getCurrentTime())
		{
			if(containingExecutionEngine.pendingLoads <= 0)
			{
				this.scheduleExecuteLatch.instructionCompletesAt[0] = ArchitecturalComponent.getCores()[sm.getTPC_number()][sm.getSM_number()].clock.getCurrentTime();
			}
		}
	}
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		long numResultsBroadCastBusAccess=0;
		EnergyConfig power = new EnergyConfig(sm.getResultsBroadcastBusPower(), numResultsBroadCastBusAccess);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}

}

