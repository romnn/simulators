package memorysystem;
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
import java.util.Vector;

import generic.Event;
import generic.EventQueue;
import generic.RequestType;
import generic.SimulationElement;

public class AddressCarryingEvent extends Event implements Cloneable
{
	private long address;
	private Vector<Integer> sourceId;
	private Vector<Integer> destinationId;
	public long event_id;
	
	public RequestType actualRequestType;
	public int hopLength;

	public AddressCarryingEvent(EventQueue eventQ, long eventTime,
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, long address) {
		super(eventQ, eventTime, requestingElement, processingElement,
				requestType, -1, -1);
		this.address = address;
		sourceId = null;
		destinationId = null;
	}
	
	public AddressCarryingEvent()
	{
		super(null, -1, null, null, RequestType.Cache_Read, -1, -1);
		this.address = -1;
		sourceId = null;
		destinationId = null;
	}
	
	@SuppressWarnings("unchecked")
	public AddressCarryingEvent(EventQueue eventQ, long eventTime,
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, long address,int tpcId, int  smId,
			Vector<Integer> sourceId, Vector<Integer> destinationId) {
		super(eventQ, eventTime, requestingElement, processingElement,
				requestType, tpcId, smId);
		this.address = address;
		this.sourceId = (Vector<Integer>) sourceId.clone();
		this.destinationId = (Vector<Integer>) destinationId.clone();
	}
	@SuppressWarnings("unchecked")
	public AddressCarryingEvent(long eventId, EventQueue eventQ, long eventTime,
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, long address,int tpcId, int  smId,
			Vector<Integer> sourceId, Vector<Integer> destinationId) {
		super(eventQ, eventTime, requestingElement, processingElement,
				requestType, tpcId, smId);
		this.event_id = eventId;
		this.address = address;
		this.sourceId = (Vector<Integer>) sourceId.clone();
		this.destinationId = (Vector<Integer>) destinationId.clone();
	}
	public AddressCarryingEvent(EventQueue eventQ, long eventTime,
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, long address,int tpcId, int  smId) {
		super(eventQ, eventTime, requestingElement, processingElement,
				requestType, tpcId, smId);
		this.address = address;
	}
	
	public AddressCarryingEvent updateEvent(EventQueue eventQ, long eventTime, 
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, long address,int tpcId, int  smId,
			Vector<Integer> sourceId, Vector<Integer> destinationId) {
		this.address = address;
		this.tpcId = tpcId;
		this.smId = smId;
		return (AddressCarryingEvent)this.update(eventQ, eventTime, requestingElement, processingElement, requestType);
	}
	
	
	public AddressCarryingEvent updateEvent(EventQueue eventQ, long eventTime, 
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, long address,int tpcId, int  smId) {
		this.address = address;
		this.tpcId = tpcId;
		this.smId = smId;
		return (AddressCarryingEvent)this.update(eventQ, eventTime, requestingElement, processingElement, requestType);
	}
	
	@SuppressWarnings("unchecked")
	public AddressCarryingEvent updateEvent(EventQueue eventQ, long eventTime, 
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, 
			Vector<Integer> sourceId,
			Vector<Integer> destinationId) {
		this.sourceId = (Vector<Integer>) sourceId.clone();
		this.destinationId = (Vector<Integer>) destinationId.clone();
		return (AddressCarryingEvent) this.update(eventQ, eventTime, requestingElement, processingElement, requestType);
	}
	
	public long getAddress() {
		return address;
	}

	public void setAddress(long address) {
		this.address = address;
	}

	@SuppressWarnings("unchecked")
	public void setSourceId(Vector<Integer> sourceId) {
		this.sourceId = (Vector<Integer>) sourceId.clone();
	}

	public Vector<Integer> getSourceId() {
		return sourceId;
	}

	@SuppressWarnings("unchecked")
	public void setDestinationId(Vector<Integer> destinationId) {
		this.destinationId = (Vector<Integer>) destinationId.clone();
	}

	public Vector<Integer> getDestinationId() {
		return destinationId;
	}
	
	public void dump()
	{
		System.out.println("TPC = " + tpcId + " SM = "+ smId + " : " + requestType + " : " + requestingElement + " : " + processingElement + " : " + eventTime + " : " + address);
	}
	
	public String toString(){
		String s = ("TPC = " + tpcId + " SM = "+ smId + " : " + requestType + " : " + requestingElement + " : " + processingElement + " : " + eventTime + " : " + address); 
		return s;
	}
}
