package dram;

import main.Main;
import generic.Event;
import generic.RequestType;
import generic.SimulationElement;

import misc.Error;

//use to encapsulate payload
//corresponds to BusPacket

public class MainMemoryBusPacket implements Cloneable{

	public static enum BusPacketType {
		READ,
		READ_P,
		WRITE,
		WRITE_P,
		ACTIVATE,
		PRECHARGE,
		REFRESH,
		DATA,
		NULL
	}
	//replaced by request type, in case of events
	//but still need for command queue
	public SimulationElement requestingelement;
	public int row;
	public int column;
	public int bank;
	public int rank;
	public long physicalAddress;
	public BusPacketType busPacketType;
	
	//for TEST
	private static long numpackets = 0;
	public long testid;
	public long timeCreated;
	
	public MainMemoryBusPacket (int row,
								int column,
								int bank,
								int rank,
								long physicalAddress,
								BusPacketType busPacketType, SimulationElement sim){
		this.row = row;
		this.column = column;
		this.bank = bank;
		this.rank = rank;
		this.physicalAddress = physicalAddress;
		this.busPacketType = busPacketType;
		this.requestingelement=sim;
		//for TEST
		numpackets++;
		this.testid = numpackets;
		
	}
	
	public MainMemoryBusPacket()
	{
		this.row = -1;
		this.column = -1;
		this.bank = -1;
		this.rank = -1;
		this.physicalAddress = -1;
		this.busPacketType = BusPacketType.NULL;		//"invalid" type to avoid spurious reads
		
		//for TEST
		numpackets++;
		this.testid = numpackets;
		this.timeCreated = -1;
	}
	
	//these entities are set on address mapping
	
	public MainMemoryBusPacket Clone(){
		try {
			return (MainMemoryBusPacket) (super.clone());
		} catch (CloneNotSupportedException e) {
			misc.Error.showErrorAndExit("Error in cloning event object");
			return null;
		}
	}
	
	public void setBusPacketType(BusPacketType busPacketType)
	{
		this.busPacketType = busPacketType;
	}
	
	public BusPacketType getBusPacketType()
	{
		return this.busPacketType;
	}
	
	public void printPacket()
	{
	
//		System.out.println("Bus packet type: "+ busPacketType +" Row: "+ row +" Col: "						
//				+ column +" Bank: "+
//				bank +" Rank: "+
//				rank +" Phys Address: "+
//				physicalAddress + " Id : " + testid + " timeCreated: " + timeCreated );
//	
	}
	
	public void printPacketToFile()
	{
		
		/*Main.debugPrinter.print("Bus packet type: "+ busPacketType +" "+ row +" "						
				+ column +" "+
				bank +" "+
				rank +" "+
				physicalAddress + "\n");
		*/
		
		/*Test.debugPrinter.print("Bus packet type: "+ busPacketType + " Row: "+ row +" Col: "						
				+ column +" Bank: "+
				bank +" Rank: "+
				rank +" Phys Address: "+
				String.format("%08X",physicalAddress) + " Id : " + testid + " timeCreated: " + timeCreated + "\n");
		*/
	
	}
/*	
	//this function for TEST
	public void printTest()
	{
		if(this == null)
			return;
		else
		{
			switch (busPacketType)
			{
			case READ:
				Main.outputLog.print("BP [READ] pa[0x"+ String.format("%07X", ((physicalAddress>>6)<<6)).toLowerCase() + "] r["+rank+"] b["+bank+"] row["+row+"] col["+column+"]\n");
				break;
			case READ_P:
				Main.outputLog.print("BP [READ_P] pa[0x"+String.format("%07X", ((physicalAddress>>6)<<6)).toLowerCase()+"] r["+rank+"] b["+bank+"] row["+row+"] col["+column+"]\n");
				break;
			case WRITE:
				Main.outputLog.print("BP [WRITE] pa[0x"+String.format("%07X", ((physicalAddress>>6)<<6)).toLowerCase()+"] r["+rank+"] b["+bank+"] row["+row+"] col["+column+"]\n");
				break;
			case WRITE_P:
				Main.outputLog.print("BP [WRITE_P] pa[0x"+String.format("%07X", ((physicalAddress>>6)<<6)).toLowerCase()+"] r["+rank+"] b["+bank+"] row["+row+"] col["+column+"]\n");
				break;
			case ACTIVATE:
				Main.outputLog.print("BP [ACT] pa[0x"+String.format("%07X", ((physicalAddress>>6)<<6)).toLowerCase()+"] r["+rank+"] b["+bank+"] row["+row+"] col["+column+"]\n");
				break;
			case PRECHARGE:
				Main.outputLog.print("BP [PRE] pa[0x"+String.format("%07X", ((physicalAddress>>6)<<6)).toLowerCase()+"] r["+rank+"] b["+bank+"] row["+row+"] col["+column+"]\n");
				break;
			case REFRESH:
				Main.outputLog.print("BP [REF] pa[0x"+String.format("%07X", ((physicalAddress>>6)<<6)).toLowerCase()+"] r["+rank+"] b["+bank+"] row["+row+"] col["+column+"]\n");
				break;
			case DATA:
				Main.outputLog.print("BP [DATA] pa[0x"+String.format("%07X", ((physicalAddress>>6)<<6)).toLowerCase()+"] r["+rank+"] b["+bank+"] row["+row+"] col["+column+"] data["+0+"]=");
				//printData();
				
				Main.outputLog.print("NO DATA\n");
				break;
			default:
				Error.showErrorAndExit("Trying to print unknown kind of bus packet");
			}

		}
	}
	*/
}
