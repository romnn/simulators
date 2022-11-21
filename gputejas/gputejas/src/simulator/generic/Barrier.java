/*****************************************************************************
				GPUTejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2010] [Indian Institute of Technology, Delhi]
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

	Contributors:  Eldhose Peter Khushal Sethi
*****************************************************************************/

package generic;

import java.util.Vector;
import generic.LocalClockperSm;
public class Barrier {
	
	long address;
	int numThreads;
	int numThreadsArrived;
	public long time;
	Vector<Integer> blockedThreads;
	Vector<Integer> TreeInfo;
	public Barrier(long address, int numThreads)
	{
		int i;
		this.address = address;
		this.numThreads = numThreads;
		this.numThreadsArrived = 0;
		this.time = 0;
		this.blockedThreads = new Vector<Integer>();
		this.TreeInfo = new Vector<Integer>();
		for(i=0;i<numThreads + 1; i++)
			TreeInfo.add(0);
	}
	
//	public Barrier() {
//		// TODO Auto-generated constructor stub
//		int i;
//		this.numThreadsArrived = 0;
//		this.blockedThreads = new Vector<Integer>();
//		this.TreeInfo = new Vector<Integer>();
//		for(i=0;i<numThreads + 1; i++)
//			TreeInfo.add(0);
//	}

	public long getBarrierAddress()
	{
		return this.address;
	}
	public void incrementThreads()
	{
		if(this.numThreadsArrived == 0)
		this.time = GlobalClock.getCurrentTime();
		this.numThreadsArrived ++;
//		this.blockedThreads.add(tid);
	}
	public void addThread(int tid){
		this.blockedThreads.add(tid);
	}
	public boolean timeToCross()
	{
//		System.out.println("in timetocross numthreads "+ numThreads + " "+ numThreadsArrived + " add : " + this.getBarrierAddress() );
		return(this.numThreads == this.numThreadsArrived);
	}
	public int getNumThreads(){
		return this.numThreads;
	}
	public int getNumThreadsArrived(){
		return this.numThreadsArrived;
	}
	public Vector<Integer> getBlockedThreads(){
		return this.blockedThreads;
	}
	public int blockedThreadSize(){
		return this.blockedThreads.size();
	}
	public boolean containsThread(int tid){
		for(int i:this.blockedThreads){
			if(i==tid)
				return true;
		}
		return false;
	}
	public void resetBarrier(){
		this.numThreadsArrived = 0;
		this.blockedThreads.clear();
	}
	public int getTreeInfo(int node)
	{
		return TreeInfo.elementAt(node);
	}
	public void addTreeInfo(int node)
	{
		TreeInfo.set(node, TreeInfo.get(node) + 1);
	}

	public void setAddress(long l, int i) {
		// TODO Auto-generated method stub
		this.address = l;
		this.numThreads = i;
	}

}
