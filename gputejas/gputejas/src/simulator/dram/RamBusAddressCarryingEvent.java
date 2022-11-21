package dram;

import dram.MainMemoryBusPacket.BusPacketType;
import generic.EventQueue;
import generic.RequestType;
import generic.SimulationElement;
import main.Main;
import memorysystem.AddressCarryingEvent;


public class RamBusAddressCarryingEvent extends AddressCarryingEvent {

	//public BusPacket busPacket;					//encapsulate in request type
	private MainMemoryBusPacket busPacket;
	
	public RamBusAddressCarryingEvent(EventQueue eventQ, long eventTime,
			SimulationElement requestingElement,
			SimulationElement processingElement,
			RequestType requestType, long address, MainMemoryBusPacket busPacket){
		super(eventQ, eventTime, requestingElement, processingElement,
		requestType, address);
		this.setBusPacket(busPacket);
	}
	
	public RamBusAddressCarryingEvent(MainMemoryBusPacket busPacket)
	{
		super();
		this.requestType = RequestType.Main_Mem_Access;
		this.setBusPacket(busPacket);
	}

	public MainMemoryBusPacket getBusPacket() {
		return busPacket;
	}

	public void setBusPacket(MainMemoryBusPacket busPacket) {
		this.busPacket = busPacket;
	}
	
	@Override
	public void dump()
	{
		//System.out.println("CoreId: " + coreId + " RequestType : " + requestType + " RequestingElement : " + requestingElement + " ProcessingElement : " + processingElement + " EventTime : " + eventTime + " Address : " + address + " BusPacketType : " + busPacket.busPacketType + "\n" );
		
		//write to debug file
		
		//Main.debugPrinter.print("CoreId: " + coreId + " RequestType : " + requestType + " RequestingElement : " + requestingElement + " ProcessingElement : " + processingElement + " EventTime : " + eventTime + " Address : " + address + " BusPacketType : " + busPacket.busPacketType + "\n" );
		//Test.debugPrinter.print("CoreId: " + coreId + " RequestType : " + requestType + " RequestingElement : " + requestingElement + " ProcessingElement : " + processingElement + " EventTime : " + eventTime + " Address : " + address + " BusPacketType : " + busPacket.busPacketType + "\n" );
	}
}
