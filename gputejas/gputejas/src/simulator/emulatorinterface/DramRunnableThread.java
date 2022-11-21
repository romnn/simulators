package emulatorinterface;

import java.io.FileInputStream;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.Hashtable;

import net.NOC.CONNECTIONTYPE;

import config.SimulationConfig;
import config.SmConfig;
import config.SystemConfig;
import config.TpcConfig;

import main.ArchitecturalComponent;
import main.Main;
import memorysystem.Cache;
import pipeline.GPUpipeline;
import emulatorinterface.ThreadBlockState.blockState;
import emulatorinterface.communication.Encoding;
import emulatorinterface.communication.IpcBase;
import emulatorinterface.communication.Packet;
import emulatorinterface.communication.filePacket.SimplerFilePacket;
import emulatorinterface.translator.x86.instruction.InstructionClass;
import emulatorinterface.translator.x86.instruction.FullInstructionClass;

import emulatorinterface.translator.x86.objparser.ObjParser;
import generic.*;

public class DramRunnableThread implements Runnable,Encoding {
	public Thread t;
	long currentTime,update;
	long RAMclock;
	long counter1=0;
	long counter2=0;
	long CoreClock;
	/* Note: DRAM simulation
	Order of execution must be maintained for cycle accurate simulation.
	Order is:
	MainMemoryController.oneCycleOperation()
	processEvents()   [called from within oneCycleOperation of pipelines]
	MainMemoryController.enqueueToCommandQ();
	*/
	public DramRunnableThread(String threadName)
	{
//	CoreClock = SmConfig.frequency * 1000000;
		update=0;
//		if(SystemConfig.memControllerToUse==true)
//			RAMclock = (long) (1 / (SystemConfig.mainMemoryConfig.tCK) * 1000000000);
		RAMclock=200;
		t=(new Thread(this, threadName));
	}
	public void run() {	
//		System.out.println("Sanity check"+!Main.allfinished);
	while(!Main.allfinished)
		{
		//GlobalClock.getCurrentTime();
		try {
            Thread.sleep(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
		currentTime=GlobalClock.getCurrentTime();
//		if( currentTime % ArchitecturalComponent.reconfInterval == 0 && update < currentTime && SystemConfig.nocConfig.ConnType == CONNECTIONTYPE.OPTICAL)
//		{
//			update = currentTime;
//			ArchitecturalComponent.laserReconfiguration();
//		} 
//		System.out.print(".");
//		System.out.println("The dram time is"+counter1+"and the global time is"+GlobalClock.getCurrentTime());	
		if(counter1<GlobalClock.getCurrentTime())
		{
			for(int k=0;k<SystemConfig.mainMemoryConfig.numChans;k++){
//				System.out.println("In the For Loop");
		ArchitecturalComponent.getMainMemoryDRAMController(null,k).oneCycleOperation();
		ArchitecturalComponent.getMainMemoryDRAMController(null,k).enqueueToCommandQ();		
		System.out.flush();
		counter1 += RAMclock;}
//			if(counter1-GlobalClock.getCurrentTime()>150)
//			{ try {
//		            Thread.sleep(10);
//		        } catch (Exception e) {
//		            e.printStackTrace();
//		        }}
		//important - one cycle operation for dram must occur before events are processed
		}
		}
	}
}
