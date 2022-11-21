/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2010] [Indian Institute of Technology, Delhi]
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

	Contributors:  Eldhose Peter, Rajshekar
*****************************************************************************/
package net;

import generic.CommunicationInterface;
import generic.Event;
import net.NOC.CONNECTIONTYPE;
import main.ArchitecturalComponent;
import config.NocConfig;
import config.SystemConfig;
import dram.MainMemoryDRAMController;
/*****************************************************
 * 
 * NocInterface to make the router generic
 *
 *****************************************************/
public class NocInterface implements CommunicationInterface{
	/*
	 * Messages are coming from simulation elements(cores, cache banks) in order to pass it to another through NOC.
	 */
	Router router;
	
public NocInterface(NocConfig nocConfig) {
		
		super();
	
		if(SystemConfig.nocConfig.ConnType == CONNECTIONTYPE.OPTICAL){
			nocConfig.latency  =0;
			//System.out.println("Optical router has been associated");
		this.router = new OpticalRouter(nocConfig, this);
		}
		else{
		this.router = new Router(nocConfig, this);}
	}
	
	public void sendMessage(Event event) {
		if(SystemConfig.nocConfig.ConnType == CONNECTIONTYPE.OPTICAL)
		{
			//System.out.println("Optical Working");
			this.getRouter().handleEvent(event.getEventQ(), event);
		}
		else{
			//System.out.println("YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
			event.update(event.getRequestingElement(), this.getRouter(), event.getRequestingElement(), event.getProcessingElement());
			this.getRouter().getPort().put(event);
		}
	}
	
	public Router getRouter(){
		return this.router;
	}
	
	public void setId(ID id)
	{
		getRouter().setID(id);
	}
	
	public ID getId()
	{
		return getRouter().getID();
	}

	public MainMemoryDRAMController getNearestMemoryController() {
		return ((NOC)ArchitecturalComponent.getInterConnect()).getNearestMemoryController(getId());		
	}
}
