package dram;

import generic.GlobalClock;
import generic.LocalClockperSm;

import java.util.ArrayList;

//import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import config.MainMemoryConfig;
import config.MainMemoryConfig.QueuingStructure;
import config.MainMemoryConfig.RowBufferPolicy;
import config.MainMemoryConfig.SchedulingPolicy;
import dram.BankState.CurrentBankState;
import dram.MainMemoryBusPacket.BusPacketType;
//import generic.GlobalClock;  TODO we need to fix this/.. added by Gantavya 29/05/2018
import main.Main;
import misc.Error;
import net.Bus;

public class CommandQueue {
	
	ArrayList<MainMemoryBusPacket> BusPacketQueue1D;
	ArrayList<ArrayList<MainMemoryBusPacket>> perRankQueue;
	ArrayList<ArrayList<ArrayList<MainMemoryBusPacket>>> queues;
	//QueuingStructure queuingStructure;				//in mainmemconfig
	int numBankQueues;
	int CMD_QUEUE_DEPTH;												
	
	BankState[][] bankStates;
	int rowAccessCounters[][];
	
	MainMemoryConfig mainMemoryConfig;
	int nextRank;
	int nextBank;
	int nextRankPRE;
	int nextBankPRE;
	int refreshRank;
	boolean refreshWaiting;
	boolean sendAct;
	ArrayList<ArrayList<Integer>> tFAWCountdown;

	public CommandQueue(MainMemoryConfig mainMemoryConfig,BankState bankStates[][])
	{
		sendAct=true;
		nextBank=0;
		nextRank=0;
		refreshWaiting=false;
		refreshRank=0;
		nextBankPRE=0;
		nextRankPRE=0;
		
				
		this.mainMemoryConfig=mainMemoryConfig;
		rowAccessCounters=new int[mainMemoryConfig.numRanks][mainMemoryConfig.numBanks];
		tFAWCountdown=new ArrayList<ArrayList<Integer>>(mainMemoryConfig.numRanks);
		
		for (int i=0;i<mainMemoryConfig.numRanks;i++)
		{
			//init the empty vectors here so we don't seg fault later
			tFAWCountdown.add(new ArrayList<Integer>());
		}
		
		
		this.bankStates=bankStates;
		
		
		if (mainMemoryConfig.queuingStructure==QueuingStructure.PerRank)
		{
			numBankQueues = 1;
		}
		else if (mainMemoryConfig.queuingStructure==QueuingStructure.PerRankPerBank)
		{
			numBankQueues = mainMemoryConfig.numBanks;
		}
		else
		{
			Error.showErrorAndExit("== Error - Unknown queuing structure");
			
		}
		
		
		//BusPacketQueue1D = new ArrayList<MainMemoryBusPacket>();
		this.CMD_QUEUE_DEPTH = 102400;									
		
		//perRankQueue = new ArrayList<ArrayList<MainMemoryBusPacket>>();
		queues = new ArrayList<ArrayList<ArrayList<MainMemoryBusPacket>>>();
		for (int rank=0; rank<mainMemoryConfig.numRanks; rank++)
		{
			//this loop will run only once for per-rank and NUM_BANKS times for per-rank-per-bank
			perRankQueue = new ArrayList<ArrayList<MainMemoryBusPacket>>();
			
			for (int bank=0; bank<numBankQueues; bank++)
			{
				BusPacketQueue1D = new ArrayList<MainMemoryBusPacket>();
				
				//actualQueue	= BusPacket1D();
				//System.out.println(BusPacketQueue1D);
				perRankQueue.add(BusPacketQueue1D);
				
			}
			queues.add(perRankQueue);
		}
		
	}

	
	public void enqueue(MainMemoryBusPacket busPacket)
	{
		
		int rank = busPacket.rank;
		int bank = busPacket.bank;
		if (mainMemoryConfig.queuingStructure==QueuingStructure.PerRank)
		{
			queues.get(rank).get(0).add(busPacket);
			//if(mainMemoryConfig.DEBUG_CMDQ)
			//	Test.outputLog.print("Enqueued to command queue for rank "+ rank + "\n");
			if (queues.get(rank).get(0).size()>CMD_QUEUE_DEPTH)	
			{
				Error.showErrorAndExit("== Error - Enqueued more than allowed in command queue");
			}
		}
		else if (mainMemoryConfig.queuingStructure==QueuingStructure.PerRankPerBank)
		{
			queues.get(rank).get(bank).add(busPacket);
			if (queues.get(rank).get(bank).size()>CMD_QUEUE_DEPTH)
			{
				Error.showErrorAndExit("== Error - Enqueued more than allowed in command queue");
			}
		}
		else
		{
			Error.showErrorAndExit("== Error - Unknown queuing structure");
			
		}
	
	}
	
	
	//checks if busPacket is allowed to be issued
	boolean isIssuable(MainMemoryBusPacket busPacket,long currentClockCycle )
	{	
//		long currentClockCycle=GlobalClock.getCurrentTime();
		
		switch (busPacket.busPacketType)
		{
		case REFRESH:

			break;
		case ACTIVATE:
			if ((bankStates[busPacket.rank][busPacket.bank].currentBankState == CurrentBankState.IDLE ||
			        bankStates[busPacket.rank][busPacket.bank].currentBankState == CurrentBankState.REFRESHING) &&
			        currentClockCycle >= bankStates[busPacket.rank][busPacket.bank].nextActivate &&
			        tFAWCountdown.get(busPacket.rank).size() < 4)
			{
				return true;
			}
			else
			{
				//busPacket.printPacketToFile();
				//Main.debugPrinter.print("\n");
				
				/*if(mainMemoryConfig.DEBUG_CMDQ)
				{
					if(currentClockCycle < 0 && busPacket.bank == 3)
					{
						Main.outputLog.print("Cannot issue Activate because \n");
						Main.outputLog.print(String.valueOf(bankStates[busPacket.rank][busPacket.bank].currentBankState == CurrentBankState.IDLE) + " ");
						Main.outputLog.print(String.valueOf(bankStates[busPacket.rank][busPacket.bank].currentBankState == CurrentBankState.REFRESHING) + " ");
						Main.outputLog.print(String.valueOf(currentClockCycle >= bankStates[busPacket.rank][busPacket.bank].nextActivate) + " ");
						Main.outputLog.print(String.valueOf(tFAWCountdown.get(busPacket.rank).size() < 4) + " \n");
						Main.outputLog.print(bankStates[0][3].currentBankState.toString());
										
					}
				}*/
				
				return false;
			}
			//break;
		case WRITE:
		case WRITE_P:
			if (bankStates[busPacket.rank][busPacket.bank].currentBankState == CurrentBankState.ROW_ACTIVE &&
			        currentClockCycle >= bankStates[busPacket.rank][busPacket.bank].nextWrite &&
			        busPacket.row == bankStates[busPacket.rank][busPacket.bank].openRowAddress &&
			        rowAccessCounters[busPacket.rank][busPacket.bank] < MainMemoryConfig.TOTAL_ROW_ACCESSES)
			{
				return true;
			}
			else
			{

				//Main.debugPrinter.print("Cannot issue packet of type: \n");
				//busPacket.printPacketToFile();
				//Main.debugPrinter.print("\n");
				return false;
			}
			//break;
		case READ_P:
		case READ:
			if (bankStates[busPacket.rank][busPacket.bank].currentBankState == CurrentBankState.ROW_ACTIVE &&
			        currentClockCycle >= bankStates[busPacket.rank][busPacket.bank].nextRead &&
			        busPacket.row == bankStates[busPacket.rank][busPacket.bank].openRowAddress &&
			        rowAccessCounters[busPacket.rank][busPacket.bank] < MainMemoryConfig.TOTAL_ROW_ACCESSES)
			{
				return true;
			}
			else
			{

				//Main.debugPrinter.print("Cannot issue packet of type: \n");
				//busPacket.printPacketToFile();
				//Main.debugPrinter.print("\n");
				return false;
			}
			//break;
		case PRECHARGE:
			if (bankStates[busPacket.rank][busPacket.bank].currentBankState == CurrentBankState.ROW_ACTIVE &&
			        currentClockCycle >= bankStates[busPacket.rank][busPacket.bank].nextPrecharge)
			{
				return true;
			}
			else
			{

				//Main.debugPrinter.print("Cannot issue packet of type: \n");
				//busPacket.printPacketToFile();
				//Main.debugPrinter.print("\n");
				return false;
			}
			//break;
		default:
			Error.showErrorAndExit("== Error - Trying to issue a crazy bus packet type : ");
			busPacket.printPacket();
			
		}
		return false;
	}
	
	
	//Removes the next item from the command queue based on the system's
	//command scheduling policy
	public MainMemoryBusPacket pop(long currentClockCycle)
	{
		MainMemoryBusPacket busPacket=null;
		//this can be done here because pop() is called every clock cycle by the parent MemoryController
		//	figures out the sliding window requirement for tFAW
		//
		//deal with tFAW book-keeping
		//	each rank has it's own counter since the restriction is on a device level
		for (int i=0;i<mainMemoryConfig.numRanks;i++)
		{
			//decrement all the counters we have going
			for (int j=0;j<tFAWCountdown.get(i).size();j++)
			{
				tFAWCountdown.get(i).set(j,tFAWCountdown.get(i).get(j)-1);
				
			}
	
			//the head will always be the smallest counter, so check if it has reached 0
			if (tFAWCountdown.get(i).size()>0 && tFAWCountdown.get(i).get(0)==0)
				
			{
				//tFAWCountdown[i].erase(tFAWCountdown[i].begin());
				//Main.debugPrinter.print("\nFinally removed\n");
				//Main.debugPrinter.print(i+" :rank\n");
				//Main.debugPrinter.print("size: "+tFAWCountdown.get(i).size());
				tFAWCountdown.get(i).remove(0);
			}
		}
	
		/* Now we need to find a packet to issue. When the code picks a packet, it will set
			 busPacket = [some eligible packet]
			 
			 First the code looks if any refreshes need to go
			 Then it looks for data packets
			 Otherwise, it starts looking for rows to close (in open page)
		*/
	
		if (mainMemoryConfig.rowBufferPolicy==RowBufferPolicy.ClosePage)
		{
			boolean sendingREF = false;
			//if the memory controller set the flags signaling that we need to issue a refresh
			if (refreshWaiting)
			{
				boolean foundActiveOrTooEarly = false;
				//look for an open bank
				for (int b=0;b<mainMemoryConfig.numBanks;b++)
				{
					ArrayList<MainMemoryBusPacket> queue = getCommandQueue(refreshRank,b);
					//checks to make sure that all banks are idle
					if (bankStates[refreshRank][b].currentBankState == CurrentBankState.ROW_ACTIVE)
					{
						foundActiveOrTooEarly = true;
						//if the bank is open, make sure there is nothing else
						// going there before we close it
						for (int j=0;j<queue.size();j++)
						{
							MainMemoryBusPacket packet = queue.get(j);
							if (packet.row == bankStates[refreshRank][b].openRowAddress &&
									packet.bank == b)
							{
								if (packet.busPacketType != BusPacketType.ACTIVATE && isIssuable(packet,currentClockCycle))
								{
									busPacket = packet;
									queue.remove(j);
									sendingREF = true;
								}
								break;
							}
						}
	
						break;
					}
					//	NOTE: checks nextActivate time for each bank to make sure tRP is being
					//				satisfied.	the next ACT and next REF can be issued at the same
					//				point in the future, so just use nextActivate field instead of
					//				creating a nextRefresh field
					else if (bankStates[refreshRank][b].nextActivate > currentClockCycle)
					{
						foundActiveOrTooEarly = true;
						break;
					}
				}
	
				//if there are no open banks and timing has been met, send out the refresh
				//	reset flags and rank pointer
				if (!foundActiveOrTooEarly && bankStates[refreshRank][0].currentBankState != CurrentBankState.POWER_DOWN)
				{
					busPacket = new MainMemoryBusPacket(1, 1, 0, refreshRank,0 ,BusPacketType.REFRESH,null);
					refreshRank = -1;
					refreshWaiting = false;
					sendingREF = true;
				}
			} // refreshWaiting
			
			
			//if we're not sending a REF, proceed as normal
			if (!sendingREF)
			{
				boolean foundIssuable = false;
				int startingRank = nextRank;
				int startingBank = nextBank;
				do
				{
					ArrayList<MainMemoryBusPacket> queue = getCommandQueue(nextRank, nextBank);
					//make sure there is something in this queue first
					//	also make sure a rank isn't waiting for a refresh
					//	if a rank is waiting for a refresh, don't issue anything to it until the
					//		refresh logic above has sent one out (ie, letting banks close)
					if (!(queue.size()==0) && !((nextRank == refreshRank) && refreshWaiting))
					{
						if (mainMemoryConfig.queuingStructure == QueuingStructure.PerRank)
						{
	
							//search from beginning to find first issuable bus packet
							for (int i=0;i<queue.size();i++)
							{
								if (isIssuable(queue.get(i),currentClockCycle))
								{
									//check to make sure we aren't removing a read/write that is paired with an activate
									if (i>0 && queue.get(i-1).busPacketType==BusPacketType.ACTIVATE &&
											queue.get(i-1).physicalAddress == queue.get(i).physicalAddress)
										continue;
	
									busPacket = queue.get(i);
									queue.remove(i);
									foundIssuable = true;
									break;
								}
							}
						}
						else
						{
							if (isIssuable(queue.get(0),currentClockCycle))
							{
	
								//no need to search because if the front can't be sent,
								// then no chance something behind it can go instead
								busPacket = queue.get(0);
								queue.remove(0);
								foundIssuable = true;
							}
						}
	
					}
					

					//if we found something, break out of do-while
					if (foundIssuable) break;
	
					//rank round robin
					if (mainMemoryConfig.queuingStructure == QueuingStructure.PerRank)
					{
						nextRank = (nextRank + 1) % mainMemoryConfig.numRanks;
						if (startingRank == nextRank)
						{
							break;
						}
					}
					else 
					{
						int temp[]=nextRankAndBank(nextRank, nextBank);
						nextRank = temp[0];
						nextBank = temp[1];
						
						if (startingRank == nextRank && startingBank == nextBank)
						{
							break;
						}
					}
				}
				while (true);
	
				//if we couldn't find anything to send, return false
				if (!foundIssuable){ 
					//Test.debugPrinter.print("Not foundIssuable !!!\n");
					
					return null;}
			}
		}
		else if (mainMemoryConfig.rowBufferPolicy==RowBufferPolicy.OpenPage)
		{
			boolean sendingREForPRE = false;
			if (refreshWaiting)
			{
				boolean sendREF = true;
				//make sure all banks idle and timing met for a REF
				for (int b=0;b<mainMemoryConfig.numBanks;b++)
				{
					//if a bank is active we can't send a REF yet
					if (bankStates[refreshRank][b].currentBankState == CurrentBankState.ROW_ACTIVE)
					{
						sendREF = false;
						boolean closeRow = true;
						//search for commands going to an open row
						ArrayList<MainMemoryBusPacket> refreshQueue = getCommandQueue(refreshRank,b);
	
						for (int j=0;j<refreshQueue.size();j++)
						{
							MainMemoryBusPacket packet = refreshQueue.get(j);
							//if a command in the queue is going to the same row . . .
							if (bankStates[refreshRank][b].openRowAddress == packet.row &&
									b == packet.bank)
							{
								// . . . and is not an activate . . .
								if (packet.busPacketType != BusPacketType.ACTIVATE)
								{
									closeRow = false;
									// . . . and can be issued . . .
									if (isIssuable(packet,currentClockCycle))
									{
										//send it out
										busPacket = packet;
										refreshQueue.remove(j);
										sendingREForPRE = true;
									}
									break;
								}
								else //command is an activate
								{
									//if we've encountered another act, no other command will be of interest
									break;
								}
							}
						}
	
						//if the bank is open and we are allowed to close it, then send a PRE
						if (closeRow && currentClockCycle >= bankStates[refreshRank][b].nextPrecharge)
						{
							rowAccessCounters[refreshRank][b]=0;
							busPacket = new MainMemoryBusPacket( 0, 0, b, refreshRank, 0, BusPacketType.PRECHARGE,null);
							sendingREForPRE = true;
						}
						break;
					}
					//	NOTE: the next ACT and next REF can be issued at the same
					//				point in the future, so just use nextActivate field instead of
					//				creating a nextRefresh field
					else if (bankStates[refreshRank][b].nextActivate > currentClockCycle) //and this bank doesn't have an open row
					{
						sendREF = false;
						break;
					}
				}
	
				//if there are no open banks and timing has been met, send out the refresh
				//	reset flags and rank pointer
				if (sendREF && bankStates[refreshRank][0].currentBankState != CurrentBankState.POWER_DOWN)
				{
					busPacket = new MainMemoryBusPacket(0, 0, 0, refreshRank, 0, BusPacketType.REFRESH,null);
					refreshRank = -1;
					refreshWaiting = false;
					sendingREForPRE = true;
				}
			}
	
			if (!sendingREForPRE)
			{
				int startingRank = nextRank;
				int startingBank = nextBank;
				boolean foundIssuable = false;
				do // round robin over queues
				{
					ArrayList<MainMemoryBusPacket> queue = getCommandQueue(nextRank,nextBank);
					//make sure there is something there first
					if (!(queue.size()==0) && !((nextRank == refreshRank) && refreshWaiting))
					{
						//search from the beginning to find first issuable bus packet
						for (int i=0;i<queue.size();i++)
						{
							MainMemoryBusPacket packet = queue.get(i);
							if (isIssuable(packet,currentClockCycle))
							{
								//check for dependencies
								boolean dependencyFound = false;
								for (int j=0;j<i;j++)
								{
									MainMemoryBusPacket prevPacket = queue.get(j);
									if (prevPacket.busPacketType != BusPacketType.ACTIVATE &&
											prevPacket.bank == packet.bank &&
											prevPacket.row == packet.row)
									{
										dependencyFound = true;
										break;
									}
								}
								if (dependencyFound) continue;
	
								busPacket = packet;
	
								//if the bus packet before is an activate, that is the act that was
								//	paired with the column access we are removing, so we have to remove
								//	that activate as well (check i>0 because if i==0 then theres nothing before it)
								if (i>0 && queue.get(i-1).busPacketType == BusPacketType.ACTIVATE)
								{
									rowAccessCounters[(busPacket).rank][(busPacket).bank]++;
									
									//Test.outputLog.print("incrementing row access counter for bank " + busPacket.bank + " value is "
									//								+ rowAccessCounters[(busPacket).rank][(busPacket).bank] +"\n");
									
									// i is being returned, but i-1 is being thrown away, so must delete it here 
									
									//changed by kushagra
									queue.set(i-1,null);
	
									// remove both i-1 (the activate) and i (we've saved the pointer in *busPacket)
									
									//queue.remove(i-1);			//added by kushagra
									queue.remove(i);
									queue.remove(i-1);
								}
								else // there's no activate before this packet
								{
									//or just remove the one bus packet
									queue.remove(i);
								}
	
								foundIssuable = true;
								break;
							}
						}
					}
	
					//if we found something, break out of do-while
					if (foundIssuable) break;
	
					//rank round robin
					if (mainMemoryConfig.queuingStructure == QueuingStructure.PerRank)
					{
						nextRank = (nextRank + 1) % mainMemoryConfig.numRanks;
						if (startingRank == nextRank)
						{
							break;
						}
					}
					else 
					{
						int temp[] = nextRankAndBank(nextRank, nextBank); 
						nextRank = temp[0];
						nextBank = temp[1];
						temp = null;
						if (startingRank == nextRank && startingBank == nextBank)
						{
							break;
						}
					}
				}
				while (true);
	
				//if nothing was issuable, see if we can issue a PRE to an open bank
				//	that has no other commands waiting
				if (!foundIssuable)
				{
					
					//Test.outputLog.print("Searching for banks to close\n");
					
					//search for banks to close
					boolean sendingPRE = false;
					startingRank = nextRankPRE;
					startingBank = nextBankPRE;
	
					do // round robin over all ranks and banks
					{
						ArrayList<MainMemoryBusPacket> queue = getCommandQueue(nextRankPRE, nextBankPRE);
						boolean found = false;
						//check if bank is open
						if (bankStates[nextRankPRE][nextBankPRE].currentBankState == CurrentBankState.ROW_ACTIVE)
						{
							for (int i=0;i<queue.size();i++)
							{
								//if there is something going to that bank and row, then we don't want to send a PRE
								if (queue.get(i).bank == nextBankPRE &&
										queue.get(i).row == bankStates[nextRankPRE][nextBankPRE].openRowAddress)
								{
									found = true;
									break;
								}
							}
	
							//if nothing found going to that bank and row or too many accesses have happened, close it
							if (!found || rowAccessCounters[nextRankPRE][nextBankPRE]==MainMemoryConfig.TOTAL_ROW_ACCESSES)
							{
								//Test.outputLog.print("Trying to issue precharge\n");
								
								if (currentClockCycle >= bankStates[nextRankPRE][nextBankPRE].nextPrecharge)
								{
									//System.out.println("issuing precharge to open bank " + nextBankPRE + " next precharge " + 
									//				bankStates[nextRankPRE][nextBankPRE].nextPrecharge + " at time " + currentClockCycle);
									//Test.outputLog.print("issuing precharge to bank " + nextBankPRE);
									sendingPRE = true;
									rowAccessCounters[nextRankPRE][nextBankPRE] = 0;
									busPacket = new MainMemoryBusPacket(0, 0, nextBankPRE, nextRankPRE, 0,BusPacketType.PRECHARGE, null );
									break;
								}
							}
						}
						int temp[] = nextRankAndBank(nextRankPRE, nextBankPRE);
						nextRankPRE = temp[0];
						nextBankPRE = temp[1];
						temp = null;
					}
					while (!(startingRank == nextRankPRE && startingBank == nextBankPRE));
	
					//if no PREs could be sent, just return false
					if (!sendingPRE) return null;
				}
			}
		}
	
		//sendAct is flag used for posted-cas
		//  posted-cas is enabled when AL>0
		//  when sendAct is true, when don't want to increment our indexes
		//  so we send the column access that is paid with this act
		if (mainMemoryConfig.tAL>0 && sendAct)
		{
			sendAct = false;
		}
		else
		{
			sendAct = true;
			int a[]=nextRankAndBank(nextRank, nextBank);
			nextRank=a[0];
			nextBank=a[1];
		}
	
		//if its an activate, add a tfaw counter
		if (busPacket.busPacketType==BusPacketType.ACTIVATE)
		{
			tFAWCountdown.get((busPacket).rank).add(mainMemoryConfig.tFAW);
			//Main.debugPrinter.print("\nInsert into FAW Rank: "+(busPacket).rank+"\n");
			//Main.debugPrinter.print("size of rank FAW: "+tFAWCountdown.get(busPacket.rank).size()+"\n");
		}
	
		return busPacket;
	
	}
	
	
	int[] nextRankAndBank(int rank, int bank)
	{
		if (mainMemoryConfig.schedulingPolicy == SchedulingPolicy.RankThenBankRoundRobin)
		{
			rank++;
			{
				rank = 0;
				bank++;
				if (bank == mainMemoryConfig.numBanks)
				{
					bank = 0;
				}
			}
			int a[]={rank,bank};
			return a;
		}
		//bank-then-rank round robin
		else if (mainMemoryConfig.schedulingPolicy == SchedulingPolicy.BankThenRankRoundRobin)
		{
			bank++;
			if (bank == mainMemoryConfig.numBanks)
			{
				bank = 0;
				rank++;
				if (rank == mainMemoryConfig.numRanks)
				{
					rank = 0;
				}
			}
			int a[]={rank,bank};
			return a;
		}
		else
		{
			Error.showErrorAndExit("== Error - Unknown scheduling policy");
			return null;
		}

	}	
	
	
	ArrayList<MainMemoryBusPacket> getCommandQueue(int rank, int bank)
	{
		if (mainMemoryConfig.queuingStructure == QueuingStructure.PerRankPerBank)
		{
			return queues.get(rank).get(bank);
		}
		else if (mainMemoryConfig.queuingStructure == QueuingStructure.PerRank)
		{
			return queues.get(rank).get(0);
		}
		else
		{
			Error.showErrorAndExit("Unknown queue structure");
			return null;
		}

	}
	
	void needRefresh(int rank)
	{
		refreshWaiting = true;
		refreshRank = rank;
	}
	
	public boolean canPop()
	{
		return(BusPacketQueue1D.size()>0);
	}
	
	
	public boolean hasRoomFor(int num, int rank, int bank)
	{
//		System.out.println(CMD_QUEUE_DEPTH - getCommandQueue(rank, bank).size() +"is the size left for rank"+rank);
		return (CMD_QUEUE_DEPTH - getCommandQueue(rank, bank).size() >= num);
		
	
	}
/*
 * The following function is for the test prupose, the outputlog was a function in the TEJAS but is not a function in the GPU TEJAS.
 * SO currently i am commenting the whole
 */
	//this function for TEST
	public void printTest()
	{
		if (mainMemoryConfig.queuingStructure== QueuingStructure.PerRank)
		{
			System.out.println("\n== Printing Per Rank Queue at Clock Cycle "+  GlobalClock.getCurrentTime() +"\n" );
			for (int i=0;i< mainMemoryConfig.numRanks;i++)
			{
				System.out.println(" = Rank " + i + "  size : " + queues.get(i).get(0).size() + "\n");
				for (int j=0;j < queues.get(i).get(0).size();j++)
				{
					System.out.println("    "+ j + "]");
					//queues.get(i).get(0).get(j).printTest();
				}
			}
		}

	}
	
}
