package pipeline;

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
import net.NOC.CONNECTIONTYPE;
import config.SystemConfig;
import main.ArchitecturalComponent;
import generic.EventQueue;
import generic.GenericCircularQueue;
import generic.GlobalClock;
import generic.Instruction;
import generic.SM;

public class GPUpipeline {

	SM sm;
	public GPUExecutionEngine containingExecutionEngine;
	EventQueue eventQ;
	int coreStepSize;
	
	public GPUpipeline(SM sm, EventQueue eventQueue) {
		this.sm= sm;
		containingExecutionEngine = (GPUExecutionEngine)sm.getExecEngine();
		this.eventQ = eventQueue;
		this.coreStepSize = sm.getStepSize();	//Not Necessary. Global clock hasn't been initialized yet
												//So, step sizes of the cores hasn't been set.
												//It will be set when the step sizes of the cores will be set
		}

	static long update = 0;	
	public void oneCycleOperation() {
//		long start = System.currentTimeMillis();
	
		long start = System.currentTimeMillis();
	    
		
		long currentTime = GlobalClock.getCurrentTime();
		
		if(currentTime % ArchitecturalComponent.reconfInterval == 0 && update < currentTime && SystemConfig.nocConfig.ConnType == CONNECTIONTYPE.OPTICAL)
		{
			update = currentTime;
			ArchitecturalComponent.laserReconfiguration();
		} 
		
		containingExecutionEngine.WarpTable();
		containingExecutionEngine.writeback();
//		System.out.println("the size of the WarpTable is"+containingExecutionEngine.WarpTable.size()+"for sm"+sm.getSM_number());
		if(containingExecutionEngine.WarpTable.size()>0 ){
			execute();
		}
		drainEvents();
		if(!containingExecutionEngine.getScheduleUnit().scheduleExecuteLatch.isFull())
		{readoperands();} 		

		if(containingExecutionEngine.getScheduleUnit().completeWarpPipeline.size()>0){
//			System.out.println(containingExecutionEngine.getScheduleUnit().completeWarpPipeline.size()+"is the size of Scheduled pipeline here");
			schedule();
		}
//		long end = System.currentTimeMillis();
//		main.Main.runners[Integer.parseInt(Thread.currentThread().getName())].pipe_time += (end - start);
//		if(main.Main.runners[Integer.parseInt(Thread.currentThread().getName())].mem_flag)
//		{
//			main.Main.runners[Integer.parseInt(Thread.currentThread().getName())].mem_time += (end - start);
//		}
//		
		
	}
	
	private void drainEvents() {
		// TODO Auto-generated method stub
		eventQ.processEvents();
		
	}


	public void schedule(){
		containingExecutionEngine.getScheduleUnit().performSchedule(this);		
	}
	public void execute(){
		containingExecutionEngine.getExecuteUnit().performExecute(this);
	}

	public void readoperands(){
		// TODO Check this also
	containingExecutionEngine.getOpndColl().step(this);
	}
	
	public boolean isExecutionComplete() {
		return (containingExecutionEngine.getExecutionComplete());
	}

	
	public void setcoreStepSize(int stepSize) {
		this.coreStepSize=stepSize;
		
	}

	
	public int getCoreStepSize() {
		return this.sm.getStepSize();
	}

	
	public void resumePipeline() {
	}

	
	public SM getCore() {
		return sm;
	}

	
	public boolean isSleeping() {
		return false;
	}

	
	public void setTimingStatistics() {
		
	}

	
	public void setPerCoreMemorySystemStatistics() {
		
	}

	
	public void setExecutionComplete(boolean status) {
		containingExecutionEngine.setExecutionComplete(status);
	}

	
	public void adjustRunningThreads(int adjval) {
		
	}

	
	public void setInputToPipeline(
			GenericCircularQueue<Instruction>[] inputToPipeline) {
		this.containingExecutionEngine.setInputToPipeline(inputToPipeline);
		
	}

	
	public long getBranchCount() {
		return 0;
	}

	
	public long getMispredCount() {
		return 0;
	}

	
	public long getNoOfMemRequests() {
		return 0;
	}

	
	public long getNoOfLoads() {
		return 0;
	}

	
	public long getNoOfStores() {
		return 0;
	}

	
	public long getNoOfValueForwards() {
		return 0;
	}

	
	public long getNoOfTLBRequests() {
		return 0;
	}

	
	public long getNoOfTLBHits() {
		return 0;
	}

	
	public long getNoOfTLBMisses() {
		return 0;
	}

	
	public long getNoOfL1Requests() {
		return 0;
	}

	
	public long getNoOfL1Hits() {
		return 0;
	}

	
	public long getNoOfL1Misses() {
		return 0;
	}

	
	public long getNoOfIRequests() {
		return 0;
	}

	
	public long getNoOfIHits() {
		return 0;
	}

	
	public long getNoOfIMisses() {
		return 0;
	}

	
	public void setBlockId(int tidBlock) {
		this.bid=tidBlock;
	}
	public int bid;
}
