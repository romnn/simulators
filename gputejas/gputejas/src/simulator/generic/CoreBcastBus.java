package generic;

import java.util.Vector;
import generic.LocalClockperSm;
import emulatorinterface.SimplerRunnableThread;
import main.ArchitecturalComponent;
import memorysystem.AddressCarryingEvent;

public class CoreBcastBus extends SimulationElement{

	public Vector<Integer> toResume =  new Vector<Integer>();

	public CoreBcastBus() {
		super(PortType.Unlimited, 1, 1, 1, 1);
	}

	public void addToResumeCore(int id){
		this.toResume.add(id);
	}

	@Override
	public void handleEvent(EventQueue eventQ, Event event) {
		int tpcId=((AddressCarryingEvent)event).tpcId;
		int smId =((AddressCarryingEvent)event).smId;
		System.out.println("handle event of core broadcast bus called");
		if(event.getRequestType() == RequestType.TREE_BARRIER_RELEASE){
			
			
			long barAddress = ((AddressCarryingEvent)event).getAddress();
			ArchitecturalComponent.getCores()[((AddressCarryingEvent)event).tpcId][((AddressCarryingEvent)event).smId].activatePipeline();
			if((((AddressCarryingEvent)event).tpcId) * 2 < BarrierTable.barrierList.get(barAddress).numThreads){
				this.getPort().put(new AddressCarryingEvent(
						0,eventQ,
						1,
						this, 
						this, 
						RequestType.TREE_BARRIER_RELEASE, 
						barAddress,
						tpcId,smId,((AddressCarryingEvent)event).getSourceId(),((AddressCarryingEvent)event).getDestinationId()));
				this.getPort().put(new AddressCarryingEvent(
						0,eventQ,
						1,
						this, 
						this, 
						RequestType.TREE_BARRIER_RELEASE, 
						barAddress,
						tpcId,smId,((AddressCarryingEvent)event).getSourceId(),((AddressCarryingEvent)event).getDestinationId()));
			}
		}
		else if(event.getRequestType() == RequestType.TREE_BARRIER){

			long barAddress = ((AddressCarryingEvent)event).getAddress();
			 tpcId = ((AddressCarryingEvent)event).tpcId;
			 smId = ((AddressCarryingEvent)event).smId;

			Barrier bar = BarrierTable.barrierList.get(barAddress);
			int numThreads = bar.getNumThreads();
			int level = (int) (Math.log(numThreads + 1)/Math.log(2));
//			if(coreId >= Math.pow(2, level - 1) && coreId < Math.pow(2,level)){
				this.getPort().put(new AddressCarryingEvent(
						0,eventQ,
						1,
						this, 
						this, 
						RequestType.TREE_BARRIER,
						barAddress,
						tpcId,smId,((AddressCarryingEvent)event).getSourceId(),((AddressCarryingEvent)event).getDestinationId()));
			}
			else{
				//System.out.println("Core Id : " + coreId );
//				bar.addTreeInfo(coreId);
//				if(bar.getTreeInfo(coreId) == 3){
//					if(coreId == 1){
						//	BarrierTable.barrierReset(barAddress);
				long barAddress = ((AddressCarryingEvent)event).getAddress();
						this.getPort().put(new AddressCarryingEvent(0,eventQ,0,this,this,RequestType.TREE_BARRIER_RELEASE,barAddress,
								tpcId,smId,((AddressCarryingEvent)event).getSourceId(),((AddressCarryingEvent)event).getDestinationId()));
					}
//					else{
//						this.getPort().put(new AddressCarryingEvent(0,eventQ,1,	this,this,RequestType.TREE_BARRIER, 
//						barAddress,tpcId,smId,((AddressCarryingEvent)event).getSourceId(),((AddressCarryingEvent)event).getDestinationId()));
//					}
//				}
//			}
//		}
		 if(event.getRequestType() == RequestType.PIPELINE_RESUME){
			for(int i : toResume){
				ArchitecturalComponent.getCores()[tpcId][smId].activatePipeline();
				//SimplerRunnableThread.setThreadState(i,false);
			}
			toResume.clear();
		}
		else{
			ArchitecturalComponent.getCores()[tpcId][smId].sleepPipeline();
		}
	}

}
