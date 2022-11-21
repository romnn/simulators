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
package emulatorinterface;

import generic.GenericCircularQueue;
import generic.Instruction;

public class BlockState {
	
	boolean finished;
	public boolean started;
	public long no_of_instructions;
	public boolean isFirstPacket;
	boolean fetchStatus; //true when #{packets} fetched by T_java (for T_app) > threshold else false
	long totalRead;
	public long tot_cycles;
	
	// We are assuming that one assembly instruction can have atmost 50 micro-operations.
	// Its an over-estimation.
	GenericCircularQueue<Instruction> outstandingMicroOps;
	
	EmulatorPacketList packetList;
	
	public BlockState()
	{
		finished = false;
		started = false;
		no_of_instructions = 0;
		isFirstPacket = true;
		totalRead = 0;
		tot_cycles=0;
		outstandingMicroOps = new GenericCircularQueue<Instruction>(Instruction.class, 50);
		packetList = new EmulatorPacketList();
	}
	
	public void checkStarted() {
		if (this.isFirstPacket) {
			this.started = true;
		}
	}
}
