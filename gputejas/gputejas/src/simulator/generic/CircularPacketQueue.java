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

import emulatorinterface.communication.Packet;
import emulatorinterface.translator.x86.instruction.InstructionClass;
import emulatorinterface.translator.x86.instruction.FullInstructionClass;

public class CircularPacketQueue {
	int head;
	int tail;
	int bufferSize;
	Packet buffer[];
	
	public CircularPacketQueue(int bufferSize)
	{
		this.bufferSize = bufferSize;	
		head = tail = -1;
		buffer = new Packet[bufferSize];
		
		for(int i=0; i<bufferSize; i++) {
			buffer[i] = new Packet();
		}
	}
	//returns true if enqueue succeeds
	public boolean enqueue(FullInstructionClass iClass,Integer ip, Long MemoryAddresses[])
	{
		if(isFull())
		{
			System.out.println("can't enqueue - queue full");
		}
		
		tail = (tail+1)%bufferSize;
		buffer[tail].set(iClass, ip,MemoryAddresses);
		if(head == -1)
		{
			head = 0;
		}
		return true;
	}
	
	public Packet dequeue()
	{
		if(isEmpty())
		{
			System.out.println("can't dequeue - queue empty");
			return null;
		}
		
		Packet toBeReturned = buffer[head];
		if(head == tail)
		{
			head = -1;
			tail = -1;
		}
		else
		{
			head = (head + 1)%bufferSize;
		}
		return toBeReturned;
	}
	
	//position refers to logical position in queue - NOT array index
	public Packet peek(int position)
	{
		if(size() <= position)
		{
			return null;
		}
		
		int peekIndex = (head + position)%bufferSize;
		return buffer[peekIndex];
	}
	
	public boolean isFull()
	{
		if((tail + 1)%bufferSize == head)
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
		if(head == -1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int size()
	{
		if(head == -1)
		{
			return 0;
		}
		if(head <= tail)
		{
			return (tail - head + 1);
		}
		else
		{
			return (bufferSize - head + tail + 1);
		}
	}
	
	public Packet pollFirst()
	{
		return dequeue();
	}
	
	public void clear()
	{
		head = -1;
		tail = -1;
	}
	
	public Packet pop()
	{
		if(size() <= 0)
		{
			return null;
		}
		
		Packet toBeReturned = buffer[tail];
		// buffer[tail] = null; setting to null is not needed.
		if(head == tail)
		{
			head = -1;
			tail = -1;
		}
		else if(tail == 0)
		{
			tail = bufferSize - 1;
		}
		else
		{
			tail = tail - 1;
		}
		
		//System.out.println("pop : " + head + " - " + tail);
		
		return toBeReturned;
	}

	@Override
	public String toString() {
		String str = "";
		if(head != -1)
		{
			for(int i = head; i <= tail; i = (i+1)%bufferSize)
			{
				str = str + buffer[i] + "\n";
			}
		}
		return str;
	}

	public int spaceLeft() {
		return this.bufferSize - this.size();
	}
}
