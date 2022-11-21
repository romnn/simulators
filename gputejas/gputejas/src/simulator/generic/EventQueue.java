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

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

import main.ArchitecturalComponent;
import dram.MainMemoryDRAMController;

import main.ArchitecturalComponent;
import memorysystem.MemorySystem;

public class EventQueue 
{
	private PriorityBlockingQueue<Event> priorityQueue;
	int tpcId, smId;
	public EventQueue(int tpcId, int smId) 
	{
		this.tpcId=tpcId;
		this.smId=smId;
		priorityQueue = new PriorityBlockingQueue<Event>(1, new EventComparator());
	}
	
	public void addEvent(Event event)
	{
		if( priorityQueue.add(event) == false) {
			Event newEvent = event.clone();
			priorityQueue.add(newEvent);
		}
	}
	
	public void processEvents()
	{
		Event event;
		long eventTime;
		long currentClockTime = ArchitecturalComponent.getCores()[tpcId][smId].clock.getCurrentTime();
		while(!priorityQueue.isEmpty())
		{
//			System.out.println("event queue size = " + priorityQueue.size());
			eventTime = priorityQueue.peek().getEventTime();
			if (eventTime <= currentClockTime)
			{
			event = priorityQueue.remove();
//				System.out.println(event.getProcessingElement()+" is req. elem. and "+event.getRequestType());
				event.getProcessingElement().handleEvent(this, event);
			}
			else
			{
				break;
			}
		}
	}
	
	public boolean isEmpty()
	{
		return priorityQueue.isEmpty();
	}
	
	public void dump()
	{
		System.out.println("------------------------------------------------------------------------------------");
		System.out.println("event queue size = " + priorityQueue.size());
		Iterator<Event> iterator = priorityQueue.iterator();
		while(iterator.hasNext())
		{
			Event event = iterator.next();			
			event.dump();
		}
		System.out.println("------------------------------------------------------------------------------------");
	}
}