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
/*
 * This file declares some parameters which is common to all IPC mechanisms. Every IPC mechanism
 * inherits this class and implements the functions declared. Since Java has runtime binding
 * so the corresponding methods will be called.
 * 
 * MAXNUMTHREADS - The maximum number of java threads running
 * EMUTHREADS - The number of emulator threads 1 java thread is reading from
 * COUNT - this many number of packets is allocated in the shared memory for each 
 * 		   application/emulator thread 
 * */

package emulatorinterface.communication;
import java.util.concurrent.Semaphore;


import emulatorinterface.BlockState;
import emulatorinterface.GlobalTable;

import emulatorinterface.SimplerRunnableThread;
import generic.CircularPacketQueue;

public abstract class IpcBase {

	// Must ensure that MAXNUMTHREADS*EMUTHREADS == MaxNumThreads on the PIN side
	// Do not move it to config file unless you can satisfy the first constraint
	public int memMapping;

	// state management for reader threads
	public boolean javaThreadTermination;
	public boolean javaThreadStarted;

	// number of instructions read by each of the threads
	// public long[] numInstructions = new long[MaxNumJavaThreads];

	// to maintain synchronization between main thread and the reader threads
	public Semaphore free = new Semaphore(0, true);

	
	

	// Initialise structures and objects
	public IpcBase () {
			javaThreadTermination=false;
			javaThreadStarted=false;
	}

	public abstract void initIpc();

	/*** start, finish, isEmpty, fetchPacket, isTerminated ****/
	

	// returns the numberOfPackets which are currently there in the stream for tidApp
	//the runnable thread does not require the numPackets in stream
	//public abstract int numPackets(int tidApp);

	// fetch one packet for tidApp from index.
	// fetchPacket creates a Packet structure which will strain the garbage collector.
	// Hence, this method is no longer supported.
	//public abstract Packet fetchOnePacket(int tidApp, int index);
	
	//public abstract int fetchManyPackets(int tidApp, int readerLocation, int numReads,ArrayList<Packet> fromPIN);
	public abstract int fetchManyPackets(SimplerRunnableThread runner,int BlockId, CircularPacketQueue fromEmulator, BlockState threadParam);
	
	//public abstract long update(int tidApp, int numReads);
	// The main thread waits for the finish of reader threads and returns total number of 
	// instructions read

	// return the total packets produced by PIN till now
	//public abstract long totalProduced(int tidApp);
	public static GlobalTable glTable;

	public abstract void errorCheck(int tidApp, long totalReads);

	// Free buffers, free memory , deallocate any stuff.
	public void finish() {
		System.out.println("Implement finish in the IPC mechanism");
	}

	
}