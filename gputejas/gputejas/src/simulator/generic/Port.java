package generic;
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

public class Port 
{
	private PortType portType;
	private int noOfPorts;

	//occupancy defines the number of clockCycles needed for  
	//a single transfer on the port.
	private long occupancy;
	private long[] portBusyUntil;
	
	//NOTE : Time is in terms of GlobalClock cycles
	
	public Port(PortType portType, int noOfPorts, long occupancy)
	{
		this.portType = portType;
		
		//initialise no. of ports and the occupancy.
		if(portType==PortType.Unlimited)
		{
			return;
		}
		
		else if(portType!=PortType.Unlimited && 
				noOfPorts>0 && occupancy>0)
		{
			// For a FCFS or a priority based port, noOfPorts and
			// occupancy must be non-zero.
			this.noOfPorts = noOfPorts;
			this.occupancy = occupancy;
			
			//set busy field of all ports to 0
			portBusyUntil = new long[noOfPorts];
					
			for(int i=0; i < noOfPorts; i++)
			{
				this.portBusyUntil[i] = 0;
			}
		}
		
		else
		{
			// Display an error for invalid initialization.
			misc.Error.showErrorAndExit("Invalid initialization of port !!\n" +
					"port-type=" + portType + " no-of-ports=" + noOfPorts + 
					" occupancy=" + occupancy);
		}
	}
	
	public void put(Event event)
	{
		//overloaded method.
		this.put(event, 1);
	}
	
	public void put(Event event, int noOfSlots)
	{
		if(this.portType == PortType.Unlimited)
		{
//			event.addEventTime(ArchitecturalComponent.getCores()[event.tpcId][event.smId].clock.getCurrentTime());
			event.addEventTime(GlobalClock.getCurrentTime());
			event.getEventQ().addEvent(event);
			return;
		}
		
		else if(this.portType==PortType.FirstComeFirstServe)
		{
			//else return the slot that will be available earliest.
			int availablePortID = 0;
			
			// find the first available port
			for(int i=0; i<noOfPorts; i++)
			{
				if(portBusyUntil[i]< 
					portBusyUntil[availablePortID])
				{
					availablePortID = i;
				}
			}
			
			// If a port is available, set its portBusyUntil field to
			// current time
	//		System.out.println("tpc "+event.tpcId+" sm id "+event.smId);
		//	if(portBusyUntil[availablePortID]<
		//			ArchitecturalComponent.getCores()[event.tpcId][event.smId].clock.getCurrentTime())
	//		{
				// this port will be busy for next occupancy cycles
		//		portBusyUntil[availablePortID] = 
	//					ArchitecturalComponent.getCores()[event.tpcId][event.smId].clock.getCurrentTime() + occupancy;
	//		}else{
				// set the port as busy for occupancy cycles
				portBusyUntil[availablePortID] += occupancy;	
//			}
						
			// set the time of the event
			event.addEventTime(portBusyUntil[availablePortID]);
			
			// add event in the eventQueue
			event.getEventQ().addEvent(event);
		}
	}

	public int getNoOfPorts() {
		return noOfPorts;
	}

	public PortType getPortType() {
		return portType;
	}
}