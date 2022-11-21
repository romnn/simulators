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
package memorysystem;

import generic.CachePullEvent;
import generic.PortType;
import generic.SimulationElement;
import generic.SM;
import generic.RequestType;
import config.SystemConfig;
import config.CacheConfig;

public abstract class SMMemorySystem extends SimulationElement
{
	protected int tpc_id;
	protected int sm_id;
	protected SM sm;
	protected Cache iCache;
	protected Cache constantCache;
	protected Cache sharedCache;
	protected Cache dCache;
	
	protected long numInstructionSetChunksNoted = 0;
	protected long numDataSetChunksNoted = 0;
	
	@SuppressWarnings("static-access")
	protected SMMemorySystem(SM sm)
	{
		super(PortType.Unlimited, -1, -1, sm.getEventQueue(), -1, -1);
		
		this.setSM(sm);
		this.tpc_id = sm.getTPC_number();
		this.sm_id = sm.getSM_number();
		
		CacheConfig cacheParameterObj;
		cacheParameterObj = SystemConfig.tpc[tpc_id].sm[sm_id].iCache;
		iCache = new Cache("iCache", this.sm_id, cacheParameterObj, this);
		iCache.setComInterface(sm.getComInterface());
		//add initial cache pull event
		this.sm.getEventQueue().addEvent(
									new CachePullEvent(
											this.sm.getEventQueue(),
											0,
											iCache,
											iCache,
											RequestType.PerformPulls,
											this.tpc_id,
											this.sm_id));
		
		// dCache
		
		cacheParameterObj = SystemConfig.tpc[tpc_id].sm[sm_id].dCache;
		dCache = new Cache("dCache", this.sm_id, cacheParameterObj, this);
		dCache.setComInterface(sm.getComInterface());
		//add initial cache pull event
		this.sm.getEventQueue().addEvent(
									new CachePullEvent(
											this.sm.getEventQueue(),
											0,
											dCache,
											dCache,
											RequestType.PerformPulls,
											this.tpc_id,
											this.sm_id));
		
		//initialize constant cache
		cacheParameterObj = SystemConfig.tpc[tpc_id].sm[sm_id].constantCache;
		constantCache = new Cache("constantCache", this.sm_id, cacheParameterObj, this);
		constantCache.setComInterface(sm.getComInterface());
		//add initial cache pull event
		this.sm.getEventQueue().addEvent(
									new CachePullEvent(
											this.sm.getEventQueue(),
											0,
											constantCache,
											constantCache,
											RequestType.PerformPulls,
											this.tpc_id,
											this.sm_id));
		
		//initialize shared cache
		cacheParameterObj = SystemConfig.tpc[tpc_id].sm[sm_id].sharedCache;
		sharedCache = new Cache("sharedCache", this.sm_id, cacheParameterObj, this);
		sharedCache.setComInterface(sm.getComInterface());
		//add initial cache pull event
		this.sm.getEventQueue().addEvent(
									new CachePullEvent(this.sm.getEventQueue(),	0,sharedCache,	sharedCache,RequestType.PerformPulls,
											this.tpc_id,
											this.sm_id));
	}
	
	public abstract void issueRequestToInstrCache(long address);
			
	public abstract boolean issueRequestToConstantCache(RequestType requestType, long address) throws Exception;
	
	public abstract boolean issueRequestToSharedCache(RequestType requestType, long address) throws Exception;
	

	public Cache getiCache() {
		return iCache;
	}
	
	public Cache getConstantCache() {
		return constantCache;
	}
	
	public Cache getSharedCache() {
		return sharedCache;
	}

	private String tagNameWithSMId(String name) {
		return (name + "[" + this.sm_id + "]");
	}
	
	public void setSM(SM sm) {
		this.sm = sm;
	}

	public SM getSM() {
		return sm;
	}
}
