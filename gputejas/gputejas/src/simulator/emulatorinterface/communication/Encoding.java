package emulatorinterface.communication;



// change encoding.h if any change here.
public interface Encoding {
	
	static final int THREADCOMPLETE = -1;
	static final int SUBSETSIMCOMPLETE = -2;
	
	static final int MEMREAD  = 2;
	static final int MEMWRITE = 3;
	static final int TAKEN = 4;
	static final int NOTTAKEN = 5;
	static final int REGREAD = 6;
	static final int REGWRITE = 7;

	static final int TIMER = 8;
	
	// synchronization values should be between SYNCHSTART AND SYNCHEND
	// The values are for corresponding "enter". For "exit" the value is
	// 1+enter value. i.e. for example LOCK enter is 14 and LOCK exit is 15
	static final int SYNCHSTART = 9;
	static final int BCAST = 10;
	static final int SIGNAL = 12;
	static final int LOCK = 14;
	static final int UNLOCK = 16;
	static final int JOIN = 18;
	static final int CONDWAIT = 20;
	static final int BARRIERWAIT = 22;
	static final int SYNCHEND = 24;
	static final int BARRIERINIT = 26; 
	
	// An instruction can have two assembly
	static final int ASSEMBLY = 27;
	static final int INSTRUCTION = 28;
	
	static final int INTERRUPT = 30;
	static final int PROCESS_SWITCH = 31;
	static final int DOM_SWITCH = 32;
	static final int CPL_SWITCH = 34;
	static final int PARENT_SPAWN = 35;
	static final int CHILD_START = 36;

}
