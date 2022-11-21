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

import main.ArchitecturalComponent;
import generic.Instruction;
import generic.SM;

public class StageLatch_MII {
	SM sm;
	Instruction[] instructions;
	long instructionCompletesAt[];	//used to indicate when the corresponding instruction is ready for
									//consumption by the next stage;
									//a long is used instead of a boolean because a boolean would require
									//modeling the completion of execution at FUs through events,
									//which would slow down simulation
	
	int size;
	int head;
	int tail;
	int curSize;
	
	public StageLatch_MII(int size,SM sm)
	{
		this.sm=sm;
		this.size = size;
		instructions = new Instruction[size];
		instructionCompletesAt = new long[size];
		head = -1;
		tail = -1;
		curSize = 0;
	}

	public boolean isFull()
	{
		if(curSize >= size)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isEmpty()
	{
		if(curSize <= 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void add(Instruction newInstruction, long instCompletesAt)
	{
		if(tail == -1)
		{
			head = 0;
			tail = 0;
		}
		else
		{
			tail = (tail + 1)%size;
		}
		
		instructions[tail] = newInstruction;
		instructionCompletesAt[tail] = instCompletesAt;
		curSize++;
	}
	
	public Instruction peek(int pos)
	{
		if(curSize <= pos)
		{
			return null;
		}
		
		int retPos = (head + pos) % size;
		
		if(instructionCompletesAt[retPos] >ArchitecturalComponent.getCores()[sm.getTPC_number()][sm.getSM_number()].clock.getCurrentTime())
		{
			return null;
		}
		
		return instructions[retPos];
	}
	
	public Instruction poll()
	{
		if(curSize <= 0)
		{
			return null;
		}
		
		Instruction toBeReturned = instructions[head];
		if(instructionCompletesAt[head] > ArchitecturalComponent.getCores()[sm.getTPC_number()][sm.getSM_number()].clock.getCurrentTime())
		{
			return null;
		}
		instructions[head] = null;
		
		if(head == tail)
		{
			head = -1;
			tail = -1;
		}
		else
		{
			head = (head + 1) % size;
		}		
		curSize--;
		
		return toBeReturned;
	}

	public Instruction[] getInstructions() {
		return instructions;
	}

	public long[] getInstructionCompletesAt() {
		return instructionCompletesAt;
	}
	
	public long getInstructionCompletesAt(Instruction ins)
	{
		for(int i = 0; i < size; i++)
		{
			if(instructions[i] == ins)
			{
				return instructionCompletesAt[i];
			}
		}
		return -1;
	}
	
}
