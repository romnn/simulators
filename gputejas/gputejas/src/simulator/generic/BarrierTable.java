package generic;

import emulatorinterface.communication.Packet;

import java.util.Enumeration;
import java.util.Hashtable;

public class BarrierTable {
	
	public static Hashtable<Long, Barrier> barrierList = new Hashtable<Long, Barrier>();
	
	public BarrierTable(){
//		this.barrierList = new Hashtable<Long, Barrier>();
	}
//	
//	public static void barrierListAdd(Packet packet){
//		Barrier barrier = new Barrier(packet.tgt, (int) packet.ip);
//		while(BarrierTable.barrierList.get(packet.tgt) != null){  //checking for re initialization
//			if(BarrierTable.barrierList.get(packet.tgt).getNumThreadsArrived() == 0){
//				barrierList.remove(packet.tgt);
//				barrierList.put(packet.tgt, barrier);
//				System.out.println("barrier is already present");
//				return;
//			}
//			packet.tgt++;
//		}
//		barrierList.put(packet.tgt, barrier);
//	}
	public static void barrierReset(long add){
		Barrier bar = barrierList.remove(add);
		bar.resetBarrier();
		barrierList.put(add, bar);
	}
	public static long barrierCopy(long add){
		Barrier bar = barrierList.get(add);
		
		while(true){
			if(barrierList.get(add + 1) != null){
				bar = barrierList.get(++add);
			}
			else
				break;
		}
		Barrier bar_new = new Barrier(++add, bar.getNumThreads());
		barrierList.put(add, bar_new);
		return add;
	}
	

}
