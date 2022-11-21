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

import java.util.Vector;

import memorysystem.AddressCarryingEvent;
import net.NOC.TOPOLOGY;
import net.RoutingAlgo.ALGO;
import net.RoutingAlgo.DIRECTION;
import net.RoutingAlgo.SELSCHEME;

import config.NocConfig;
import generic.Event;
import generic.EventQueue;
import generic.RequestType;
import generic.SimulationElement;

public class Switch extends SimulationElement{
	
	protected SELSCHEME selScheme;
	protected Switch connection[];
	protected int range[];
	protected int level;
	protected int numColumns;
	public TOPOLOGY topology;
	public ALGO rAlgo;
	protected int availBuff;           //available number of buffers
	public int hopCounters;
	public static int totalBufferAccesses;
	//0 - up ; 1 - right ; 2- down ; 3- left (clockwise) 
	
	public Switch(NocConfig nocConfig,int level){
		super(nocConfig.portType,nocConfig.getAccessPorts(), nocConfig.getPortOccupancy(),nocConfig.getLatency(), nocConfig.operatingFreq);
		this.selScheme = nocConfig.selScheme;
		this.connection = new Switch[4];
		this.level = level; //used in omega network
		this.numColumns = nocConfig.numberOfColumns;
		this.topology = nocConfig.topology;
		this.rAlgo = nocConfig.rAlgo;
		this.availBuff = nocConfig.numberOfBuffers;
		this.hopCounters = 0;
		
	}
	public Switch(NocConfig nocConfig){
		super(nocConfig.portType,
				nocConfig.getAccessPorts(), 
				nocConfig.getPortOccupancy(),
				nocConfig.getLatency(),
				nocConfig.operatingFreq);
		this.selScheme = nocConfig.selScheme;
		this.connection = new Switch[4];
		this.availBuff = nocConfig.numberOfBuffers;
		this.range = new int[2]; // used in fat tree
		this.hopCounters = 0;
	}
	
	public int nextIdbutterflyOmega(String binary)
	{
		if(binary.charAt(level) == '0')
			return 2;
		else
			return 3;
	}
	
	public int nextIdFatTree(int elementNumber)
	{
		if(elementNumber < range[0] || elementNumber > range[1])
			return 0;
		else
		{
			if((range[0] + range[1])/2 < elementNumber)
				return 1;
			else
				return 3;
		}
	}
	/************************************************************************
     * Method Name  : AllocateBuffer
     * Purpose      : check whether buffer available
     * Parameters   : none
     * Return       : true if allocated , false if no buffer available
     *************************************************************************/
	public boolean AllocateBuffer()  // reqOrReplay = true=>incoming false=>outgoing 
	{
		if(this.availBuff>2)     //incoming request leave atleast one buff space
			{						 //for outgoing request to avoid deadlock
				this.availBuff --;
				totalBufferAccesses++;
				return true;
			}
		return false;
	}
	/*******************************************************
	 * Allocates buffer by checking the direction of the request
	 * Giving priority to the outgoing request
	 * To avoid deadlock
	 * @param nextId
	 * @return
	 *******************************************************/
	public boolean AllocateBuffer(DIRECTION nextId)  // reqOrReplay = true=>incoming false=>outgoing 
	{
		if(this.availBuff>2)     //incoming request leave atleast one buff space
		{						 //for outgoing request to avoid deadlock
			this.availBuff --;
			totalBufferAccesses++;
			return true;
		}
		else{
			if(nextId == DIRECTION.UP){
				if(this.availBuff>1)     //incoming request leave atleast one buff space
				{						 //for outgoing request to avoid deadlock
					this.availBuff --;
					totalBufferAccesses++;
					return true;
				}
			}
			else if(nextId == DIRECTION.LEFT){
				if(this.availBuff>0)
				{
					totalBufferAccesses++;
					this.availBuff --;
					return true;
				}
			}
		}
		return false;
	}
	/*******************************************************
	 * Increment available number of buffers
	 *******************************************************/
	public void FreeBuffer()
	{
		this.availBuff ++;
	}
	
	public int bufferSize()
	{
		return this.availBuff;
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		int nextID;
		event.setEventTime(0);
		ID destinationId = ((NocInterface)event.getProcessingElement().getComInterface()).getId();
		int elementNumber = destinationId.gety();     //bank id interpreted as one row, multiple column number
															//and second element gives the actual number and first number
															//will be zero always.
		String binary = Integer.toBinaryString(numColumns | elementNumber).substring(1);
		RequestType requestType = event.getRequestType();
		
		if(topology == TOPOLOGY.BUTTERFLY || topology == TOPOLOGY.OMEGA)
			nextID = nextIdbutterflyOmega(binary);			//binary representation of number needed for routing
		else                                               //if(topology == TOPOLOGY.FATTREE)
			nextID = nextIdFatTree(elementNumber);
		this.hopCounters++;
		((AddressCarryingEvent)event).hopLength++;
		this.connection[nextID].getPort().put(             //posting event to nextID
				event.update(
						eventQ,
						1,
						this, 
						this.connection[nextID],
						requestType));
	}

}
