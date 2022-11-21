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

	Contributors:  Eldhose Peter
*****************************************************************************/
package net;

import generic.Event;
import generic.EventQueue;
import generic.RequestType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import main.ArchitecturalComponent;
import memorysystem.AddressCarryingEvent;
import net.NOC.TOPOLOGY;
import net.RoutingAlgo.SELSCHEME;
import config.EnergyConfig;
//import config.EnergyConfig;
import config.NocConfig;
import config.SystemConfig;

public class Router extends Switch{
	protected RoutingAlgo routingAlgo = new RoutingAlgo();
	protected int numberOfRows;
	protected int numberOfColumns;
	private ID id;
	protected int latencyBetweenNOCElements;
	protected Vector<Router> neighbours;
	//EnergyConfig power;
	public static int incoming=0;
	public static int outgoing=0;	
	
	/************************************************************************
     * Method Name  : Router
     * Purpose      : Constructor for Router class
     * Parameters   : bank id, noc configuration, cache bank reference
     * Return       : void
     *************************************************************************/
	
	public Router(NocConfig nocConfig, NocInterface reference)
	{
		super(nocConfig);
		this.topology = nocConfig.topology;
		this.rAlgo = nocConfig.rAlgo;
		this.numberOfRows = nocConfig.numberOfRows;
		this.numberOfColumns = nocConfig.numberOfColumns;
		this.latencyBetweenNOCElements = nocConfig.latencyBetweenNOCElements;
		this.neighbours= new Vector<Router>(4);
		this.hopCounters = 0;
		//power = nocConfig.power;
		
		ArchitecturalComponent.addNOCRouter(this);
	}
	/***************************************************
	 * Connects the banks
	 * @param dir
	 * @param networkElements
	 ***************************************************/
	public void SetConnectedNOCElements(RoutingAlgo.DIRECTION dir,NocInterface networkElements)
	{
		this.neighbours.add(dir.ordinal(), networkElements.getRouter());
	}
	/***************************************************
	 * Connects the banks
	 * @param dir
	 ***************************************************/
	public void SetConnectedNOCElements(RoutingAlgo.DIRECTION dir)
	{
		this.neighbours.add(dir.ordinal(), null);
	}
	public Vector<Router> GetNeighbours()
	{
		return this.neighbours;
	}
	/*****************************************************
	 * Check if the neighbour buffer has free entry
	 * reqOrReply is kept for future use
	 * @param nextId
	 * @param reqOrReply
	 * @return
	 *****************************************************/
	public boolean CheckNeighbourBuffer(RoutingAlgo.DIRECTION nextId,boolean reqOrReply)  //request for neighbour buffer
	{
		return ((Router) this.neighbours.elementAt(nextId.ordinal())).AllocateBuffer(nextId);
	}
	
	/***************************************************************************************
     * Method Name  : RouteComputation
     * Purpose      : computing next bank id,Adaptive algorithm selects less contention path
     * Parameters   : current and destination bank id
     * Return       : next bank id
     ***************************************************************************************/
	public RoutingAlgo.DIRECTION RouteComputation(ID current,ID destination)
	{ 
		//find the route to go
		Vector<RoutingAlgo.DIRECTION> choices = new Vector<RoutingAlgo.DIRECTION>();
		switch (rAlgo) {
		case WESTFIRST :
			choices = routingAlgo.WestFirstnextBank(current, destination,this.topology,this.numberOfRows,this.numberOfColumns);
			break;
		case NORTHLAST : 
			choices = routingAlgo.NorthLastnextBank(current, destination,this.topology,this.numberOfRows,this.numberOfColumns);
			break;
		case NEGATIVEFIRST :
			choices = routingAlgo.NegativeFirstnextBank(current, destination,this.topology,this.numberOfRows,this.numberOfColumns);
			break;
		case TABLE :
		break;
		case SIMPLE :
			choices = routingAlgo.XYnextBank(current, destination,this.topology,this.numberOfRows,this.numberOfColumns);
			//System.out.println(choices);
			break;
		}
		if(selScheme == SELSCHEME.ADAPTIVE && choices.size()>1)
		{
			if(((Router)this.neighbours.elementAt(choices.elementAt(0).ordinal())).bufferSize()>
			   ((Router)this.neighbours.elementAt(choices.elementAt(1).ordinal())).bufferSize())
				return choices.elementAt(0);
			else
				return choices.elementAt(1);
		}
		return choices.elementAt(0);
	}
	
	public boolean reqOrReply(ID currentId, ID destinationId) {
		if(currentId.getx() < destinationId.getx())
		{
			if(currentId.gety() < destinationId.gety())
			{
				return false;//for reply messages
			}
			else
			{
				return true;//for incoming messages
			}
		}
		else
		{
			if(currentId.gety() < destinationId.gety())
			{
				return false;//for reply messages
			}
			else
			{
				return true;//for incoming messages
			}
		}
	}
	/************************************************************************
     * Method Name  : handleEvent
     * Purpose      : handle the event request and service it
     * Parameters   : eventq and event id
     * Return       : void
     *************************************************************************/

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		// TODO Auto-generated method stub
		RoutingAlgo.DIRECTION nextID;
		boolean reqOrReply;
		ID currentId = id;
		ID destinationId = ((NocInterface)event.getActualProcessingElement().getComInterface()).getId();
		RequestType requestType = event.getRequestType();
		
		event.setEventTime(0);
				
		if((topology == TOPOLOGY.OMEGA || topology == TOPOLOGY.BUTTERFLY || topology == TOPOLOGY.FATTREE)
				&& !currentId.equals(destinationId))  //event passed to switch in omega/buttrfly/fat tree connection
		{
			this.hopCounters++;
			((AddressCarryingEvent)event).hopLength++;
			
			this.connection[0].getPort().put(
					event.update(
							eventQ,
							0,	//this.getLatency()
							this, 
							this.connection[0],
							requestType));
		}
        //If this is the destination
		else if(currentId.equals(destinationId))  
		{
			event.update(event.getActualRequestingElement(), event.getActualProcessingElement());
			event.getProcessingElement().getPort().put(event);
			this.FreeBuffer();
		}
		//If this event is just entering NOC, then allocate buffer for it
		else if(event.getRequestingElement().getClass() != Router.class){ 
            if(this.AllocateBuffer())
            {
                event.setRequestingElement(this);
                handleEvent(eventQ, event);
            }
            else //post event to this ID
            {            
                this.getPort().put(event);
            }
        }
		else
		{
			nextID = this.RouteComputation(currentId, destinationId);
			reqOrReply = reqOrReply(currentId, destinationId);              // To avoid deadlock
			//If buffer is available forward the event
			if(this.CheckNeighbourBuffer(nextID,reqOrReply))   
			{
				//post event to nextID
				this.hopCounters++;
				((AddressCarryingEvent)event).hopLength++;
				this.GetNeighbours().elementAt(nextID.ordinal()).getPort().put(
						event.update(
								eventQ,
								latencyBetweenNOCElements,        	//this.getLatency()
								this, 
								this.GetNeighbours().elementAt(nextID.ordinal()),
								requestType));
				this.FreeBuffer();
			}
			//If buffer is not available in next router keep the message here itself
			else                                              
			{	//post event to this ID
				this.getPort().put(	event.update(this,this));
			}
		}
	}
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		if(hopCounters == 0)
		{
			return new EnergyConfig(0, 0);
		}
		EnergyConfig power = new EnergyConfig(SystemConfig.nocConfig.power, hopCounters);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}
	
	public EnergyConfig calculateEnergy() {
		if(hopCounters == 0)
		{
			return new EnergyConfig(0, 0);
		}
		EnergyConfig power = new EnergyConfig(SystemConfig.nocConfig.power, hopCounters);
		return power;
	}
	public void setID(ID id) 
	{
		this.id = id.clone();
	}
	
	public ID getID() {
		return id;
	}
}