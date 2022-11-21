package emulatorinterface;

public class SynchType {

	public SynchType(int thread, long time, long value) {
		super();
		this.thread = thread;
		this.time = time;
		this.encoding = value;
	}

	int thread;
	long time;
	long encoding; // same as in Encoding.java
}
