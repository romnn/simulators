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
package emulatorinterface;

public class ThreadBlockState {
	public enum blockState{LIVE, BLOCK, INACTIVE};
	blockState BlockState;
	int encode;
	public ThreadBlockState() {
		// TODO Auto-generated constructor stub
		this.BlockState=blockState.INACTIVE;
		encode=-1;
	}
	blockState getState()
	{
		return BlockState;
	}
	/**
	 * 
	 * @param encode
	 * LOCK	14,15
	 * JOIN	18,19
	 * CONDWAIT	20,21
	 * BARRIERWAIT	22,23
	 */
	public void gotBlockingPacket(int encode)
	{
		switch(BlockState)
		{
			case LIVE: this.encode=encode; BlockState=blockState.BLOCK;break;
			case BLOCK: this.encode=encode;break;
			case INACTIVE: this.encode=encode; BlockState=blockState.BLOCK;break;
		}
	}
	/**
	 * Thread started receiving packets after blockage
	 */
	public void gotUnBlockingPacket()
	{
		switch(BlockState)
		{
			case LIVE: break;
			case BLOCK: this.encode=-1;BlockState=blockState.LIVE;break;
			case INACTIVE: this.encode=-1;BlockState=blockState.LIVE;break;
		}
	}
	
	public void gotLive()
	{
		if(BlockState==blockState.INACTIVE)
		{
			BlockState=blockState.LIVE;
		}
	}

}
