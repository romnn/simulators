package config;

import generic.PortType;

public class MainMemoryConfig {
	
	public static enum RowBufferPolicy {
		OpenPage,
		ClosePage
	}
	
	public static enum QueuingStructure
	{
		PerRank,
		PerRankPerBank
	};

	public static enum SchedulingPolicy
	{
		RankThenBankRoundRobin,
		BankThenRankRoundRobin
	};
	
	public RowBufferPolicy rowBufferPolicy; 	
	public SchedulingPolicy schedulingPolicy;	//TODO: hard-coded for now
	public QueuingStructure queuingStructure;
	
	//system config
	//changed by harveenk for testing RAM clk
	//below params acc to DDR3_micron_32M_8B_x4_sg125.ic int numRankPorts;
	public int numRankPorts;
	public PortType rankPortType;			//TODO: check this
	public int rankOccupancy;
	public int rankLatency;
	public int rankOperatingFrequency;								//TODO: currently synchronous with MC

	public double sm_ram_ratio;
	
	//system config
	public int numChans;
	public int numRanks;	//****************************		//TODO Hard coded for now!! Need to calculate this
	public int numBanks;
	public int numRows;
	public int numCols;
	public int TRANSQUEUE_DEPTH;
	//public int CMD_QUEUE_DEPTH = 32;
	public static int TOTAL_ROW_ACCESSES;

	//all timing parameters 							//TODO: are these to be specified here?
	
	public int tCCD;
	public int BL;				//this is the number of bursts, not scaled to cpu clock
	public int tBL;
	public int tCL;
	public int tAL;
	public int tRP;
	public int tCMD;
	public int tRC;
	public int tRCD;
	public int tRAS;
	public int tRFC;
	public int tRTRS;
	public int tRRD;
	public int tFAW;
	
			
	
	public int tRL;	
	public int tWL; 
	
	//for refresh
	public double tCK;
	public int RefreshPeriod;
	
	public int tRTP;
	public int tWTR;
	public int tWR;
	
	public int ReadToPreDelay;
	public int WriteToPreDelay;
	public int ReadToWriteDelay;
	public int ReadAutopreDelay;
	public int WriteAutopreDelay;
	public int WriteToReadDelayB;				//interbank
	public int WriteToReadDelayR;			//interrank
	
	//yet to implement
	//public int tCKE = 4;
	//public int tXP = 4;
	
	
	//bus parameters
	public int DATA_BUS_BITS;				//width of the data bus in bits
	public int TRANSACTION_SIZE;
	public int DATA_BUS_BYTES;
	
	//debug variables
	public boolean DEBUG_BUS = false;
	public boolean DEBUG_BANKSTATE = false;
	public boolean DEBUG_CMDQ = false;
	
	public RowBufferPolicy getRowBufferPolicy()
	{
		return rowBufferPolicy;
	}
	
	public int getRankNumPorts()
	{
		return numRankPorts;
	}
	
	public PortType getRankPortType()
	{
		return rankPortType;
	}
	
	public int getRankOccupancy()
	{
		return rankOccupancy;
	}
	
	public int getRankLatency()
	{
		return rankLatency;
	}
	
	public int getRankOperatingFrequency()
	{
		return rankOperatingFrequency;
	}
	
}
