package emulatorinterface;

import java.util.LinkedList;

public class PerAddressInfoNew {

	LinkedList<Integer> probableInteractors;
	long timeSinceSlept;
	long address;
	boolean timedWait=false;
	boolean on_broadcast = false;
	long broadcastTime = Long.MAX_VALUE;
	boolean on_barrier = false;

	public PerAddressInfoNew(LinkedList<Integer> tentativeInteractors,
			long timeSinceSlept,long address,boolean timedWait) {
		super();
		this.probableInteractors = tentativeInteractors;
		this.timeSinceSlept = timeSinceSlept;
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
