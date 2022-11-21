package dram;

import main.Main;
import generic.EventQueue;
import generic.RequestType;
import generic.SimulationElement;
import generic.Event;

public class StateUpdateEvent extends Event {

	private int rank;
	private int bank;
	
	public StateUpdateEvent(EventQueue eventQ, long eventTime,
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, int rank, int bank) {
		super(eventQ, eventTime, requestingElement, processingElement,requestType, -1);
		this.setRank(rank);
		this.setBank(bank);
	}

	public StateUpdateEvent updateEvent(EventQueue eventQ, long eventTime, 
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, int rank, int bank) {
		this.setRank(rank);
		this.setBank(bank);
		return (StateUpdateEvent)this.update(eventQ, eventTime, requestingElement, processingElement, requestType);
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getBank() {
		return bank;
	}

	public void setBank(int bank) {
		this.bank = bank;
	}
	
	
	public void dump()
	{
		//write to debug file
		
		//Main.debugPrinter.print("CoreId: " + coreId + " RequestType : " + requestType + " RequestingElement : " + requestingElement + " ProcessingElement : " + processingElement + " EventTime : " + eventTime + " Rank : " + rank + " Bank : " + bank + "\n" );
		//Test.debugPrinter.print("CoreId: " + coreId + " RequestType : " + requestType + " RequestingElement : " + requestingElement + " ProcessingElement : " + processingElement + " EventTime : " + eventTime + " Rank : " + rank + " Bank : " + bank + "\n" );
	}
	
}
