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

import memorysystem.Cache;
import net.BusInterface;
import main.ArchitecturalComponent;
public abstract class SimulationElement implements Cloneable
{
	//a simulation element encapsulates a port.
	//all the request for the port are ported through simulationElement
	Port port;
	protected long latency;
	protected int stepSize = 1;
	CommunicationInterface comInterface;


   public Object clone()
    {
        try
        {
            // call clone in Object.
            return super.clone();
        } catch(CloneNotSupportedException e)
        {
            System.out.println("Cloning not allowed.");
            return this;
        }
    }

	
	public SimulationElement(PortType portType,	int noOfPorts,	long occupancy,long latency)
	{
		this.port = new Port(portType, noOfPorts, occupancy);
		this.latency = latency;
	}
	public SimulationElement(PortType portType, 
			int noOfPorts, long occupancy, long latency, long frequency	)
		{
			this.port = new Port(portType, noOfPorts, occupancy);//to be added latency in ports in gpu not present but present in tejas
			this.latency = latency;
		}
	
	public SimulationElement(PortType portType,int noOfPorts,long occupancy,EventQueue eq,long latency,long frequency)
	{
		this.port = new Port(portType, noOfPorts, occupancy);
		this.latency = latency;
	}
	
	//To get the time delay(due to latency) to schedule the event 
	public long getLatencyDelay()
	{
		return (this.latency /** this.stepSize*/);
	}
	
	public long getLatency() 
	{
		return this.latency;
	}
	public void setComInterface(CommunicationInterface comInterface) {
		this.comInterface = comInterface;
	}
	
	public CommunicationInterface getComInterface() {
		
		if (comInterface==null)
			{ 
			System.out.println("comInterface is null");
			//BusInterface busInterface;
			//busInterface = new BusInterface(ArchitecturalComponent.bus);
		//	setComInterface(busInterface);
			}
	
		return comInterface;
	}
	
	protected void setLatency(long latency) {
		this.latency = latency;
	}

	public Port getPort()
	{
		return this.port;
	}	
	
	public void setPort(Port port){
		this.port = port;
	}
	public abstract void handleEvent(EventQueue eventQ, Event event);
	public void sendEvent(Event event) {
		if (event.getEventTime() != 0) {
			misc.Error.showErrorAndExit("Send event with zero latency !!");
		}

		if (event.getProcessingElement().getComInterface() != this.getComInterface()) {
			getComInterface().sendMessage(event);
//			System.out.println("Message not going through bus"+event.getProcessingElement().getComInterface()+ "  "+this.getComInterface());
		} 
	else {
			event.getProcessingElement().getPort().put(event);
		}
	}
}