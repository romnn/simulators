package dram;

import java.util.ArrayList;

import config.MainMemoryConfig;
import dram.BankState.CurrentBankState;
import dram.MainMemoryBusPacket.BusPacketType;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.LocalClockperSm;
import generic.RequestType;
import generic.SimulationElement;

import java.util.ArrayList;

import main.Main;
import misc.Error;
import config.MainMemoryConfig;
import dram.BankState.CurrentBankState;

public class Rank extends SimulationElement{
	
	int id;
	int incomingWriteBank;
	int incomingWriteRow;
	int incomingWriteColumn;
	boolean isPowerDown;


	MainMemoryBusPacket outgoingDataPacket;
	//this can't be static as we can have multiple mem controllers having separate ram arrays
	//static long busFreeTime ;
	boolean refreshWaiting;

	BankState bankStates[];
	
	MainMemoryConfig mainMemoryConfig;
	
	//parent memory controller
	MainMemoryDRAMController parentMemContrlr;

	Rank(MainMemoryConfig mainMemoryParameters, int id, MainMemoryDRAMController parentMemContrlr){
		super(mainMemoryParameters.getRankPortType(), mainMemoryParameters.getRankNumPorts(), mainMemoryParameters.getRankOccupancy(), mainMemoryParameters.getRankLatency(), mainMemoryParameters.getRankOperatingFrequency());
		//System.out.println("Constructing a rank!");
		refreshWaiting=false;
		bankStates = new BankState[mainMemoryParameters.numBanks];
		
		for(int i=0; i < mainMemoryParameters.numBanks; i++)
		{
			bankStates[i] = new BankState();
		}
		
		this.mainMemoryConfig = mainMemoryParameters;
		this.id = id;
		this.parentMemContrlr = parentMemContrlr;
		
	}

	int getId(){
		return this.id;
	}

	void setId(int id){
		this.id = id;
	}

	
	public synchronized void handleEvent(EventQueue eventQ, Event e)		//basically a transaction of the receive from bus function
	{
		RamBusAddressCarryingEvent event = (RamBusAddressCarryingEvent) e;
		//cast event to ram address carrying event
		MainMemoryBusPacket b=event.getBusPacket().Clone();
		//BusPacketType busPacketType = b.getBusPacketType();
		//long addr = event.getAddress();
		//RequestType requestType = event.getRequestType();
		long currentTime = GlobalClock.getCurrentTime(); 		//Assumption: time will never change while handling an event
		
//		System.out.println("Handling rank event scheduled for time " + event.getEventTime());
		b.printPacket();
		//bankStates[b.bank].printState();
//		System.out.println("Time:" + currentTime);
		
		//Main.debugPrinter.print("\n\nHandling rank event.....\n");
		//Main.debugPrinter.print("Received packet..\n");
		//b.printPacketToFile();
		//Main.debugPrinter.print("\n");
		
		
		//for TEST
		/*if(mainMemoryConfig.DEBUG_BUS)
		{
		if(e.getRequestType() != RequestType.Column_Read_Complete)		//it's not an actual bus receive in this case
			{
			Test.outputLog.print(" -- R" + this.id + " Receiving On Bus at Clock Cycle "+ GlobalClock.getCurrentTime() +"    : ");
			b.printTest();
			}
		}*/
		
		switch(b.busPacketType){
		
		case READ:
			//make sure read is allowed
			if (bankStates[b.bank].currentBankState != CurrentBankState.ROW_ACTIVE || currentTime < bankStates[b.bank].nextRead || b.row != bankStates[b.bank].openRowAddress)
			{
				break;
//			System.out.println("is row_active ?"+bankStates[b.bank].currentBankState);
//			System.out.println("is current time"+bankStates[b.bank].nextRead);
//			System.out.println("is row correct"+bankStates[b.bank].openRowAddress);
//			Error.showErrorAndExit("Received a read which is not allowed");	
			}
			
			//update state table
			bankStates[b.bank].nextPrecharge = Math.max(bankStates[b.bank].nextPrecharge, currentTime + mainMemoryConfig.ReadToPreDelay);
			
			
			for (BankState bs : bankStates)
			{
				bs.nextRead = Math.max(bs.nextRead, currentTime + Math.max(mainMemoryConfig.tCCD, mainMemoryConfig.tBL/2));
				bs.nextWrite=Math.max(bs.nextWrite,currentTime + mainMemoryConfig.ReadToWriteDelay);
			}
			
			b.setBusPacketType(BusPacketType.DATA);
			event.addEventTime(mainMemoryConfig.tRL);
			event.setRequestType(RequestType.Column_Read_Complete);
			event.setBusPacket(b);
			//don't use sendEvent as it routes through the "port" will add latency
			event.incrementSerializationID();
			event.getEventQ().addEvent(event);
			//don't update processing and requesting elements as we want the event to come back to rank
			break;
			
		case READ_P:
			if (bankStates[b.bank].currentBankState != CurrentBankState.ROW_ACTIVE ||
			        currentTime < bankStates[b.bank].nextRead ||
			        b.row != bankStates[b.bank].openRowAddress)
			{
				break;
//				Error.showErrorAndExit("Received a read which is not allowed");
			}
			
			bankStates[b.bank].currentBankState = CurrentBankState.IDLE;
			bankStates[b.bank].nextActivate = Math.max(bankStates[b.bank].nextActivate, currentTime + mainMemoryConfig.ReadAutopreDelay);
			
			for (BankState bs : bankStates)
			{
				bs.nextRead = Math.max(bs.nextRead, currentTime + Math.max(mainMemoryConfig.tCCD, mainMemoryConfig.tBL/2));
				bs.nextWrite = Math.max(bs.nextWrite, currentTime + mainMemoryConfig.ReadToWriteDelay);
			}
			
			
			b.setBusPacketType(BusPacketType.DATA);
			event.addEventTime(mainMemoryConfig.tRL);
			event.setRequestType(RequestType.Column_Read_Complete);
			
			event.setBusPacket(b);
			//Main.debugPrinter.print("time to get data event: "+event.getEventTime());
			
			event.incrementSerializationID();
			event.getEventQ().addEvent(event);
			
			break;
		
			
		case WRITE:
			
			if (bankStates[b.bank].currentBankState != CurrentBankState.ROW_ACTIVE ||
			currentTime < bankStates[b.bank].nextWrite ||
	        b.row != bankStates[b.bank].openRowAddress)
			{
//				System.out.println("MAYBE SOMETHING WRONG");
//				Error.showErrorAndExit("== Error - Rank " +id + " received a WRITE when not allowed");
				
			}

			//update state table
			bankStates[b.bank].nextPrecharge = Math.max(bankStates[b.bank].nextPrecharge, currentTime + mainMemoryConfig.WriteToPreDelay);
			for (BankState bs:bankStates)
			{
				bs.nextRead = Math.max(bs.nextRead, currentTime + mainMemoryConfig.WriteToReadDelayB);
				bs.nextWrite = Math.max(bs.nextWrite, currentTime + Math.max(mainMemoryConfig.tBL/2, mainMemoryConfig.tCCD));
			}

			//take note of where data is going when it arrives
			incomingWriteBank = b.bank;
			incomingWriteRow = b.row;
			incomingWriteColumn = b.column;
			b=null;

			break;
			
		case WRITE_P:
			
			if (bankStates[b.bank].currentBankState != CurrentBankState.ROW_ACTIVE ||
	        currentTime < bankStates[b.bank].nextWrite ||
	        b.row != bankStates[b.bank].openRowAddress)
			{
				Error.showErrorAndExit("== Error - Rank " + id + " received a WRITE_P when not allowed");
			
			}
			//update state table
			bankStates[b.bank].currentBankState = CurrentBankState.IDLE;
			bankStates[b.bank].nextActivate = Math.max(bankStates[b.bank].nextActivate, currentTime + mainMemoryConfig.WriteAutopreDelay);
			for (BankState bs : bankStates)
			{
				bs.nextWrite = Math.max(bs.nextWrite, currentTime + Math.max(mainMemoryConfig.tCCD, mainMemoryConfig.tBL/2));
				bs.nextRead = Math.max(bs.nextRead, currentTime + mainMemoryConfig.WriteToReadDelayB);
			}
		
			//take note of where data is going when it arrives
			incomingWriteBank = b.bank;
			incomingWriteRow = b.row;
			incomingWriteColumn = b.column;
			b=null;
			
			break;
		
		case ACTIVATE:
			//make sure activate is allowed		
			
			if (bankStates[b.bank].currentBankState != CurrentBankState.IDLE ||  currentTime < bankStates[b.bank].nextActivate)
			{
//				Error.showErrorAndExit("== Error - Rank " + id + " received an ACT when not allowed");
				break;
//				System.out.println("ACT not Allowed !!!");
			}
			
			bankStates[b.bank].currentBankState = CurrentBankState.ROW_ACTIVE;
			bankStates[b.bank].nextActivate = currentTime + mainMemoryConfig.tRC;
			bankStates[b.bank].openRowAddress = b.row;

			//if AL is greater than one, then posted-cas is enabled - handle accordingly
			if (mainMemoryConfig.tAL>0)
			{
				bankStates[b.bank].nextWrite = currentTime + (mainMemoryConfig.tRCD-mainMemoryConfig.tAL);
				bankStates[b.bank].nextRead = currentTime + (mainMemoryConfig.tRCD-mainMemoryConfig.tAL);}
			else
			{
				bankStates[b.bank].nextWrite = currentTime + (mainMemoryConfig.tRCD-mainMemoryConfig.tAL);
				bankStates[b.bank].nextRead = currentTime + (mainMemoryConfig.tRCD-mainMemoryConfig.tAL);
			}

			bankStates[b.bank].nextPrecharge = currentTime + mainMemoryConfig.tRAS;
			for (int i=0;i<mainMemoryConfig.numBanks;i++)
			{
				if (i != b.bank)
				{
					bankStates[i].nextActivate = Math.max(bankStates[i].nextActivate, currentTime + mainMemoryConfig.tRRD);
				}
			}
			b=null;
			
			break;
		case PRECHARGE:
			//make sure precharge is allowed
			if (bankStates[b.bank].currentBankState != CurrentBankState.ROW_ACTIVE ||
			        currentTime < bankStates[b.bank].nextPrecharge)
			{
				//System.out.println("time of next precharge: " + bankStates[b.bank].nextPrecharge);
//				Error.showErrorAndExit("== Error - Rank " + id + " received a PRE when not allowed");	
				break;
			}
			
			bankStates[b.bank].currentBankState = CurrentBankState.IDLE;
			bankStates[b.bank].nextActivate = Math.max(bankStates[b.bank].nextActivate, currentTime + mainMemoryConfig.tRP);
			b=null;
			 
			break;
		case REFRESH:
			refreshWaiting = false;
			for (int i=0;i<mainMemoryConfig.numBanks;i++)
			{
				if (bankStates[i].currentBankState != CurrentBankState.IDLE){
//					Error.showErrorAndExit("== Error - Rank " + id + " received a REF when not allowed");
				break;
				}
				bankStates[i].nextActivate = currentTime + mainMemoryConfig.tRFC;
			}
			b=null;
			 
			break;
		
		case DATA:
			
			if(event.getRequestType()==RequestType.Column_Read_Complete){
			
			long busFreeTime = parentMemContrlr.getBusFreeTime();
			if(currentTime >= busFreeTime){
				parentMemContrlr.setBusFreeTime(currentTime+mainMemoryConfig.tBL/2);
				event.addEventTime(mainMemoryConfig.tBL/2);
			
				//change the requesting and the processing elements
				event.setRequestType(RequestType.Rank_Response);
				SimulationElement element=event.getProcessingElement();
				
				event.setProcessingElement(event.getRequestingElement());
				event.setRequestingElement(element);
				event.incrementSerializationID();
				event.getEventQ().addEvent(event);
				
				//for TEST
				/*if(mainMemoryConfig.DEBUG_BUS)
				{
					Test.outputLog.print(" -- R" + this.id + " Issuing On Data Bus at Clock Cycle " + GlobalClock.getCurrentTime() + " : ");
					event.getBusPacket().printTest();
					Test.outputLog.print("\n");
				}*/
				
			}
			else{
				//simply moving the event forward in the event queue
				event.addEventTime(busFreeTime-currentTime);
				event.incrementSerializationID();
				event.getEventQ().addEvent(event);
			}
			}
			
			else{
				//it is a write
			}
			break;
		default:
			Error.showErrorAndExit("Invalid bus packet type received by rank");
		}
		
	}
	


}
