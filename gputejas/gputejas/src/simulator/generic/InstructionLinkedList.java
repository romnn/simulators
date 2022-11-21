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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;

public class InstructionLinkedList implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public LinkedList<Instruction> instructionLinkedList;
	private ListIterator<Instruction> listIterator;
	
	public InstructionLinkedList()
	{
		instructionLinkedList = new LinkedList<Instruction>();
		listIterator = instructionLinkedList.listIterator();
	}

	//appends a single instruction to the instruction list
	public void appendInstruction(Instruction instruction)
	{
		instructionLinkedList.add(instruction);
	}
	
	// appends multiple instructions at the end of InstructionLinkedList
	public void appendInstruction(InstructionLinkedList instructionLinkedList)
	{
		this.instructionLinkedList.
					addAll(instructionLinkedList.instructionLinkedList);
	}
	
	public boolean isEmpty()
	{
		return instructionLinkedList.isEmpty();
	}
	
	public void printList() 
	{
		for(int i = 0; i< instructionLinkedList.size(); i++)
		{
			System.out.print(instructionLinkedList.get(i).toString() + "\n");
		}
	}

	public Instruction getNextInstruction()
	{
		if(listIterator.hasNext())
		{
			return listIterator.next(); 
		}
		else 
		{
			return null;
		}
	}

	public Instruction peekInstructionAt(int position)
	{
		return instructionLinkedList.get(position);
	}

	public Instruction pollFirst()
	{
		return instructionLinkedList.pollFirst();
	}

	public int getListSize()
	{
		return instructionLinkedList.size();
	}

	public int length()
	{
		return instructionLinkedList.size();
	}
	
	public void removeInstructionAt(int i)
	{
		instructionLinkedList.remove(i);
	}
}
