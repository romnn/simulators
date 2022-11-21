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

import generic.CommunicationInterface;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.PortType;
import generic.RequestType;
import generic.SM;
import generic.SimulationElement;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Vector;

import net.NocInterface;

import main.ArchitecturalComponent;
import memorysystem.directory.CentralizedDirectoryCache;
import memorysystem.NucaCache;
import memorysystem.NucaCache.NucaType;
import misc.Util;
import config.CacheConfig;
import config.CacheConfig.WritePolicy;
import config.CacheEnergyConfig;
import config.EnergyConfig;
import config.SimulationConfig;
import config.SystemConfig;
import dram.MainMemoryDRAMController;

public class Cache extends SimulationElement
{
		public static enum CacheType{
			L1,
			iCache,
			dCache,
			constantCache,
			sharedCache,
			Lower,
			L2,
			Directory
		}
		
		public static enum CoherenceType{
			Snoopy,
			Directory,
			None,
			LowerLevelCoherent
		}

		public static String trafficToNextLevel;
		CacheEnergyConfig energy;
		/* cache parameters */
		public SMMemorySystem containingMemSys;
		protected int blockSize; // in bytes
		public int blockSizeBits; // in bits
		public  int assoc;
		protected int assocBits; // in bits
		protected int size; // MegaBytes
		protected int numLines;
		protected int numLinesBits;
		public int numSetsBits;
		protected double timestamp;
		protected int numLinesMask;
		protected Vector<Long> evictedLines = new Vector<Long>();
		
		public CoherenceType coherence = CoherenceType.None;
		public int numberOfBuses = 1;
		
		public CacheType levelFromTop; 
		public boolean isLastLevel; //Tells whether there are any more levels of cache
		public CacheConfig.WritePolicy writePolicy; //WRITE_BACK or WRITE_THROUGH
		public String nextLevelName; //Name of the next level cache according to the configuration file
		public ArrayList<Cache> prevLevel = new ArrayList<Cache>(); //Points towards the previous level in the cache hierarchy
		public Cache nextLevel; //Points towards the next level in the cache hierarchy
		protected CacheLine lines[];
		public NucaType nucaType;
		public CacheConfig cacheConfig;
		
		public MissStatusHoldingRegister missStatusHoldingRegister;
		public String cacheName;
		public int id;
		
		public void createLinkToNextLevelCache(Cache nextLevelCache) {
			this.nextLevel = nextLevelCache;
			this.nextLevel.prevLevel.add(this);
		}
		
		public long noOfRequests;
		public long noOfAccesses;
		public long noOfResponsesReceived;
		public long noOfResponsesSent;
		public long noOfWritesForwarded;
		public long noOfWritesReceived;
		public long hits;
		public long misses;
		public long evictions;
		public boolean debug =false;
		
		public Cache(String cacheName, int id, CacheConfig cacheParameters, SMMemorySystem containingMemSys)
		{
			super(cacheParameters.portType,
					cacheParameters.getAccessPorts(), 
					cacheParameters.getPortOccupancy(),
					cacheParameters.getLatency());
			
			MemorySystem.addToCacheList(cacheName, this);
				
				if(containingMemSys==null) {
					ArchitecturalComponent.sharedCaches.add(this);
				}
				
				ArchitecturalComponent.caches.add(this);
				
				this.writePolicy = cacheParameters.getWritePolicy();

		    this.cacheName = cacheName;
		    this.id = id;
			
			this.containingMemSys = containingMemSys;
			// set the parameters
			this.blockSize = cacheParameters.getBlockSize();
			this.assoc = cacheParameters.getAssoc();
			this.size = cacheParameters.getSize(); // in kilobytes
			this.blockSizeBits = Util.logbase2(blockSize);
			this.assocBits = Util.logbase2(assoc);
			this.numLines = getNumLines();
			this.numLinesBits = Util.logbase2(numLines);
			this.numSetsBits = numLinesBits - assocBits;
			this.cacheConfig = cacheParameters;
	
			this.writePolicy = cacheParameters.getWritePolicy();
			this.levelFromTop = cacheParameters.getLevelFromTop();
			this.isLastLevel = cacheParameters.isLastLevel();
			this.nextLevelName = cacheParameters.getNextLevel();
			this.coherence = cacheParameters.getCoherence();
			this.numberOfBuses = cacheParameters.getNumberOfBuses();
			this.energy=cacheParameters.power;
			this.timestamp = 0;
			this.numLinesMask = numLines - 1;
			this.noOfRequests = 0;
			noOfAccesses = 0;noOfResponsesReceived = 0;noOfResponsesSent = 0;noOfWritesForwarded = 0;
			noOfWritesReceived = 0;
			this.hits = 0;
			this.misses = 0;
			this.evictions = 0;
			// make the cache
			makeCache();
			
			if(this.levelFromTop == CacheType.L1 || this.levelFromTop == CacheType.iCache)
			{
				missStatusHoldingRegister = new Mode3MSHR(blockSizeBits, SimulationConfig.ThreadsPerCTA, this.containingMemSys.sm.eventQueue);
			}
			else
				missStatusHoldingRegister = new Mode3MSHR(blockSizeBits, 16*SimulationConfig.ThreadsPerCTA, null);
		}
		
		public Cache(
				int size,
				int associativity,
				int blockSize,
				WritePolicy writePolicy,
				int mshrSize
				)
		{
			super(PortType.FirstComeFirstServe,	2,2,2);
			
			// set the parameters
			this.blockSize = blockSize;
			this.assoc = associativity;
			this.size = size; // in kilobytes
			this.blockSizeBits = Util.logbase2(blockSize);
			this.assocBits = Util.logbase2(assoc);
			this.numLines = getNumLines();
			this.numLinesBits = Util.logbase2(numLines);
			this.numSetsBits = numLinesBits - assocBits;
	
			this.writePolicy = writePolicy;
			this.levelFromTop = CacheType.L1;
			this.isLastLevel = true;
			
			this.timestamp = 0;
			this.numLinesMask = numLines - 1;
			this.noOfRequests = 0;
			noOfAccesses = 0;noOfResponsesReceived = 0;noOfResponsesSent = 0;noOfWritesForwarded = 0;
			noOfWritesReceived = 0;
			this.hits = 0;
			this.misses = 0;
			this.evictions = 0;
			// make the cache
			makeCache();
			
			System.out.println("Initialized");
			missStatusHoldingRegister = new Mode3MSHR(blockSizeBits, 10000, null);
			
		}
		
		
		TreeSet<Long> workingSet = null;
		long workingSetChunkSize = 0;
		public long numWorkingSetHits = 0;
		public long numWorkingSetMisses = 0;
		public long numFlushesInWorkingSet = 0;
		public long totalWorkingSetSize = 0;
		public long maxWorkingSetSize = Long.MIN_VALUE;
		public long minWorkingSetSize = Long.MAX_VALUE;
		
		
		void addToWorkingSet(long addr) {
			long lineAddr = addr >>> blockSizeBits;
			if(workingSet!=null) {
				if(workingSet.contains(lineAddr)==true) {
					numWorkingSetHits++;
					return;
				} else {
					numWorkingSetMisses++;
					workingSet.add(lineAddr);
				}
			}
		}
		
		float getWorkingSetHitrate() {
			if(numWorkingSetHits==0 && numWorkingSetMisses==0) {
				return 0.0f;
			} else {
				return (float)numWorkingSetHits/(float)(numWorkingSetHits + numWorkingSetMisses);
			}
		}
		
		void clearWorkingSet() {
			numFlushesInWorkingSet++;
			totalWorkingSetSize += workingSet.size();
			if(workingSet.size()>maxWorkingSetSize) {
				maxWorkingSetSize = workingSet.size();
			}
			
			if(workingSet.size()<minWorkingSetSize) {
				minWorkingSetSize = workingSet.size();
			}
			
			
			if(workingSet!=null) {
				workingSet.clear();
			}
		}
		
		
		private boolean printCacheDebugMessages = false;
		public void handleEvent(EventQueue eventQ, Event event)
		{
			
//			// Sanity check for iCache
//			if(this.levelFromTop==CacheType.iCache && event.getRequestType()==RequestType.Cache_Read && ((AddressCarryingEvent)event).getAddress()==-1) {
//				misc.Error.showErrorAndExit("iCache is getting request for invalid ip : -1");
//			}
//			
//			if(printCacheDebugMessages==true) {
//				if(event.getClass()==AddressCarryingEvent.class)
//				{
//				System.out.println("CACHE : globalTime = " + ArchitecturalComponent.getCores()[event.tpcId][event.smId].clock.getCurrentTime() +
//					"\teventTime = " + event.getEventTime() + "\t" + event.getRequestType() +
//					"\trequestingElelement = " + event.getRequestingElement() +
//					"\taddress = " + ((AddressCarryingEvent)event).getAddress() +
//					"\t" + this);
//				}
//			}
//			
//			if(this.levelFromTop == CacheType.L1 || this.levelFromTop == CacheType.iCache)
//			{
//			}
//			System.out.println("Handle Event Cache Please");
			if (event.getRequestType() == RequestType.Cache_Read || event.getRequestType() == RequestType.Cache_Write)
			{
				this.handleAccess(eventQ, (AddressCarryingEvent) event);
				long address = ((AddressCarryingEvent) event).getAddress();
				this.addToWorkingSet(address); //only read and write count as working set
			}
			
			else if (event.getRequestType() == RequestType.Cache_Read_Writeback|| event.getRequestType() == RequestType.Send_Mem_Response 
					|| event.getRequestType() == RequestType.Send_Mem_Response_Invalidate) 
			{
				this.handleAccessWithDirectoryUpdates(eventQ, (AddressCarryingEvent) event);
			}
			
			else if (event.getRequestType() == RequestType.Mem_Response)
			{ 
//				System.out.println("request type Mem response");
				this.handleMemResponse(eventQ, event);
			}
			
			else if (event.getRequestType() == RequestType.MESI_Invalidate)
			{
				this.handleInvalidate((AddressCarryingEvent) event);
			}
			
			else if (event.getRequestType() == RequestType.MSHR_Full)
			{
				// Reset the requestType to actualRequestType
				event.setRequestType(((AddressCarryingEvent)event).actualRequestType);
				((AddressCarryingEvent)event).actualRequestType = null;
				Cache processingCache = (Cache)event.getProcessingElement();
				if (processingCache.addEvent((AddressCarryingEvent)event) == false)
				{
					missStatusHoldingRegister.handleLowerMshrFull((AddressCarryingEvent)event);
				}
			}
		}
		public void sendAcknowledgement(AddressCarryingEvent event) {
			RequestType returnType = null;
			if(event.getRequestType()==RequestType.Cache_Read) {
				returnType = RequestType.Mem_Response;
			} else {
				misc.Error.showErrorAndExit("sendAcknowledgement is meant for cache read operation only : " + event);
			}
			
			AddressCarryingEvent memResponseEvent = new AddressCarryingEvent(
					event.getEventQ(), 0, event.getProcessingElement(),
					event.getRequestingElement(), returnType,
					event.getAddress());
			
			sendEvent(memResponseEvent);
			noOfResponsesSent++;

		}
		protected void cacheHit(long addr, RequestType requestType, CacheLine cl,
				AddressCarryingEvent event) {
			hits++;
			noOfRequests++;
//			System.out.println("eventsTobeServed for "+containingMemSys.tpc_id);
//			System.out.println("Cache hit here");
			noOfAccesses++;
			
			if (requestType == RequestType.Cache_Read) {
				sendAcknowledgement(event);
			} else if (requestType == RequestType.Cache_Write) {
				if(this.writePolicy == WritePolicy.WRITE_THROUGH) {
					sendRequestToNextLevel(addr, RequestType.Cache_Write, event.tpcId, event.smId);
				}
				
				if( (cl.getState() == MESI.SHARED || cl.getState() == MESI.EXCLUSIVE)) {
					//handleCleanToModified(addr, event);
				}
			} else {
				misc.Error.showErrorAndExit("cache hit unknown event type\n" + event + "\ncache : " + this);
			}
		}
		public void handleAccess(EventQueue eventQ, AddressCarryingEvent event)
		{
//			System.out.println("Cache Calls"+event.tpcId+", "+event.smId+" ");
			long address = event.getAddress();
			
			
			if(event.getRequestType() == RequestType.Cache_Write)
			{
				
				noOfWritesReceived++;
			}
			
			RequestType requestType = event.getRequestType();
			
			
			CacheLine cl = this.processRequest(requestType, address, event);
			this.noOfAccesses++;
			
			if (cl != null || missStatusHoldingRegister.containsWriteOfEvictedLine(address) )
			{
				this.hits++;
				processBlockAvailable(event);				
			}
			else
			{	
				
				if(this.coherence == CoherenceType.Directory && event.getRequestType() == RequestType.Cache_Write)
				{
					writeMissUpdateDirectory(event.tpcId, event.smId, ( address>>> blockSizeBits ), event, address);
				}
				else if(this.coherence == CoherenceType.Directory && event.getRequestType() == RequestType.Cache_Read )
				{
					readMissUpdateDirectory(event.tpcId, event.smId,( address>>> blockSizeBits ), event, address);
				} 
				else 
				{
					this.misses++;				
					sendRequestToNextLevel(address, RequestType.Cache_Read, event.tpcId,event.smId );
				}
			
			missStatusHoldingRegister.addOutstandingRequest(event);	
			}
			
		}
		public void sendRequestToNextLevel(long addr, RequestType requestType, int tpcId, int smId) {
			if(this.isLastLevel==true)
			{
				ArchitecturalComponent.tomemory++;
				AddressCarryingEvent event = null;
			//event = new AddressCarryingEvent(getEventQueue(), 0, this, c,	requestType, addr);
			//addEventAtLowerCache(event, c);
			SM core0 = ArchitecturalComponent.getCores()[0][0];
	
			MainMemoryDRAMController memController;
		CommunicationInterface ComInterface = getComInterface();
			//System.out.println(ComInterface + " "+levelFromTop);
		memController = getComInterface().getNearestMemoryController();	
//				}

		event = new AddressCarryingEvent(core0.getEventQueue(), 0, this,memController, requestType, addr, tpcId, smId);
		sendEvent(event);
			}
			
	else {	
		
		Cache c = this.nextLevel;
		//System.out.println(this.nextLevel);
		//System.out.println(c.toString());
		AddressCarryingEvent event = null;
		
		if (c != null) {
			
			if(c.nucaType!=null)
				{
					if(c.nucaType != NucaType.NONE)
					{
				
						NucaCache nuca = ArchitecturalComponent.nucaList.get("L2");//FIXME: dont use static L2
						c = nuca.getSNucaBank(addr);
					}
			}
			event = new AddressCarryingEvent(c.getEventQueue(), 0, this, c,	requestType, addr);
			addEventAtLowerCache(event, c);
				}
		}
	}
		
		
		
public boolean isBusy() {
			return missStatusHoldingRegister.isFull();
		}
	

public boolean addEventAtLowerCache(AddressCarryingEvent event, Cache c) {
		if (c.isBusy() == false) {
			sendEvent(event);
//			c.workingSetUpdate();
			return true;
		} else {
			// Slight approximation used. MSHR full is a rare event.
			// On occurrence of such events, we just add this event to the pending events list of the lower level cache.
			// The network congestion and the port occupancy of the next level is not modelled in such cases.
			// It must be noted that the MSHR full event of the first level caches is being modelled correctly.
			// This approximation applies only to non-firstlevel caches.
			c.eventsWaitingOnMSHR.add(event);
			return false;
		}
	}

	public LinkedList<AddressCarryingEvent> eventsWaitingOnMSHR = new LinkedList<AddressCarryingEvent>(); 
		public EventQueue getEventQueue() {
			if (containingMemSys != null) {
				return containingMemSys.getSM().eventQueue;
			} else {
				return (ArchitecturalComponent.getCores()[0])[0].eventQueue;
			}
		}

		@SuppressWarnings("unused")
		private void handleAccessWithDirectoryUpdates(EventQueue eventQ, AddressCarryingEvent event) 
		{
			if(event.getRequestType() == RequestType.Cache_Read_Writeback ) 
			{
				if (this.isLastLevel)
				{
					putEventToPort(event, ArchitecturalComponent.memoryControllers.get(0), RequestType.Main_Mem_Write, true, true);
				}
				else
				{
					long addr = event.getAddress();
					long set_addr = addr>>blockSizeBits;
					CacheLine cl = access(addr);
					
					if(cl!=null) {
						cl.setState(MESI.EXCLUSIVE);
					}
				}
			}
			else if(event.getRequestType() == RequestType.Send_Mem_Response_Invalidate)
			{
				handleInvalidate(event);
			}

			sendMemResponseDirectory(event);
		}
		
		protected void handleMemResponse(EventQueue eventQ, Event event)
		{
			noOfResponsesReceived++;
			
			this.fillAndSatisfyRequests(eventQ, event, MESI.EXCLUSIVE);
		}
		
		public void handleInvalidate(Event event)
		{
			CacheLine cl = this.access(((AddressCarryingEvent)event).getAddress());
			if (cl != null) {
				cl.setState(MESI.INVALID);
			}
			
			invalidatePreviousLevelCaches((AddressCarryingEvent)event);
		}
		
		private void invalidatePreviousLevelCaches(AddressCarryingEvent event)
		{
			// If I am invalidating a cache entry, I must inform all the previous level caches 
			// about the same
			if(prevLevel==null) {
				return;
			}
			
			for(int i=0; i<prevLevel.size(); i++) {
				Cache c = prevLevel.get(i);
				c.getPort().put(
					new AddressCarryingEvent(
						c.containingMemSys.getSM().getEventQueue(),
						c.getLatency(),
						this, 
						c,
						RequestType.MESI_Invalidate, 
						((AddressCarryingEvent)event).getAddress(),
						c.containingMemSys.getSM().getTPC_number(), c.containingMemSys.getSM().getSM_number()));
			}
		}
		
		private void sendWriteRequest(AddressCarryingEvent receivedEvent)
		{
			if(this.isLastLevel)
			{
				sendWriteRequestToMainMemory(receivedEvent);
			} 
			else
			{
				sendWriteRequestToLowerCache(receivedEvent);
			}			
		}		

//		private void sendReadRequest(AddressCarryingEvent receivedEvent)
//		{
//			if(this.isLastLevel)
//			{
//				sendReadRequestToMainMemory(receivedEvent);
//			} 
//			else
//			{
//				sendReadRequestToLowerCache(receivedEvent);
//			}			
//		}
//		
		/* forward memory request to next level
		 * handle related lower level mshr scenarios
		 */
		public void sendWriteRequestToLowerCache(AddressCarryingEvent receivedEvent)
		{
			receivedEvent.update(receivedEvent.getEventQ(),
									this.nextLevel.getLatency(),
									this,
									this.nextLevel,
									RequestType.Cache_Write);
			
			boolean isAddedinLowerMshr = this.nextLevel.addEvent(receivedEvent);
			if(!isAddedinLowerMshr)
			{
				missStatusHoldingRegister.handleLowerMshrFull(receivedEvent);
			}
		}
		
		/*
		 * forward memory request to next level
		 * handle related lower level mshr scenarios
		 */
		public void sendReadRequestToLowerCache(AddressCarryingEvent receivedEvent)
		{
			receivedEvent.update(receivedEvent.getEventQ(),
									this.nextLevel.getLatency(),
									this,
									this.nextLevel,
									RequestType.Cache_Read);
			
			boolean isAddedinLowerMshr = this.nextLevel.addEvent(receivedEvent);
			if(!isAddedinLowerMshr)
			{
				missStatusHoldingRegister.handleLowerMshrFull(receivedEvent);
			}
		}

		private void sendWriteRequestToMainMemory(AddressCarryingEvent receivedEvent)
		{
//			System.out.println("Was earlier calling through here");
			receivedEvent.update(receivedEvent.getEventQ(),ArchitecturalComponent.memoryControllers.get(0).getLatency(),this,ArchitecturalComponent.memoryControllers.get(0),	RequestType.Main_Mem_Write);

			ArchitecturalComponent.memoryControllers.get(0).getPort().put(receivedEvent);
		}
		
		private void sendReadRequestToMainMemory(AddressCarryingEvent receivedEvent)
		{ 
		//	System.out.println("Was earlier calling through here");
			receivedEvent.update(receivedEvent.getEventQ(),ArchitecturalComponent.memoryControllers.get(0).getLatency(),this,
                                ArchitecturalComponent.memoryControllers.get(0),receivedEvent.getRequestType());

			ArchitecturalComponent.memoryControllers.get(0).getPort().put(receivedEvent);
		}
		protected void sendResponseToWaitingEvent(ArrayList<AddressCarryingEvent> outstandingRequestList)
		{
			int numberOfWrites = 0;
			AddressCarryingEvent sampleWriteEvent = null;
			while (!outstandingRequestList.isEmpty())
			{	
				AddressCarryingEvent eventPoppedOut = (AddressCarryingEvent) outstandingRequestList.remove(0); 
				if (eventPoppedOut.getRequestType() == RequestType.Cache_Read)
				{
					sendMemResponse(eventPoppedOut);
				}
				
				else if (eventPoppedOut.getRequestType() == RequestType.Cache_Write)
				{
					if (this.writePolicy == CacheConfig.WritePolicy.WRITE_THROUGH)
						{
								if (this.isLastLevel)
								{	//System.out.println(eventPoppedOut.getRequestingElement());
//									putEventToPort(eventPoppedOut,eventPoppedOut.getRequestingElement(), RequestType.Main_Mem_Write, true,true);
									long addr=eventPoppedOut.getAddress();
									sendRequestToNextLevel(addr, eventPoppedOut.getRequestType(), eventPoppedOut.tpcId, eventPoppedOut.smId);
								}
								else if (this.coherence == CoherenceType.None)
								{
									numberOfWrites++;
									sampleWriteEvent = (AddressCarryingEvent) eventPoppedOut.clone();
								}
						}
						else
						{
								CacheLine cl = this.access(eventPoppedOut.getAddress());
								if (cl != null)
								{
										cl.setState(MESI.MODIFIED);
								}
								else
								{
									numberOfWrites++;
									sampleWriteEvent = (AddressCarryingEvent) eventPoppedOut.clone();
								}
						}
				}
			}
			
			if(numberOfWrites > 0)
			{
				//for all writes to the same block at this level,
				//one write is sent to the next level
				propogateWrite(sampleWriteEvent);
			}
		}
		
		/*
		 * called by higher level cache
		 *returned value signifies whether the event will be saved in mshr or not
		 * */
		@SuppressWarnings("unused")
		public boolean addEvent(AddressCarryingEvent addressEvent)
		{	
			// Clear the working set data after every x instructions
			if(this.containingMemSys!=null && this.workingSet!=null) {
				
				if(levelFromTop==CacheType.iCache) {
					long numInsn = containingMemSys.getiCache().hits + containingMemSys.getiCache().misses; 
					long numWorkingSets = numInsn/workingSetChunkSize; 
					if(numWorkingSets>containingMemSys.numInstructionSetChunksNoted) {
						this.clearWorkingSet();
						containingMemSys.numInstructionSetChunksNoted++;
					}
				} else if(levelFromTop==CacheType.L1) {
					long numInsn = containingMemSys.getiCache().hits + containingMemSys.getiCache().misses;
					long numWorkingSets = numInsn/workingSetChunkSize; 
					if(numWorkingSets>containingMemSys.numDataSetChunksNoted) {
						this.clearWorkingSet();
						containingMemSys.numDataSetChunksNoted++;
					}
				}
			}
			
			
			long address = addressEvent.getAddress();
			boolean entryCreated = missStatusHoldingRegister.addOutstandingRequest(addressEvent);
//			System.out.println("entry Created here");
			if(entryCreated)
			{
				this.getPort().put(addressEvent);
			}
			else
			{
			}
			
			return true;
		}
		
		protected void fillAndSatisfyRequests(EventQueue eventQ, Event event, MESI stateToSet)
		{		
			long addr = ((AddressCarryingEvent)(event)).getAddress();
			
			ArrayList<AddressCarryingEvent> eventsToBeServed = missStatusHoldingRegister.removeRequestsByAddress((AddressCarryingEvent)event);
			if(eventsToBeServed!=null)
			{misses += eventsToBeServed.size();			
			noOfRequests += eventsToBeServed.size();
//			System.out.println("eventsTobeServed for "+containingMemSys.tpc_id);
			noOfAccesses+=eventsToBeServed.size() + 1;}
			CacheLine evictedLine = this.fill(addr, stateToSet);
			
			//This does not ensure inclusiveness
			if (
				evictedLine != null && 
				evictedLine.getState() != MESI.INVALID && 
				// if the line is modified, the cache write policy must NOT be WRITE_THROUGH
				((evictedLine.getState()!=MESI.MODIFIED) || (evictedLine.getState()==MESI.MODIFIED && this.writePolicy!=WritePolicy.WRITE_THROUGH))
			)
			{
				//Update directory in case of eviction
					if(this.coherence==CoherenceType.Directory)
						
					{
							int requestingCore = containingMemSys.getSM().getTPC_number();
							long address= evictedLine.getAddress();	//Generating an address of this cache line
							evictionUpdateDirectory(requestingCore,evictedLine.getTag(),event,address);
					}
					else if (this.isLastLevel)
					{	
						sendRequestToNextLevel(addr, event.getRequestType(), event.tpcId, event.smId);
					//	putEventToPort(event, ArchitecturalComponent.memoryControllers.get(0), RequestType.Main_Mem_Write, false,true);
						
					}
					else
					{
						AddressCarryingEvent eventToForward = new AddressCarryingEvent(event.getEventQ(), this.nextLevel.getLatency(), this,  this.nextLevel, 
																					   RequestType.Cache_Write,  evictedLine.getAddress(),
																					   event.tpcId, event.smId);
						propogateWrite(eventToForward);
					}
			}
			
			if(this.coherence == CoherenceType.Directory)
			{
				AddressCarryingEvent addrEvent = (AddressCarryingEvent) event;
				memResponseUpdateDirectory(addrEvent.tpcId, addrEvent.smId,addrEvent.getAddress() >>> blockSizeBits, addrEvent, addrEvent.getAddress());
			}
		if(eventsToBeServed!=null)
			sendResponseToWaitingEvent(eventsToBeServed);
		}
		
		public void propogateWrite(AddressCarryingEvent event)
		{
			AddressCarryingEvent eventToForward = event.updateEvent(event.getEventQ(),
					   										this.nextLevel.getLatency(),
					   										this,
					   										this.nextLevel,
					   										RequestType.Cache_Write,
					   										event.getAddress(),
					   										event.tpcId, event.smId);
			
			boolean isAdded = this.nextLevel.addEvent(eventToForward);
			if( !isAdded )
			{
				boolean entryCreated =  missStatusHoldingRegister.addOutstandingRequest( eventToForward );
				if (entryCreated)
				{
					missStatusHoldingRegister.handleLowerMshrFull( eventToForward );
				}
			}
			else
			{
				noOfWritesForwarded++;
			}
		}
		public static int findChannelNumber(long physicalAddress){
			long tempA,tempB;
			int channelBits = log2(SystemConfig.mainMemoryConfig.numChans);
			
			int DataBusBytesOffest = log2(SystemConfig.mainMemoryConfig.DATA_BUS_BYTES);		//for 64 bit bus -> 8 bytes -> lower 3 bits of address irrelevant
			
			int ColBytesOffset = log2(SystemConfig.mainMemoryConfig.BL);		
			//these are the bits we need to throw away because of "bursts". The column address is incremented internally on bursts
			//So for a burst length of 4, 8 bytes of data are transferred on each burst
			//Each consecutive 8 byte chunk comes for the "next" column
			//So we traverse 4 columns in 1 request. Thus the lower log2(4) bits become irrelevant for us. Throw them away
			//Finally we get 8 bytes * 4 = 32 bytes of data for a 64 bit data bus and BL = 4.
			//This is equal to a cache line
			
			//For clarity
			//Throw away bits to account for data bus size in bytes
			//and for burst length
			physicalAddress >>>= (DataBusBytesOffest + ColBytesOffset); 		//using >>> for unsigned right shift
			//System.out.println("Shifted address by " + (DataBusBytesOffest + ColBytesOffset) + " bits");
					
					
			//By the same logic, need to remove the burst-related column bits from the column bit width to be decoded
			//colEffectiveBits = colBits - ColBytesOffset;
		
			//row:rank:bank:col:chan
			
			tempA = physicalAddress;
			physicalAddress = physicalAddress >>> channelBits;			//always unsigned shifting
			tempB = physicalAddress << channelBits;
			//System.out.println("Shifted address by " + rankBits + " bits");
			int decodedChan = (int) (tempA ^ tempB);
			return decodedChan;
		}
		
		public static int log2(int a)
		{
			return (int) (Math.log(a)/Math.log(2));
		}

		
		private void processBlockAvailable(AddressCarryingEvent event)
		{
			ArrayList<AddressCarryingEvent> eventsToBeServed = missStatusHoldingRegister.removeRequestsByAddress(event);
			if(eventsToBeServed!=null)
			{
//			System.out.println("MSHR List turns empty!!");
			hits += eventsToBeServed.size();
			noOfRequests += eventsToBeServed.size();
//			System.out.println("eventsTobeServed for "+containingMemSys.tpc_id);
			noOfAccesses+=eventsToBeServed.size();
			sendResponseToWaitingEvent(eventsToBeServed);}
		}
		
		public void sendMemResponse(AddressCarryingEvent eventToRespondTo)
		{
			long delay = eventToRespondTo.getRequestingElement().getLatency();
			
			// ------- Add the network delay when coherent caches are communicating with each other -----------
			// L1 data cache
			if(eventToRespondTo.getRequestingElement().getClass()==Cache.class &&
				((Cache)eventToRespondTo.getRequestingElement()).coherence==CoherenceType.Directory)
			{
				delay += CentralizedDirectoryCache.getNetworkDelay(); 
			}
			
			// Instruction cache connected directly to the lower cache
			// Here, coherence is not there but network delay must be added
			// We are checking for connection to lower cache because if tomorrow we have a private L2 cache, this
			// network delay must not be added.
			if(eventToRespondTo.getRequestingElement().getClass()==Cache.class &&
				((Cache)eventToRespondTo.getRequestingElement()).levelFromTop==CacheType.iCache &&
				((Cache)eventToRespondTo.getRequestingElement()).nextLevel.levelFromTop==CacheType.Lower)
			{
				delay += CentralizedDirectoryCache.getNetworkDelay(); 
			}

						
			noOfResponsesSent++;
			eventToRespondTo.getRequestingElement().getPort().put(
										eventToRespondTo.update(
												eventToRespondTo.getEventQ(),
												delay,
												eventToRespondTo.getProcessingElement(),
												eventToRespondTo.getRequestingElement(),
												RequestType.Mem_Response));
		}
		
		@SuppressWarnings("static-access")
		public void sendMemResponseDirectory(AddressCarryingEvent eventToRespondTo)
		{
			eventToRespondTo.getRequestingElement().getPort().put(
										eventToRespondTo.update(eventToRespondTo.getEventQ(),MemorySystem.getDirectoryCache().getNetworkDelay(),
												eventToRespondTo.getProcessingElement(),eventToRespondTo.getRequestingElement(),
												RequestType.Mem_Response));
		}
		
		private AddressCarryingEvent  putEventToPort(Event event, SimulationElement simElement, RequestType requestType, boolean flag, boolean time  )
		{
			long eventTime = 0;
			if(time) {
				eventTime = simElement.getLatency();
			}
			else {
				eventTime = 1;
			}
			if(flag){
				simElement.getPort().put(event.update(event.getEventQ(),eventTime,this,simElement,requestType));
				return null;
			} else {
				AddressCarryingEvent addressEvent = 	new AddressCarryingEvent( 	event.getEventQ(),eventTime, this,
simElement, requestType,((AddressCarryingEvent)event).getAddress(),((AddressCarryingEvent)event).tpcId ,((AddressCarryingEvent)event).smId); 
				simElement.getPort().put(addressEvent);
				return addressEvent;
			}
		}
		/**
		 * 
		 * PROCESS DIRECTORY WRITE HIT
		 * Change cache line state to modified
		 * Directory state:
		 * invalid -> modified 
		 * modified -> modified,
		 * shared -> modified , invalidate,writeback others
		 * 
		 * */
		private void writeHitUpdateDirectory(int requestingTPC, int requestingSM, long dirAddress, Event event, long address){
			CentralizedDirectoryCache centralizedDirectory = MemorySystem.getDirectoryCache();
			
			long delay = 0;
			if(this.coherence==CoherenceType.Directory) {
				delay += CentralizedDirectoryCache.getNetworkDelay() + centralizedDirectory.getLatencyDelay();
			}
			
			centralizedDirectory.getPort().put(
							new AddressCarryingEvent(
									event.getEventQ(),
									delay,
									this,
									centralizedDirectory,
									RequestType.WriteHitDirectoryUpdate,
									address,
									(event).tpcId, (event).smId));

		}

		/**
		 * PROCESS DIRECTORY READ MISS
		 *Cache block state -> 
		 *invalid -> shared
		 *modified -> shared 
		 *writeback to memory!
		 *
		 * Directory state:
		 *invalid -> shared
		 *modified -> shared
		 * */
		
		private void readMissUpdateDirectory(int requestingTPC,int requestingSM,long dirAddress, Event event, long address) {
			
			CentralizedDirectoryCache centralizedDirectory = MemorySystem.getDirectoryCache();
			
			long delay = 0;
			if(this.coherence==CoherenceType.Directory) {
				delay += CentralizedDirectoryCache.getNetworkDelay() + centralizedDirectory.getLatencyDelay();
			}
			
			centralizedDirectory.getPort().put(
							new AddressCarryingEvent(
									event.getEventQ(),
									delay,
									this,
									centralizedDirectory,
									RequestType.ReadMissDirectoryUpdate,
									address,
									(event).tpcId, (event).smId));
		}

		/**
		 * 
		 * PROCESS DIRECTORY WRITE MISS
		 *cache state modified
		 *directory state:
		 *invalid -> modified
		 *modified -> modified , invalidate, update sharers
		 *shared -> modified, invalidate , update sharers
		 * 
		 * */
		private void writeMissUpdateDirectory(int requestingTPC, int requestingSM, long dirAddress, Event event, long address) {
			CentralizedDirectoryCache centralizedDirectory = MemorySystem.getDirectoryCache();
			long delay = 0;
			if(this.coherence==CoherenceType.Directory) {
				delay += CentralizedDirectoryCache.getNetworkDelay() + centralizedDirectory.getLatencyDelay();
			}
			
			centralizedDirectory.getPort().put(
							new AddressCarryingEvent(
									event.getEventQ(),
									delay,
									this,
									centralizedDirectory,
									RequestType.WriteMissDirectoryUpdate,
									address,
									(event).tpcId, (event).smId));

		}
		
		private void memResponseUpdateDirectory( int requestingTPC, int requestingSM, long dirAddress,Event event, long address )
		{
			CentralizedDirectoryCache centralizedDirectory = MemorySystem.getDirectoryCache();
			long delay = 0;			
			if(this.coherence==CoherenceType.Directory) {
				delay += CentralizedDirectoryCache.getNetworkDelay() + centralizedDirectory.getLatency();
			}
			
			
			centralizedDirectory.getPort().put(
							new AddressCarryingEvent(
									event.getEventQ(),
									delay,
									this,
									centralizedDirectory,
									RequestType.MemResponseDirectoryUpdate,
									address,
									(event).tpcId, (event).smId));
			
		}
		/**
		 * UPDATE DIRECTORY FOR EVICTION
		 * Update directory for evictedLine
		 * If modified, writeback, else just update sharers
		 * */
		private void evictionUpdateDirectory(int requestingCore, long dirAddress,Event event, long address) {
			if(debug && this.levelFromTop == CacheType.L1)System.out.println("tag of line evicted " + (address >>>  this.blockSizeBits)+ " coreID  " + event.tpcId + event.smId);
			CentralizedDirectoryCache centralizedDirectory = MemorySystem.getDirectoryCache();
			long delay = 0;			
			if(this.coherence==CoherenceType.Directory) {
				delay += CentralizedDirectoryCache.getNetworkDelay() + centralizedDirectory.getLatency();
			}
			
			AddressCarryingEvent addrEvent = new AddressCarryingEvent(
					event.getEventQ(),
					delay,
					this,
					centralizedDirectory,
					RequestType.EvictionDirectoryUpdate,
					address,
					(event).tpcId, (event).smId);
			centralizedDirectory.getPort().put(addrEvent);
			
			invalidatePreviousLevelCaches((AddressCarryingEvent)event);			
		}
		
		public long computeTag(long addr) {
			long tag = addr >>> (numSetsBits + blockSizeBits);
			return tag;
		}
		
		public int getSetIdx(long addr)
		{
			int startIdx = getStartIdx(addr);
			return startIdx/assoc;
		}
		
		public int getStartIdx(long addr) {
			long SetMask =( 1 << (numSetsBits) )- 1;
			int startIdx = (int) ((addr >>> blockSizeBits) & (SetMask));
			return startIdx;
		}
		
		public int getNextIdx(int startIdx,int idx) {
			int index = startIdx +( idx << numSetsBits);
			return index;
		}
		
		public CacheLine getCacheLine(int idx) {
			return this.lines[idx];
		}

		public CacheLine access(long addr)
		{
			/* compute startIdx and the tag */
			int startIdx = getStartIdx(addr);
			long tag = computeTag(addr);
			
			/* search in a set */
			for(int idx = 0; idx < assoc; idx++) 
			{
				// calculate the index
				int index = getNextIdx(startIdx,idx);
				// fetch the cache line
				CacheLine ll = getCacheLine(index);
				// If the tag is matching, we have a hit
				if(ll.hasTagMatch(tag) && (ll.getState() != MESI.INVALID)) {
					return  ll;
				}
			}
			return null;
		}
		
		protected void mark(CacheLine ll, long tag)
		{
			ll.setTag(tag);
			mark(ll);
		}
		
		private void mark(CacheLine ll)
		{
			ll.setTimestamp(timestamp);
			timestamp += 1.0;
		}
		
		private void makeCache()
		{
			lines = new CacheLine[numLines];
			for(int i = 0; i < numLines; i++)
			{
				lines[i] = new CacheLine(i);
			}
		}
		
		private int getNumLines()
		{
			long totSize = size * 1024;
			return (int)(totSize / (long)(blockSize));
		}

		protected CacheLine read(long addr)
		{
			CacheLine cl = access(addr);
			if(cl != null) {
				mark(cl);
			}
			return cl;
		}
		
		protected CacheLine write(long addr, Event event)
		{
			CacheLine cl = access(addr);
			if(cl != null) { 
				mark(cl);
				
				if(cl.getState()!=MESI.MODIFIED) {
					
					cl.setState(MESI.MODIFIED);
					//System.out.println(this.coherence);
					// Send request to lower cache.
					if(this.coherence==CoherenceType.None) {
						
						AddressCarryingEvent newEvent  = (AddressCarryingEvent)event.clone();
						newEvent.setAddress(addr);
						sendWriteRequest(newEvent);
					}
					
					// If I have coherence, I should send this request to Directory 
					if(this.coherence == CoherenceType.Directory) {
						writeHitUpdateDirectory(event.tpcId,event.smId, ( addr>>> blockSizeBits ), event.clone(), addr);
					}
				}
			}
			return cl;
		}
		
		public CacheLine fill(long addr, MESI stateToSet) //Returns a copy of the evicted line
		{
			CacheLine evictedLine = null;
    		/* compute startIdx and the tag */
			int startIdx = getStartIdx(addr);
			long tag = computeTag(addr); 
			boolean addressAlreadyPresent = false;
			/* find any invalid lines -- no eviction */
			CacheLine fillLine = null;
			boolean evicted = false;

			for (int idx = 0; idx < assoc; idx++) 
			{
				int nextIdx = getNextIdx(startIdx, idx);
				CacheLine ll = getCacheLine(nextIdx);
				if (ll.getTag() == tag && ll.getState() != MESI.INVALID) 
				{	
					addressAlreadyPresent = true;
					fillLine = ll;
					break;
				}
			}
			
			for (int idx = 0;!addressAlreadyPresent && idx < assoc; idx++) 
			{
				int nextIdx = getNextIdx(startIdx, idx);
				CacheLine ll = getCacheLine(nextIdx);
				if (!(ll.isValid())) 
				{
					fillLine = ll;
					break;
				}
			}
			
			/* LRU replacement policy -- has eviction*/
			if (fillLine == null) 
			{
				evicted = true; // We need eviction in this case
				double minTimeStamp = Double.MAX_VALUE;
				for(int idx=0; idx<assoc; idx++) 
				{
					int index = getNextIdx(startIdx, idx);
					CacheLine ll = getCacheLine(index);
					if(minTimeStamp > ll.getTimestamp()) 
					{
						minTimeStamp = ll.getTimestamp();
						fillLine = ll;
					}
				}
			}

			/* if there has been an eviction */
			if (evicted) 
			{
				evictedLine = (CacheLine) fillLine.clone();
				long evictedLinetag = evictedLine.getTag();
				evictedLinetag = (evictedLinetag << numSetsBits ) + (startIdx/assoc) ;
				evictedLine.setTag(evictedLinetag);
				this.evictions++;
			}

			/* This is the new fill line */
			fillLine.setState(stateToSet);
			fillLine.setAddress(addr);
			mark(fillLine, tag);
			return evictedLine;
		}
	
		public CacheLine processRequest(RequestType requestType, long addr, Event event)
		{
//			System.out.println("request in processrequest of cache "+ requestType );
			CacheLine ll = null;
			if(requestType == RequestType.Cache_Read )  {
				ll = this.read(addr);
			} else if (requestType == RequestType.Cache_Write) {
				ll = this.write(addr, event);
			}
			return ll;
		}
		
		public void populateConnectedMSHR()
		{
			//if mode3
//			((Mode3MSHR)(missStatusHoldingRegister)).populateConnectedMSHR(this.prevLevel);
		}
		
		public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter,
				String componentName) throws IOException {
			EnergyConfig newPower = new EnergyConfig(energy.leakageEnergy,
					energy.readDynamicEnergy);
			EnergyConfig cachePower = new EnergyConfig(newPower, noOfAccesses);
			cachePower.printEnergyStats(outputFileWriter, componentName);
			return cachePower;
		}
		//getters and setters
		public MissStatusHoldingRegister getMissStatusHoldingRegister() {
			return missStatusHoldingRegister;
		}

		public String toString()
		{
			StringBuilder s = new StringBuilder();
			s.append(this.levelFromTop + " : ");
			if(this.levelFromTop == CacheType.L1 || this.levelFromTop == CacheType.iCache)
			{
				s.append(this.containingMemSys.sm_id);
			}
			return s.toString();
		}
}