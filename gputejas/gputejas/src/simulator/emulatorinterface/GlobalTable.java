package emulatorinterface;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import memorysystem.AddressCarryingEvent;


import emulatorinterface.communication.Encoding;
import emulatorinterface.communication.IpcBase;
import emulatorinterface.communication.Packet;
import generic.Barrier;
import generic.BarrierTable;
import generic.CoreBcastBus;
import generic.RequestType;

class ResumeSleep {
	ArrayList<Integer> resume=new ArrayList<Integer>();
	ArrayList<Integer> sleep=new ArrayList<Integer>();
	
	long barrierAddress;
	
	void addSleeper(int t){
		this.sleep.add(t);
	}
	
	void addResumer(int t){
		this.resume.add(t);
	}
	
	int getNumResumers() {
		return this.resume.size();
	}
	
	int getNumSleepers() {
		return this.sleep.size();
	}

	public void merge(ResumeSleep other) {
		for (int res : other.resume) {
			this.addResumer(res);
		} 
		for (int slp : other.sleep) {
			this.addSleeper(slp);
		} 
	}
	public void setBarrierAddress(long add){
		this.barrierAddress = add;
	}
	
}

public final class GlobalTable implements Encoding {
	
	private Hashtable<Long, SynchPrimitive> synchTable;
	private Hashtable<Integer, ThreadState> stateTable;
	private IpcBase ipcType;
	public GlobalTable(IpcBase ipcType) {
		this.ipcType = ipcType;
		this.synchTable = new Hashtable<Long, SynchPrimitive>();
		this.stateTable = new Hashtable<Integer, ThreadState>();
//		this.coreBcastBus = coreBcastBus;
	}

	public Hashtable<Long, SynchPrimitive> getSynchTable() {
		return synchTable;
	}
	public Hashtable<Integer, ThreadState> getStateTable() {
		return stateTable;
	}

	public void setStateTable(Hashtable<Integer, ThreadState> stateTable) {
		this.stateTable = stateTable;
	}


	// returns -2 if no thread needs to be slept/resumed.
	// returns -1 if 'this' thread needs to sleep
	// o/w returns otherThreadsId which will now resume
	@SuppressWarnings("unused")
	public ResumeSleep update(long addressSynchItem, int thread, long time,
			long value) {

		
		SynchPrimitive s;
		if (synchTable.containsKey(addressSynchItem))
			s = (SynchPrimitive)synchTable.get(addressSynchItem);
		else {
			s = new SynchPrimitive(
					addressSynchItem, thread, time, value, ipcType);
				synchTable.put(addressSynchItem, s);
		}

		ResumeSleep ret = new ResumeSleep();
		switch ((int)value) {
		case (BCAST):
//			ret = s.broadcastEnter(thread,time,value);
			break;
		case (SIGNAL):
//			ret = s.sigEnter(thread, time, value);
			break;
		case (LOCK):
//			ret = s.lockEnter(thread, time, value);
			break;
		case (UNLOCK):
//			ret = s.unlockEnter(thread, time, value);
			break;
		case (JOIN):
			//TODO
			break;
		case (CONDWAIT):
//			ret = s.waitEnter(thread, time, value);
			break;
		case (BARRIERWAIT):
			Barrier bar = BarrierTable.barrierList.get(addressSynchItem);
			if(bar != null){ //to track the condition that the barrier is already opened
				
				while(bar.blockedThreadSize() == bar.getNumThreads()){ //to track re initialization of barrier
					bar = BarrierTable.barrierList.get(++addressSynchItem);
					if(bar == null){
						addressSynchItem = BarrierTable.barrierCopy(--addressSynchItem);
						bar = BarrierTable.barrierList.get(addressSynchItem);
						System.out.println(thread + " bar was null");
						//return ret;
					}
				}
				while(bar.containsThread(thread)){                     //to block the same thread entering barrier twice
					int i = bar.getNumThreads();
					//addressSynchItem = BarrierTable.barrierCopy(addressSynchItem);
					System.out.println("already contains " + thread);
					bar = BarrierTable.barrierList.get(++addressSynchItem);
					if(bar == null){
						bar = new Barrier(addressSynchItem, i);
						BarrierTable.barrierList.put(addressSynchItem, bar);
						break;
					}
				}
				if(bar==null){
					System.err.println("barrier not yet initialized");
					System.exit(0);
				}
			//	System.out.println("             thread " + thread + " waiting on " +bar.getBarrierAddress() +" " + addressSynchItem);
			//	System.out.println(BarrierTable.barrierList.get(addressSynchItem).getBlockedThreads());
				bar.addThread(thread);
			//	System.out.println(BarrierTable.barrierList.get(addressSynchItem).getBlockedThreads());
				ret.setBarrierAddress((long)addressSynchItem);
				ret.addSleeper(thread);
				
				return ret;
			}
			break;
		case (BCAST + 1):
			// TODO
			break;
		case (SIGNAL + 1):
			// ignore
			break;
		case (LOCK + 1):
//			ret = s.lockExit(thread, time, value);
			break;
		case (UNLOCK + 1):
			// ignore
			break;
		case (JOIN + 1):
			break;
		case (CONDWAIT + 1):
//			ret = s.waitExit(thread, time, value);
			break;
		case (BARRIERWAIT + 1):
			break;
		}
		
		return null;
		
	}
//	
//	ResumeSleep resumePipelineTimer(int tidToResume) {
//		ResumeSleep ret = new ResumeSleep();
//		int numResumes=IpcBase.glTable.getStateTable().get(tidToResume).countTimedSleep;
//		IpcBase.glTable.getStateTable().get(tidToResume).countTimedSleep=0;
//		for (int i=0; i<numResumes; i++) {
//			//System.out.println("Resuming by timer"+tidToResume);
//			//this.pipelineInterfaces[tidToResume].resumePipeline();
//			ret.addResumer(tidToResume);
//		}
//		if (ret==null) misc.Error.shutDown("resumePipelineTimer returned null"); 
//		return ret;
//	}
//
//	ResumeSleep tryResumeOnWaitingPipelines(int signaller, long time) {
//		ResumeSleep ret = new ResumeSleep();
//		Hashtable<Integer, ThreadState> stateTable = IpcBase.glTable.getStateTable();
//		Hashtable<Long, SynchPrimitive> synchTable = IpcBase.glTable.getSynchTable();
//		ThreadState signallingThread = stateTable.get(signaller);
//		signallingThread.lastTimerseen = time;
//
//		for (PerAddressInfoNew pai : signallingThread.addressMap.values()) {
//			for (Iterator<Integer> iter = pai.probableInteractors.iterator(); iter.hasNext();) {
//				int waiter = (Integer)iter.next();
//				ThreadState waiterThread = stateTable.get(waiter);
//				if (waiterThread.isOntimedWaitAt(pai.address)) {
//					//TODO if multiple RunnableThreads then this should be synchronised
//					if (time>=waiterThread.timeSlept(pai.address)) {
//						//Remove dependencies from both sides.
//						iter.remove();
//						waiterThread.removeDep(signaller);
//						if (!waiterThread.isOntimedWait()) {
//							//TODOthis means waiter got released from a timedWait by a timer and not by synchPrimitive.
//							//this means that in the synchTable somewhere there is a stale entry of their lockEnter/Exit
//							// or unlockEnter. which needs to removed.
//							// flushSynchTable();
//							/*							System.out.println(waiter+" pipeline is resuming by timedWait from"+signaller
//									+" num of Times"+stateTable.get(waiter).countTimedSleep);
//							 */
//							ret = resumePipelineTimer(waiter);
//							 PerAddressInfoNew p = waiterThread.addressMap.get(pai.address);
//							 if (p!=null) {
//								 if (p.on_broadcast) {
//									 ret.merge(synchTable.get(pai.address).broadcastResume(p.broadcastTime,waiter));
//									 p.on_broadcast = false;
//									 p.broadcastTime = Long.MAX_VALUE;
//								 }
//								 else if (p.on_barrier) {
//									 ret.merge(synchTable.get(pai.address).barrierResume());
//									 p.on_barrier = false;
//								 }
//							 }
//						}
//					}
//				}
//				else {
//					// this means that the thread was not timedWait anymore as it got served by the synchronization
//					// it was waiting for.
//					iter.remove();
//				}
//			}
//		}
//		return ret;
//	}

}
