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


/*
 * Event class contains the bare-minimum that every event must contain.
 * This class must be extended for every RequestType.
 * 
 * The extendedClass would define the requestType and would contain the payLoad 
 * of the request too.This simplifies the code as we now don't have to create a 
 * separate pay-load class for each type of request. 
 */
public abstract class Event implements Cloneable
{
	protected long eventTime;
	protected EventQueue eventQ;
	protected RequestType requestType;
	private long priority;
	public int tpcId;
	public int smId;
//serialization and globalSerialization copied
	public long serializationID = 0;
	public static long globalSerializationID = 0;
	public Event clone()
	{
		try {
			return (Event) (super.clone());
		} catch (CloneNotSupportedException e) {
			misc.Error.showErrorAndExit("Error in cloning event object");
			return null;
		}
	}
	
	//Element which processes the event.
	protected SimulationElement requestingElement;
	protected SimulationElement processingElement;
	protected SimulationElement actualRequestingElement;
	protected SimulationElement actualProcessingElement;
	public void incrementSerializationID() {
		globalSerializationID++;
		serializationID = globalSerializationID;
	}
	public Event(EventQueue eventQ, SimulationElement requestingElement,
			SimulationElement processingElement, RequestType requestType) 
	{
		eventTime = -1; // this should be set by the port
		this.eventQ = eventQ;
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		this.requestType = requestType;
		this.tpcId = 0;	
		this.smId = 0;		
		this.priority = requestType.ordinal();
	}
	
	
	/*
	 * Added by Gantavya:
	 * I am making a special constructor in this Event class to tackle the event in the DRAM.
	 * in the DRAM code of CPUTEJAS, i found that they are sending the core id to be equal to -1 
	 * because that event was not having any resemblance with the core, as it was happening in the 
	 * DRAM. So here also i am defining a constructor in which the last argument will be core id. 
	 * Since it will be called only by the state update event of the DRAM package, thus its value will
	 * be -1. (As in TEJAS). This -1 value will correspond to the tpcID = -1 and smID = -1.
	 * 
	 */
	
	public Event(EventQueue eventQ, long eventTime, SimulationElement requestingElement,
			SimulationElement processingElement, RequestType requestType, int coreId) 
	{
		incrementSerializationID();
		this.eventTime = eventTime; // this should be set by the port
		this.eventQ = eventQ;
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		this.requestType = requestType;
		this.smId = -1;
		this.tpcId= -1;
		this.priority = requestType.ordinal();
	}

	
	public Event update(SimulationElement requestingElement,
			SimulationElement processingElement, SimulationElement actualRequestingElement,
			SimulationElement actualProcessingElement)
	{
		incrementSerializationID(); //copied from tejas
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		this.actualProcessingElement = actualProcessingElement;//copied from tejas
		this.actualRequestingElement = actualRequestingElement;//copied from tejas
		return this;
	}
//	public Event(EventQueue eventQ, long eventTime, SimulationElement requestingElement,
//			SimulationElement processingElement, RequestType requestType, int coreId) 
//	{
//		incrementSerializationID();
//		this.eventTime = eventTime; // this should be set by the port
//		this.eventQ = eventQ;
//		this.requestingElement = requestingElement;
//		this.processingElement = processingElement;
//		this.requestType = requestType;
//		this.coreId = coreId;
//		this.priority = requestType.ordinal();
//	}
	public Event(EventQueue eventQ, long eventTime, SimulationElement requestingElement,
			SimulationElement processingElement, RequestType requestType, int tpcId, int smId) 
	{
		this.eventTime = eventTime; // this should be set by the port
		this.eventQ = eventQ;
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		this.requestType = requestType;
		this.tpcId = tpcId;
		this.smId = smId;
		this.priority = requestType.ordinal();
	}

	public Event update(EventQueue eventQ, long eventTime, SimulationElement requestingElement,
			SimulationElement processingElement, RequestType requestType)
	{
		this.eventTime =  eventTime;
		this.eventQ = eventQ;
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		this.requestType = requestType;
		this.priority = requestType.ordinal();
		return this;
	}
	public Event update(SimulationElement requestingElement,
			SimulationElement processingElement)
	{
		incrementSerializationID();
		this.requestingElement = requestingElement;
		this.processingElement = processingElement;
		return this;
	}

	public SimulationElement getActualRequestingElement() {
		return actualRequestingElement;
	}// copied from tejas
	
	public SimulationElement getActualProcessingElement() {
		return actualProcessingElement;
	}// copied from Tejas
	public Event update(long eventTime)
	{
		incrementSerializationID();
		this.eventTime =  eventTime;
		return this;
	}
	
	public long getEventTime() {
		return eventTime;
	}

	public long getPriority() {
		return priority;
	}

	public SimulationElement getRequestingElement() {
		return requestingElement;
	}
	
	public void setRequestingElement(SimulationElement requestingElement) {
		this.requestingElement = requestingElement;
	}

	public SimulationElement getProcessingElement() {
		return processingElement;
	}  

	public void setProcessingElement(SimulationElement processingElement) {
		this.processingElement = processingElement;
	}

	
	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}
	
	public void addEventTime(long additionTime) {
		this.setEventTime(this.eventTime + additionTime);
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}
	
	public EventQueue getEventQ() {
		return eventQ;
	}

	public void resetPriority(RequestType requestType)
	{
		this.priority = requestType.ordinal();
	}
	
	public RequestType getRequestType()
	{
		return requestType;
	}
	
	public void setRequestType(RequestType requestType)
	{
		this.requestType = requestType;
	}

	//If the event cannot be handled in the current clock-cycle,
	//then the eventPriority and eventTime will be changed and then 
	//the modified event will be added to the eventQueue which is 
	//now passed as a parameter to this function.
	public void handleEvent(EventQueue eventQ)
	{
			processingElement.handleEvent(eventQ, this);
	}
	
	public void dump()
	{
		System.out.println("TPC = "+tpcId + " SM = "+ smId + " : " + requestType + " : " + requestingElement + " : " + processingElement + " : " + eventTime);
	}
}
