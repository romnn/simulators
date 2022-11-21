package emulatorinterface;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

class PerAddressInfo {

	LinkedList<Integer> probableInteractors;
	long timeSinceSlept;
	long address;
	boolean timedWait=false;


	public PerAddressInfo(LinkedList<Integer> tentativeInteractors,
			long time,long address,boolean timedWait) {
		super();
		this.probableInteractors = tentativeInteractors;
		this.timeSinceSlept = time;
		this.address = address;
		this.timedWait = timedWait;
	}

	public LinkedList<Integer> getTentativeInteractors() {
		return probableInteractors;
	}

	public void setTentativeInteractors(LinkedList<Integer> tentativeInteractors) {
		this.probableInteractors = tentativeInteractors;
	}

	public long getTime() {
		return timeSinceSlept;
	}

	public void setTime(long time) {
		this.timeSinceSlept = time;
	}



}

public class ThreadState {
	int threadIndex;
	int countTimedSleep=0;
	long lastTimerseen=(long)-1>>>1;
	//boolean timedWait=false;
	HashMap <Long,PerAddressInfoNew> addressMap = new HashMap<Long,PerAddressInfoNew>();

	public ThreadState(int tid){
		this.threadIndex = tid;
	}

	public void removeDep(long address) {
		addressMap.remove((Long)address);
	}

	public void removeDep(int tidApp) {
		//for (PerAddressInfo pai : addressMap.values()) {
		for (Iterator<PerAddressInfoNew> iter = addressMap.values().iterator(); iter.hasNext();) {
			PerAddressInfoNew pai = (PerAddressInfoNew) iter.next();
			pai.probableInteractors.remove((Integer)tidApp);
			if (pai.probableInteractors.size()==0) {
				iter.remove();
			}
		}
	}

	public void addDep(long address, long time, int thread) {
		PerAddressInfoNew opai;
		if ((opai = this.addressMap.get(address)) != null) {
			opai.probableInteractors.add(thread);
			//opai.timeSinceSlept = time;
		} else {
			LinkedList<Integer> th = new LinkedList<Integer>();
			th.add(thread);
			this.addressMap.put(address,
					new PerAddressInfoNew(th, -1, address, false));
		}

	}
	public long timeSlept(long address) {
		return addressMap.get(address).timeSinceSlept;
	}

	public boolean isOntimedWait() {
		boolean ret = false;
		for (PerAddressInfoNew pai : addressMap.values()) {
			ret = ret || pai.timedWait;
		}
		return ret;
	}

	public boolean isOntimedWaitAt(long address) {
		if (addressMap.get(address) == null) return false;
		else return addressMap.get(address).timedWait;
	}

	}
