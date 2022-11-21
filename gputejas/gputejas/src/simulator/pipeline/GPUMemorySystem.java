package pipeline;
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
import generic.SM;
import generic.Event;
import generic.EventQueue;
import generic.RequestType;
import memorysystem.AddressCarryingEvent;
import memorysystem.SMMemorySystem;

public class GPUMemorySystem extends SMMemorySystem {
	
	GPUExecutionEngine containingExecEngine;
	public int numOfLoads=0;
	public long numOfStores;
	public GPUMemorySystem(SM sm, int i)
	{
		super(sm);
		sm.getExecEngine().setCoreMemorySystem(this);
		containingExecEngine = (GPUExecutionEngine)sm.getExecEngine();
	}
	//To issue the request to instruction cache
	@SuppressWarnings("unused")
	public void issueRequestToInstrCache(long address)
	{
		GPUpipeline pipeline = (GPUpipeline)sm.getPipelineInterface();
//		System.out.println("Issued to Instruction Caches"+sm.getTPC_number()+ sm.getSM_number());

		AddressCarryingEvent addressEvent = new AddressCarryingEvent(getSM().getEventQueue(),
				 iCache.getLatencyDelay(),this,  iCache, RequestType.Cache_Read, address, sm.getTPC_number(), sm.getSM_number());

		
		//attempt issue to lower level cache
		AddressCarryingEvent clone = (AddressCarryingEvent) addressEvent.clone();
		boolean isAddedinLowerMshr = this.iCache.addEvent(clone);
		if(!isAddedinLowerMshr)
		{
			misc.Error.showErrorAndExit("Unable to add event to iCache's MSHR !!" + 
					"\nevent = " + addressEvent + 
					"\niCache = " + this.iCache);
		}
		
	}
	
	@SuppressWarnings("unused")
	@Override
	public boolean issueRequestToConstantCache(RequestType requestType,
			long address) throws Exception{
		GPUpipeline inorderPipeline = (GPUpipeline)sm.getPipelineInterface();

		
		AddressCarryingEvent addressEvent = new AddressCarryingEvent(getSM().getEventQueue(),
																	 constantCache.getLatencyDelay(),
																	 this, 
																	 constantCache,
																	 requestType, 
																	 address,
																	 sm.getTPC_number(), sm.getSM_number() );
		
	
		//attempt issue to lower level cache
		AddressCarryingEvent clone = (AddressCarryingEvent) addressEvent.clone();
		boolean isAddedinLowerMshr = this.constantCache.addEvent(clone);
		if(!isAddedinLowerMshr)
		{
			misc.Error.showErrorAndExit("Unable to add event to constant Cache's MSHR !!" + 
					"\nevent = " + addressEvent + 
					"\niCache = " + this.iCache);
		}
		
		containingExecEngine.updateNoOfMemRequests(1);
		if(requestType == RequestType.Cache_Read)
		{
			containingExecEngine.updateNoOfLd(1);
		}
		else if(requestType == RequestType.Cache_Write)
		{
			containingExecEngine.updateNoOfSt(1);
		}
		
		return true;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean issueRequestToSharedCache(RequestType requestType,
			long address) {
		GPUpipeline inorderPipeline = (GPUpipeline)sm.getPipelineInterface();

		
		AddressCarryingEvent addressEvent = new AddressCarryingEvent(getSM().getEventQueue(),
																	 sharedCache.getLatencyDelay(),
																	 this, sharedCache, requestType, address,
																	 sm.getTPC_number(), sm.getSM_number() );
		
	
		
		//attempt issue to lower level cache
		AddressCarryingEvent clone = (AddressCarryingEvent) addressEvent.clone();
		boolean isAddedinLowerMshr = this.sharedCache.addEvent(clone);
		if(!isAddedinLowerMshr)
		{
			misc.Error.showErrorAndExit("Unable to add event to shared Cache's MSHR !!" + 
					"\nevent = " + addressEvent + 
					"\niCache = " + this.iCache);
		}
		
		containingExecEngine.updateNoOfMemRequests(1);
		if(requestType == RequestType.Cache_Read)
		{
			containingExecEngine.updateNoOfLd(1);
		}
		else if(requestType == RequestType.Cache_Write)
		{
			containingExecEngine.updateNoOfSt(1);
		}
		
		return true;
	}
	
	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		
		//handle memory response
		
		AddressCarryingEvent memResponse = (AddressCarryingEvent) event;
		long address = memResponse.getAddress();
		
		//if response comes from iCache, inform fetchunit
		if(memResponse.getRequestingElement() == iCache)
			
		{	
//			System.out.println("calling from the gpumemorysystem i cache condition");
			containingExecEngine.getScheduleUnit().processCompletionOfMemRequest(address);
		}
		else if(memResponse.getRequestingElement() == constantCache)
		{
			containingExecEngine.getExecuteUnit().processCompletionOfMemRequest(address);
		}
		
		else if(memResponse.getRequestingElement() == sharedCache)
		{
			containingExecEngine.getExecuteUnit().processCompletionOfMemRequest(address);
		}
		
		else
		{
			System.out.println("mem response received by inordercoreMemSys from unkown object : " + memResponse.getRequestingElement());
		}
	}



}
