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
import generic.SM;
import generic.GenericCircularQueue;
import generic.Instruction;
import memorysystem.SMMemorySystem;

public abstract class ExecutionEngine {
	
	protected SM containingCore;
	protected boolean executionComplete;
	protected SMMemorySystem coreMemorySystem;

	private long instructionMemStall;
	
	
	public ExecutionEngine(SM containingCore)
	{
		this.containingCore = containingCore;
		executionComplete = false;
		coreMemorySystem = null;
		instructionMemStall=0;
		
	}
	
	public abstract void setInputToPipeline(GenericCircularQueue<Instruction>[] inpList);

	public void setExecutionComplete(boolean executionComplete) {
		this.executionComplete = executionComplete;
	}

	public boolean isExecutionComplete() {
		return executionComplete;
	}

	public void setCoreMemorySystem(SMMemorySystem coreMemorySystem) {
		this.coreMemorySystem = coreMemorySystem;
	}

	public SMMemorySystem getCoreMemorySystem() {
		return coreMemorySystem;
	}

	public void incrementInstructionMemStall(int i) {
		this.instructionMemStall += i;
		
	}

	public long getInstructionMemStall() {
		return instructionMemStall;
	}

	public SM getContainingCore() {
		return containingCore;
	}
	public abstract EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException;

}
