package emulatorinterface;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

//import org.apache.log4j.Logger;


import emulatorinterface.communication.Encoding;
import emulatorinterface.communication.IpcBase;

public class SynchPrimitive implements Encoding {

//	private static final Logger logger = Logger.getLogger(SynchPrimitive.class);
	private static final boolean debugMode = true; 
	LinkedList<SynchType> entries;
	long address;

	public SynchPrimitive(long addressSynchItem, int thread, long time,
			long value, IpcBase ipcType) {
		this.address = addressSynchItem;
		this.entries = new LinkedList<SynchType>();
		entries.add(new SynchType(thread, time, value));
	}

	private int putOnTimedWait(int thread, long time, long value) {
		Hashtable<Integer, ThreadState> stateTable = IpcBase.glTable
		.getStateTable();
		ThreadState ts = stateTable.get(thread);
		LinkedList<Integer> others = new LinkedList<Integer>();
		// add dependencies bothways
		for (ThreadState otherThreads : stateTable.values()) {
			if (otherThreads.lastTimerseen < time && otherThreads.threadIndex!=thread) {
				otherThreads.addDep(address, time, thread);
				others.add(otherThreads.threadIndex);
			}
		}
		if (others.size()!=0) {
			if(debugMode)	
				System.out.println(this.address+"  "+thread+" `"+value+"`, going on a timedWait on "+others.size()+" threads");
			stateTable.get((Integer)thread).countTimedSleep++;
			ts.addressMap.put(address, new PerAddressInfoNew(others, time, address,true));
			entries.add(new SynchType(thread, time, value));
		}
		else {
			if(debugMode)
				System.out.println(this.address+"  "+thread+" `"+value+"`, no TimedWait ");
			ts.addressMap.remove(address);
		}
		return others.size();
	}

	ResumeSleep sigEnter(int thread, long time, long value) {
		boolean done = false;
		//int interactingThread = -1;
		ResumeSleep ret = new ResumeSleep();
		SynchType toBeRemoved1 = null, toBeRemoved2 = null;
		for (SynchType entry : entries) {
			// if a wait enter before
			if (entry.encoding == CONDWAIT && entry.time < time  && entry.thread != thread) {
				for (SynchType exit : entries) {
					// if a wait exit after
					if (exit.encoding == CONDWAIT + 1 && exit.time > time
							&& exit.thread == entry.thread) {
						if(debugMode)
							System.out.println(this.address+" "+thread+" sigenter, got waitenter & exit from "+exit.thread);
						if (done)
							misc.Error.showErrorAndExit ("Duplicate entry in sigEnter");
						//interactingThread = exit.thread;
						ret.addResumer(exit.thread);
						done = true;
						toBeRemoved1 = entry;
						toBeRemoved2 = exit;
						Hashtable<Integer, ThreadState> stateTable = IpcBase.glTable.getStateTable();
						stateTable.get(exit.thread).addressMap.remove(address);
						stateTable.get(thread).addressMap.remove(address);
						break;
					}
				}
			}
			if (done)
				break;
		}

		if (!done) {
			int otherThreads = putOnTimedWait(thread, time, value);
			if (otherThreads==0) {
				System.out.println("SynchPrimitive: Spurious signal received");
				//interactingThread = -2; // means nobody sleeps/resumes
			}
			else ret.addSleeper(thread);
		} else {
			entries.remove(toBeRemoved1);
			entries.remove(toBeRemoved2);
		}

/*		if (interactingThread==-1) ret.addSleeper(thread);
		else if (interactingThread==-2) {}
		else {
			ret.addResumer(interactingThread);
		}
*/		return ret;
	}

	ResumeSleep waitEnter(int thread, long time, long value) {
		//System.out.println(this.address+" "+" waitEnter");
		entries.add(new SynchType(thread, time, value));
		ResumeSleep ret = new ResumeSleep();
		ret.addSleeper(thread);
		return ret;
	}

	ResumeSleep waitExit(int thread, long time, long value) {
		boolean done = false;
		//int interactingThread = -1;
		ResumeSleep ret = new ResumeSleep();
		SynchType toBeRemoved1 = null, toBeRemoved2 = null;
		for (SynchType entry : entries) {
			// if this thread entered
			if (entry.encoding == CONDWAIT && entry.time < time
					&& entry.thread == thread) {
				for (SynchType sig : entries) {
					// if a signal by some other thread found
					if (sig.encoding == SIGNAL && sig.time < time
							&& sig.time > entry.time && sig.thread!=thread) {
						if (done)
							misc.Error.showErrorAndExit("Duplicate entry in wEx");
						if(debugMode)
							System.out.println(this.address+"  "+thread+" waitexit, got signal dep on "+sig.thread);
						//interactingThread = sig.thread;
						ret.addResumer(sig.thread);
						ret.addResumer(thread);
						
						done = true;
						toBeRemoved1 = entry;
						toBeRemoved2 = sig;

						Hashtable<Integer, ThreadState> stateTable = IpcBase.glTable
						.getStateTable();
						stateTable.get(sig.thread).addressMap
						.remove(address);
						stateTable.get(thread).addressMap.remove(address);
						break;
					}
				}
			}
			if (done)
				break;
			//if (entry.encoding == BCAST && entry.time < time)
		}

		if (!done) {
			// XXX the only difference between lock/unlock and wait/signal is here.
			// as we are not going for a timedWait but original wait.
			entries.add(new SynchType(thread, time, value));
			//interactingThread = -2;
		} else {
			entries.remove(toBeRemoved1);
			if (toBeRemoved2!=null) entries.remove(toBeRemoved2);
		}

		/*if (interactingThread==-1) ret.addSleeper(thread);
		else if (interactingThread==-2) {}
		else {
			ret.addResumer(interactingThread);
			ret.addResumer(thread);
		}*/
		return ret;
	}

	ResumeSleep unlockEnter(int thread, long time, long value) {
		boolean done = false;
		ResumeSleep ret = new ResumeSleep();
//		int interactingThread = -1;
		SynchType toBeRemoved1 = null, toBeRemoved2 = null;
		for (SynchType entry : entries) {
			// if a lock enter before
			if (entry.encoding == LOCK && entry.time < time && entry.thread!=thread) {
				for (SynchType exit : entries) {
					// if a lock exit after
					if (exit.encoding == LOCK + 1 && exit.time > time
							&& exit.thread == entry.thread) {
						if(debugMode)
							System.out.println(this.address+"  "+thread+" unlockenter, got lockenter and lockexit from "+exit.thread);
						if (done)
							misc.Error.showErrorAndExit("Duplicate entry in unlockEnter");
						//XXXinteractingThread = exit.thread;
						ret.addResumer(exit.thread);
						done = true;
						toBeRemoved1 = entry;
						toBeRemoved2 = exit;

						Hashtable<Integer, ThreadState> stateTable = IpcBase.glTable
						.getStateTable();
						stateTable.get(exit.thread).addressMap
						.remove(address);
						stateTable.get(thread).addressMap.remove(address);
						break;
					}
				}
			}
			if (done)
				break;
		}

		if (!done) {
			//May never get a corresponding lockenter lockexit
			//so do a timed wait.
			int otherThreads = putOnTimedWait(thread, time, value);
			//XXX if (otherThreads==0) interactingThread = -2;// means nobody sleeps/resumes
			if (otherThreads!=0) ret.addSleeper(thread);

		} else {
			entries.remove(toBeRemoved1);
			entries.remove(toBeRemoved2);
		}

		/*XXX if (interactingThread==-1) ret.addSleeper(thread);
		else if (interactingThread==-2) {}
		else {
			ret.addResumer(interactingThread);
		}*/
		return ret;
	}

	ResumeSleep lockEnter(int thread, long time, long value) {
		if(debugMode) System.out.println(this.address+"  "+thread+" lockenter");
		entries.add(new SynchType(thread, time, value));
		ResumeSleep ret = new ResumeSleep();
		ret.addSleeper(thread);
		return ret;
	}

	ResumeSleep lockExit(int thread, long time, long value) {
		boolean done = false;
		//int interactingThread = -1;
		ResumeSleep ret = new ResumeSleep();
		SynchType toBeRemoved1 = null, toBeRemoved2 = null;
		for (SynchType entry : entries) {
			// if this thread entered
			if (entry.encoding == LOCK && entry.time < time
					&& entry.thread == thread) {
				for (SynchType unlock : entries) {
					// if an unlock by some other thread found
					if (unlock.encoding == UNLOCK && unlock.time < time
							&& unlock.time > entry.time && unlock.thread!=thread) {
						if(debugMode) 
							System.out.println(this.address+"  "+thread+" lockexit, got unlock dep on "+unlock.thread);
						//XXXinteractingThread = unlock.thread;
						ret.addResumer(unlock.thread);
						ret.addResumer(thread);
						done = true;
						toBeRemoved1 = entry;
						toBeRemoved2 = unlock;

						Hashtable<Integer, ThreadState> stateTable = IpcBase.glTable
						.getStateTable();
						stateTable.get(unlock.thread).addressMap
						.remove(address);
						stateTable.get(thread).addressMap.remove(address);
						break;
					}
				}
			}
			if (done)
				break;
		}

		if (!done) {
			// lock enter and lock exit seen but no unlock enter. so
			// wait till others pass its time
			int otherThreads = putOnTimedWait(thread, time, value);
			if (otherThreads==0) ret.addResumer(thread);
			
			/*if (otherThreads == 0) interactingThread = thread;
			else interactingThread = -2;*/
		} else {
			entries.remove(toBeRemoved1);
			if(toBeRemoved2!=null) entries.remove(toBeRemoved2);
		}

/*		if (interactingThread==-1) ret.addSleeper(thread);
		else if (interactingThread==-2) {}
		else if (interactingThread==thread){
			ret.addResumer(thread);
		}
		else {
			ret.addResumer(interactingThread);
			ret.addResumer(thread);
		}*/
		return ret;
	}

	//check if "waitenter before" and "waitexit after/or not available"
	ResumeSleep broadcastResume(long broadcastTime, int thread) {
		ResumeSleep ret = new ResumeSleep();
		ArrayList<SynchType> toBeRemoved = new ArrayList<SynchType>();
		for (SynchType entry : entries) {
			if (entry.encoding == BCAST && entry.thread==thread) {
				ret.addResumer(entry.thread);
				toBeRemoved.add(entry);
			}
			boolean exitPresent = false;
			if (entry.encoding == CONDWAIT && entry.time<broadcastTime && entry.thread!=thread) {
				for (SynchType exit : entries) {
					if (exit.encoding == CONDWAIT+1 && exit.time>broadcastTime && exit.thread==entry.thread) {
						// resume these thread
						ret.addResumer(exit.thread);
						toBeRemoved.add(entry);
					}
					if (exit.encoding == CONDWAIT+1 && exit.time>entry.time) {
						exitPresent = true;
						// this means it is a stale entry
					}
				}
				if (!exitPresent) {
					//no exit, ONLY enter, resume these as well
					ret.addResumer(entry.thread);
					toBeRemoved.add(entry);
				}
			}
		}
		for(SynchType rem : toBeRemoved) {
			entries.remove(rem);
		}
		return ret;
	}

	ResumeSleep broadcastEnter(int thread, long time, long value) {
		if(debugMode)
			System.out.println(this.address+"  "+thread+" broadcastenter");
		ResumeSleep ret = new ResumeSleep(); 
		Hashtable<Integer, ThreadState> stateTable = IpcBase.glTable.getStateTable();
		if (putOnTimedWait(thread, time, value)==0) {
			//return all threads which were waiting to resume
			ArrayList<SynchType> toBeRemoved = new ArrayList<SynchType>();
			for (SynchType entry : entries ) {
				if (entry.encoding == CONDWAIT && entry.time < time && entry.thread != thread) {
					for (SynchType exit : entries) {
						if (exit.encoding == CONDWAIT+1 && exit.time > time && exit.thread==entry.thread) {
							ret.addResumer(exit.thread);
							stateTable.get(exit.thread).addressMap.remove(address);
							toBeRemoved.add(entry);
							toBeRemoved.add(exit);
						}
					}
				}
			}

			stateTable.get(thread).addressMap.remove(address);
			for (SynchType t : toBeRemoved) {
				entries.remove(t);
			}
		}
		else {
			PerAddressInfoNew p = stateTable.get(thread).addressMap.get(address);
			p.on_broadcast = true;
			p.broadcastTime = time;
			// Not all threads have passed in time.
			entries.add(new SynchType(thread, time, value));
			ret.addSleeper(thread);
		}
		return ret;
	}

	public ResumeSleep barrierEnter(int thread, long time, long value) {
		if(debugMode)
			System.out.println(this.address+"  "+thread+" barrierenter");
		entries.add(new SynchType(thread, time, value));
		ResumeSleep ret = new ResumeSleep();
		ret.addSleeper(thread);
		return ret;
	}

	public ResumeSleep barrierExit(int thread, long time, long value) {
		if(debugMode)
			System.out.println(this.address+"  "+thread+" barrierexit");
		Hashtable<Integer, ThreadState> stateTable = IpcBase.glTable.getStateTable();
		ResumeSleep ret = new ResumeSleep();
		if (putOnTimedWait(thread, time, value)==0) {
			ArrayList<SynchType> toBeRemoved = new ArrayList<SynchType>();
			for (SynchType entry : entries ) {
				if (entry.encoding==BARRIERWAIT) {
					ret.addResumer(entry.thread);
					stateTable.get(entry.thread).addressMap.remove(address);
					toBeRemoved.add(entry);
				}
			}

			stateTable.get(thread).addressMap.remove(address);
			for (SynchType t : toBeRemoved) {
				entries.remove(t);
			}
		}
		else {
			PerAddressInfoNew p = stateTable.get(thread).addressMap.get(address);
			p.on_barrier = true;
		}
		return ret;
	}

	public ResumeSleep barrierResume() {
		ResumeSleep ret = new ResumeSleep();
		ArrayList<SynchType> toBeRemoved = new ArrayList<SynchType>();
		for (SynchType entry : entries) {
			if (entry.encoding == BARRIERWAIT) {
				ret.addResumer(entry.thread);
				toBeRemoved.add(entry);
			}
		}

		for(SynchType t : toBeRemoved) {
			entries.remove(t);
		}
		return ret;
	}

}
