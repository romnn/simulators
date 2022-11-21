/*
/*****************************************************************************
				GPUTejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2014] [Indian Institute of Technology, Delhi]
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

	Contributors:  Seep Goel, Geetika Malhotra, Harinder Pal
*****************************************************************************/ 
package memorysystem;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import net.BusInterface;
import net.ID;
import net.NocInterface;

import pipeline.GPUMemorySystem;
import main.ArchitecturalComponent;
import memorysystem.Cache.CacheType;
//import memorysystem.coherence.Directory;
//import memorysystem.coherence.Directory;
import memorysystem.directory.CentralizedDirectoryCache;
import memorysystem.NucaCache;
import memorysystem.NucaCache.NucaType;
//import memorysystem.nuca.NucaCache;
//import memorysystem.nuca.NucaCache.NucaType;
import generic.*;
import config.CacheConfig;
import config.SystemConfig;
import config.TpcConfig;
import dram.*;

public class MemorySystem
{
	static SM[][] cores;
	static Hashtable<String, Cache> cacheList;
	public static Hashtable<String, Cache> cacheNameMappings = new Hashtable<String, Cache>();
	public static MainMemoryDRAMController mainMemoryController;
        
	public static CentralizedDirectoryCache centralizedDirectory;
	
	public static Hashtable<String, Cache> getCacheList() {
		return cacheList;
	}

	/* private static void createLinkToNextLevelCache(Cache c) {
		String cacheName = c.cacheName;
		int cacheId = c.id;
		String nextLevelName = c.cacheConfig.nextLevel;
		
		if(nextLevelName=="" || nextLevelName==null) {
			return;
		}
		
		String nextLevelIdStrOrig = c.cacheConfig.nextLevelId;
		
		if(nextLevelIdStrOrig!=null && nextLevelIdStrOrig!="") {
			int nextLevelId = getNextLevelId(cacheName, cacheId, nextLevelIdStrOrig);
			nextLevelName += "[" + nextLevelId + "]";
		} else {
			nextLevelName += "[0]";
		}
		
		Cache nextLevelCache = cacheNameMappings.get(nextLevelName);
		if(nextLevelCache==null) {
			misc.Error.showErrorAndExit("Inside " + cacheName + ".\n" +
				"Could not find the next level cache. Name : " + nextLevelName);
		}
		
		c.createLinkToNextLevelCache(nextLevelCache);		
	}  */
	
	
	public static Cache createSharedCache(String token, CommunicationInterface comInterface) {
		for(CacheConfig config : SystemConfig.sharedCacheConfigs) {
			
			if(token.equals(config.cacheName)) {
				Cache c = null;
				if(config.nucaType != NucaType.NONE)
				{
					NucaCache nuca;
					if(!ArchitecturalComponent.nucaList.containsKey(token))
					{
						nuca = new NucaCache(token, 0, config, null);
						ArchitecturalComponent.nucaList.put(token, nuca);
					}
					else{
						nuca = ArchitecturalComponent.nucaList.get(token);
					}
					c = nuca.createBanks(token, config, comInterface);
				}
				else{
					c = new Cache(token+"[0]", 0, config, null);
					
				}
				c.setComInterface(comInterface);
				return c;
			}
		}
		
		misc.Error.showErrorAndExit("Unable to find a cache config for " + token);
		return null;
	}
	
	public static void createLinkBetweenCaches() {
		for(Map.Entry<String, Cache> cacheListEntry : cacheNameMappings.entrySet()) {
			
			Cache c = cacheListEntry.getValue();
			//System.out.println("the next levvel is " + c.nextLevel.cacheName);
			
			// If the next level field has been set in the coreMemSys, do not set it again
			if(c.nextLevel==null) {
				createLinkToNextLevelCache(c);
			}
		}
	}
	
	private static void createLinkToNextLevelCache(Cache c) {
		String cacheName = c.cacheName;
		int cacheId = c.id;
		String nextLevelName = c.cacheConfig.nextLevel;
		
		
		if(nextLevelName=="" || nextLevelName==null) {
			return;
		}
		
		String nextLevelIdStrOrig = c.cacheConfig.nextLevelId;
		
		
			nextLevelName += "[0]";
		Cache nextLevelCache = cacheNameMappings.get(nextLevelName);
		if(nextLevelCache==null) {
			misc.Error.showErrorAndExit("Inside " + cacheName + ".\n" +
				"Could not find the next level cache. Name : " + nextLevelName);
		}
		
		c.createLinkToNextLevelCache(nextLevelCache);		
	}
	
	
	
	public static void addToCacheList(String cacheName, Cache cache) {
		if(cacheNameMappings.contains(cacheName)) {
			misc.Error.showErrorAndExit("A cache with same name already exists !!\nCachename : " + cacheName);
		} else {
			cacheNameMappings.put(cacheName, cache);
		}
	}
	
	@SuppressWarnings("unused")
	public static SMMemorySystem[][] initializeMemSys(SM[][] sms)
	{
		MemorySystem.cores = sms;
		SMMemorySystem smMemSysArray[][] = new SMMemorySystem[sms.length][sms[0].length];
		
		System.out.println("initializing memory system...");
		CacheConfig cacheParameterObj;
		/*First initialize the L2 and greater caches (to be linked with L1 caches and among themselves)*/
		cacheList = new Hashtable<String, Cache>(); //Declare the hash table for level 2 or greater caches
		boolean flag = false;
		mainMemoryController = new MainMemoryDRAMController(SystemConfig.mainMemoryConfig);
		for (int i = 0; i < SystemConfig.NoOfTPC ; i++)
		{
			for(int j =0 ; j<  TpcConfig.NoOfSM; j++)
			{
				int k=0;
				{
					SMMemorySystem smMemSys = null;
					smMemSys = new GPUMemorySystem(sms[i][j], k);
			
					smMemSysArray[i][j] = smMemSys;
				}
			}
		}
				
					//Set the next levels of the instruction cache
			/*		if (smMemSys.iCache.isLastLevel == true) //If this is the last level, don't set anything
					{
						continue;
					}
				
					String nextLevelName = "L2";
				
					if (nextLevelName.isEmpty())
					{
						System.err.println("Memory system configuration error : The iCache is not last level but the next level is not specified");
						System.exit(1);
					}
					
					if (cacheList.containsKey(nextLevelName)) 
					{
						//Point the cache to its next level
						smMemSys.iCache.nextLevel = cacheList.get(nextLevelName);
						smMemSys.iCache.nextLevel.prevLevel.add(smMemSys.iCache);
					}
					else
					{
						System.err.println("Memory system configuration error : A cache specified as a next level does not exist");
						System.exit(1);
					}
				}
				
			}			
		} */
			
	//		/*for (Enumeration<String> cacheNameSet = cacheList.keys(); cacheNameSet.hasMoreElements(); /*Nothing*/)
		/*	{
				String cacheName = cacheNameSet.nextElement();
				Cache cacheToSetNextLevel = cacheList.get(cacheName);
					
				if (cacheToSetNextLevel.isLastLevel == true) //If this is the last level, don't set anything
				{
					continue;
				}
				String nextLevelName = cacheToSetNextLevel.nextLevelName;
				
				if (nextLevelName.isEmpty())
				{
					System.err.println("Memory system configuration error : The cache \""+ cacheName +"\" is not last level but the next level is not specified");
					System.exit(1);
				}
				if (cacheName.equals(nextLevelName)) //If the cache is itself given as its next level
				{
					System.err.println("Memory system configuration error : The cache \""+ cacheName +"\" is specified as a next level of itself");
					System.exit(1);
				}
					
				if (cacheList.containsKey(nextLevelName)) 
				{
					//Point the cache to its next level
					cacheToSetNextLevel.nextLevel = cacheList.get(nextLevelName);
					cacheToSetNextLevel.nextLevel.prevLevel.add(cacheToSetNextLevel);
				}
				else
				{
					System.err.println("Memory system configuration error : A cache specified as a next level does not exist");
					System.exit(1);
				}
			}
			
		//	for (Enumeration<String> cacheNameSet = cacheList.keys(); cacheNameSet.hasMoreElements(); /*Nothing*/ //)
			/*{
				String cacheName = cacheNameSet.nextElement();
				Cache cacheToSetConnectedMSHR = cacheList.get(cacheName);
				cacheToSetConnectedMSHR.populateConnectedMSHR();
			} */ 
			
			return smMemSysArray;
	
			
	}
		
	
	
		public static   CentralizedDirectoryCache getDirectoryCache()
		{
			return centralizedDirectory;
		}
		
		public static void printMemSysResults()
		{
			System.out.println("\n Memory System results\n");
			
			System.out.println(" ");
			System.out.println(" Results of other caches");
			
			for (Enumeration<String> cacheNameSet = cacheList.keys(); cacheNameSet.hasMoreElements(); /*Nothing*/)
			{
				String cacheName = cacheNameSet.nextElement();
				Cache cache = cacheList.get(cacheName);
				
				System.out.println(
						cacheName + " Hits : " 
						+ cache.hits 
						+ "\t ; " + cacheName + " misses : " 
						+ cache.misses);
			}
		}
		public MainMemoryDRAMController getMemoryControllerId(CommunicationInterface comInterface) {
			
			MainMemoryDRAMController memControllerRet = null;
			
			if(comInterface.getClass()==NocInterface.class) {
				ID currBankId = ((NocInterface)comInterface).getId();
		    	double distance = Double.MAX_VALUE;
		    	ID memControllerId = ((NocInterface) (ArchitecturalComponent.memoryControllers.get(0).getComInterface())).getId();
		    	int x1 = currBankId.getx();//bankid/cacheColumns;
		    	int y1 = currBankId.gety();//bankid%cacheColumns;
		   
		    	for(MainMemoryDRAMController memController:ArchitecturalComponent.memoryControllers) {
		    		int x2 = ((NocInterface)memController.getComInterface()).getId().getx();
		    		int y2 = ((NocInterface)memController.getComInterface()).getId().gety();
		    		double localdistance = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
		    		if(localdistance < distance) {
		    			distance = localdistance;
		    			memControllerRet = memController;
		    			memControllerId = ((NocInterface)memController.getComInterface()).getId();
		    		}
		    	}
			} else if(comInterface.getClass()==BusInterface.class) {
				memControllerRet = ArchitecturalComponent.memoryControllers.get(0);
			}
	    	
	    	return memControllerRet;
	    }

		
	}
