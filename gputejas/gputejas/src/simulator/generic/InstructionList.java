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



public class InstructionList 
{
	private GenericCircularQueue<Instruction> instructionQueue = null;
	
	public InstructionList(int initSize)
	{
		instructionQueue = new GenericCircularQueue<Instruction>(Instruction.class, initSize);
	}

	//appends a single instruction to the instruction list
	public void appendInstruction(Instruction newInstruction)
	{
		instructionQueue.enqueue(newInstruction);
	}
	
	public boolean isEmpty()
	{
		return instructionQueue.isEmpty();
	}
	
	public Instruction get(int index)
	{
		if(index >= instructionQueue.size())
		{
			return null;
		}
		else
		{
			return instructionQueue.peek(index);
		}
	}
	
	public void printList() 
	{
		for(int i = 0; i< instructionQueue.size(); i++)
		{
			System.out.print(instructionQueue.peek(i).toString() + "\n");
		}
	}
	
	public int getListSize()
	{
		return instructionQueue.size();
	}
	
	public Instruction peekInstructionAt(int position)
	{
		return instructionQueue.peek(position);
	}
	public int length()
	{
		return instructionQueue.size();
	}

	public void clear() {
		instructionQueue.clear();
	}
}