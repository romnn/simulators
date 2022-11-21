/*****************************************************************************
				Tejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2010] [Indian Institute of Technology, Delhi]
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

				Contributor: Eldhose Peter
*****************************************************************************/


package memorysystem;

import generic.CommunicationInterface;
import generic.Event;
import generic.EventQueue;
import generic.GlobalClock;
import generic.RequestType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import main.ArchitecturalComponent;
import memorysystem.AddressCarryingEvent;
import memorysystem.Cache;
import memorysystem.CacheLine;
import memorysystem.SMMemorySystem;
import memorysystem.MESI;
//import memorysystem.MSHR;
//import memorysystem.nuca.RNucaBank.AddressType;
import net.ID;
import net.NocInterface;
import config.CacheConfig;
import config.SystemConfig;

public class NucaCache extends Cache
{
	public static enum ONUCAType{
		BCAST,
		TSI,
		OP_BCAST,
		OP_BCASTR,
		BUTTERFLY_S,
		BUTTERFLY_D,
		NONE
	}
	public static enum NucaType{
		S_NUCA,
		D_NUCA,
		O_NUCA,
		R_NUCA,
		NONE
	}
	
	public static enum Mapping {
		SET_ASSOCIATIVE,
		ADDRESS,
		BOTH
	}
    
    public Vector<Cache> cacheBank;
    public Vector<Vector<Integer>> bankSets; //set of bank sets, each value denote the position of cache bank in "cacheBank"
    public Cache[][] bankSetsButterfly;
    public NucaType nucaType;
    public Mapping mapping;
    public HashMap<Event, Integer> activeEventsInNuca;
    public boolean ONucaStatus;
    public long hopCount;
    public ONUCAType onucaType;
    
	public long homeBankHits;
	public long homeBankAccesses;
	public long nonHomeBankAccesses;
	public long nonHomeBankHits;
	public long migrations;
	public long evictions;
	public long effectiveKill;
	
	
	public static HashMap<Long, List<ID>> sharingAddress;
   // public static HashMap<Long, AddressType> addressToType;
    public static HashMap<ID, Integer> coreIdtoRId;
    
    public NucaCache(String cacheName, int id, CacheConfig cacheParameters,
    		SMMemorySystem containingMemSys)
	{
		super(cacheName, id, cacheParameters, containingMemSys);
        this.cacheBank =new Vector<Cache>(); //cache banks are added later
        System.out.println("Cache paramters" + cacheParameters.nucaType);
        this.mapping = cacheParameters.mapping;
        this.nucaType = cacheParameters.nucaType;
        activeEventsInNuca = new HashMap<Event, Integer>();
    }
  
    
    public Cache createBanks(String token, CacheConfig config, CommunicationInterface cominterface) {
		int size = cacheBank.size();
		Cache c =null;
		c = new SNucaBank(token+"["+size+"]", 0, config, null, this);
		
		cacheBank.add(c);
		return c;
	}
    public Cache getBank(ID id, long addr) {
		if(this.nucaType == NucaType.S_NUCA)
			return getSNucaBank(addr);
		else
		{
			misc.Error.showErrorAndExit("Invalid Nuca Type");
			return null;
		}
					
	}
    
    public long getBlockAddress(long addr)
    {
    	return (addr>>>blockSizeBits);
    }
	
	public Cache getSNucaBank(long addr)
	{
		if(mapping == Mapping.SET_ASSOCIATIVE) 
		{
//			long tag = (addr>>>(numSetsBits + blockSizeBits));
			long tag = (addr>>>(blockSizeBits)); //END
			return integerToBank((int)(tag & (getNumOfBanks()-1)));
		}
		else if(mapping == Mapping.ADDRESS)
		{
			long tag = (addr>>>(numLinesBits+blockSizeBits));
			return integerToBank((int)(tag & (getNumOfBanks()-1)));
		}
		else
		{
			misc.Error.showErrorAndExit("Invalid Type of Mapping!!!");
			return null;
		}
	}
	

	//For D_NUCA
	Cache getDNucaBank(int bankS,ID coreId)
	{
		Vector<Integer> bankSet = bankSets.get(bankS); 
		int bankNum=-1;
		int min=Integer.MAX_VALUE;
		for(int bank : bankSet)
		{
			ID bankId =((NocInterface)(cacheBank.get(bank)).getComInterface()).getId();
			int dist = (coreId.getx() - bankId.getx())*(coreId.getx() - bankId.getx()) + 
					   (coreId.gety() - bankId.gety())*(coreId.gety() - bankId.gety()) ;
			if(dist<min)
			{
				min=dist;
				bankNum = bank;
			}
		}
		return (Cache) this.cacheBank.get(bankNum);
	}
	public int calculateRId(long address,ID id)
	{
		int rid=0;
		long tag = computeTag(address);
		int a1a0=(int)tag & 3;
		int a1=0,a0=0;
		switch(a1a0)
		{
		case 0:
			a1=0;
			a0=0;
			break;
		case 1:
			a1=0;
			a0=1;
			break;
		case 2:
			a1=1;
			a0=0;
			break;
		case 3:
			a1=1;
			a0=1;
			break;
		}
		int c1c0 = coreIdtoRId.get(id);
		int c1=0,c0=0;
		switch(c1c0)
		{
		case 0:
			c1=0;
			c0=0;
			break;
		case 1:
			c1=0;
			c0=1;
			break;
		case 2:
			c1=1;
			c0=0;
			break;
		case 3:
			c1=1;
			c0=1;
			break;
		}
		int r1 = ((c1 ^ c0 ^ a1)|(~(c0 ^ a0)))&((c1 ^ a1)|(c0 ^ a0));
		int r0 = c0 ^ a0;
		//System.err.println(r1 + "" + r0);
		if(r1==0 && r0==0)
			rid=0;
		else if(r1==0 && r0==1)
			rid=1;
		else if(r1==1 && r0==0)
			rid=2;
		else if(r1==1 && r0==1)
			rid=3;
		return rid;
	}
	public int calculateCoreId(int row,int rId)
	{
		int offset;
		if(row%2!=0)
		{
			offset = (rId + 2)%4;
		}
		else
		{
			offset = rId;
		}
		return 4*(row-1)+offset;
	}	
	public Cache findRotationalBank(long address, ID id)
	{
		int rId = calculateRId(address, id);
		int bankId = calculateCoreId(id.getx(), rId);
//		System.err.println(id.getx() + " " + rId + "  " + bankId + " " + cacheBank.size() + " "+ this.toString());
		return (cacheBank.get(bankId));
	}
	
	public Cache getButterflyBank(Cache cache, long addr, ID id) {
		int phase,x,y,numBanks;
		x=id.getx();
		y=id.gety();
		if(x<17)
		{
			if(y<16)
				phase=0;
			else
				phase=1;
		}
		else
		{
			if(y<16)
				phase=2;
			else
				phase=3;
		}
		numBanks = getNumOfBanks()/4-1; 
		if(mapping == Mapping.SET_ASSOCIATIVE) 
		{
//			long tag = (addr>>>(numSetsBits + blockSizeBits));//MID
			long tag = (addr>>>(blockSizeBits)); //END
			return integerToBank(((int)(tag & numBanks))+phase*64);
		}
		else if(mapping == Mapping.ADDRESS)
		{
			long tag = (addr>>>(blockSizeBits));
			return integerToBank(((int)(tag & numBanks))+phase*64);
		}
		else
		{
			misc.Error.showErrorAndExit("Invalid Type of Mapping!!!");
			return null;
		}
	}
	
	int getBankNum(long addr)
	{
		int bankNum=-1;
		if(mapping == Mapping.SET_ASSOCIATIVE) 
		{
			long tag = (addr>>>(numSetsBits+blockSizeBits));
			bankNum = (int) (tag & (getNumOfBanks()-1)); //FIXME: getNumOfBanks() assumes 2^n.. remove that
		}
		else if(mapping == Mapping.ADDRESS)
		{
			long tag = (addr>>>(numLinesBits+blockSizeBits));
			bankNum = (int) (tag & (getNumOfBanks()-1));
		}
		else
		{
			misc.Error.showErrorAndExit("Invalid Type of Mapping!!!");
			return 0;
		}
		return bankNum;
	}
	int findBankSetNum(int bankNum)
	{
		int bankSetNum = -1;
		for(Vector<Integer> bankSet : bankSets)
		{
			for(int bankNumber : bankSet){
				if(bankNum == bankNumber)
					bankSetNum = bankSets.indexOf(bankSet);
			}
		}
		if(bankSetNum == -1)
			misc.Error.showErrorAndExit("Error in finding the bank set!!!");
		return bankSetNum;
	}

	int getBankSetId(long addr)
	{
		int bankNum = getBankNum(addr);
		int bankSet = findBankSetNum(bankNum);
		return bankSet;
	}

	
    public Cache integerToBank(int bankNumber)
	{
		return (Cache) this.cacheBank.get(bankNumber);
	}
	
	public int getNumOfBanks()
	{
		return cacheBank.size();		
	}
	public MissStatusHoldingRegister getMshr()
	{
		return this.missStatusHoldingRegister;
	}
	public void callCacheHandleEvent(EventQueue q, Event e)
	{
		super.handleEvent(q, e);
	}
	public void intializeStatisticsVariables()
	{
        this.hits = 0;
        
        this.homeBankHits = 0;
        this.homeBankAccesses = 0;
        this.nonHomeBankHits = 0;
        this.nonHomeBankAccesses = 0;
        this.migrations = 0;
        this.evictions = 0;
        this.effectiveKill = 0;
	}
}	