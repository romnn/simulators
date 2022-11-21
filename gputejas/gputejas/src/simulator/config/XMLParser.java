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
package config;


import generic.GpuType;
import generic.MultiPortingType;
import generic.PortType;

import java.io.File;
import java.util.Hashtable;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import memorysystem.directory.CentralizedDirectoryCache;
import memorysystem.NucaCache.Mapping;
import memorysystem.NucaCache;
import memorysystem.NucaCache.NucaType;

import memorysystem.Cache;
import memorysystem.Cache.CacheType;
import memorysystem.Cache.CoherenceType;
import net.RoutingAlgo;
import net.NOC.CONNECTIONTYPE;
import net.NOC.TOPOLOGY;
import main.ArchitecturalComponent;

import org.w3c.dom.*;

import config.MainMemoryConfig.QueuingStructure;
import config.MainMemoryConfig.RowBufferPolicy;
import config.MainMemoryConfig.SchedulingPolicy;



public class XMLParser 
{
	private static Document doc;

	public static void parse(String fileName) 
	{ 
		try 
		{
			File file = new File(fileName);
			DocumentBuilderFactory DBFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder DBuilder = DBFactory.newDocumentBuilder();
			doc = DBuilder.parse(file);
			doc.getDocumentElement().normalize();
		
			setSimulationParameters();
			setSystemParameters();
			createSharedCacheConfigs();
			setLatencyandEnergyParameters();
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
 	}
			

	private static void createSharedCacheConfigs() throws Exception {
		Element sharedCachesNode = (Element)doc.getElementsByTagName("SharedCaches").item(0);
		
		NodeList nodeLst = sharedCachesNode.getElementsByTagName("Cache");
		if (nodeLst.item(0) == null) {
			System.out.println("Shared caches not found !!");
		}
		
		for(int i=0; i<nodeLst.getLength(); i++) {
			Element cacheNode = (Element)nodeLst.item(i);
			CacheConfig config = createCacheConfig(cacheNode);
			config.cacheName = cacheNode.getAttribute("type");
			SystemConfig.sharedCacheConfigs.add(config);
		}
	}
	
	private static CacheConfig createCacheConfig(Element cacheNode) {
		CacheConfig config = new CacheConfig();
		
		config.nextLevel=cacheNode.getAttribute("nextLevel");
		config.cacheName = cacheNode.getAttribute("name");
		
		String cacheType = cacheNode.getAttribute("type");
		
		Element cacheTypeElmnt = searchLibraryForItem(cacheType);
		setCacheProperties(cacheTypeElmnt, config);
		
		return config;
	}
	
	private static void setLatencyandEnergyParameters()
	{
		NodeList nodeLst = doc.getElementsByTagName("OperationLatency");
		Node latencyNode = nodeLst.item(0);
		Element latencyElmnt = (Element) latencyNode;
		OperationLatencyConfig.loadLatency= Integer.parseInt(getImmediateString("load", latencyElmnt));
		OperationLatencyConfig.storeLatency= Integer.parseInt(getImmediateString("store", latencyElmnt));
		OperationLatencyConfig.addressLatency= Integer.parseInt(getImmediateString("address", latencyElmnt));
		OperationLatencyConfig.intALULatency= Integer.parseInt(getImmediateString("intALU", latencyElmnt));
		OperationLatencyConfig.intMULLatency= Integer.parseInt(getImmediateString("intMUL", latencyElmnt));
		OperationLatencyConfig.intDIVLatency= Integer.parseInt(getImmediateString("intDIV", latencyElmnt));
		OperationLatencyConfig.floatALULatency= Integer.parseInt(getImmediateString("floatALU", latencyElmnt));
		OperationLatencyConfig.floatMULLatency= Integer.parseInt(getImmediateString("floatMUL", latencyElmnt));
		OperationLatencyConfig.floatDIVLatency= Integer.parseInt(getImmediateString("floatDIV", latencyElmnt));
		OperationLatencyConfig.predicateLatency= Integer.parseInt(getImmediateString("predicate", latencyElmnt));
		OperationLatencyConfig.branchLatency= Integer.parseInt(getImmediateString("branch", latencyElmnt));
		OperationLatencyConfig.callLatency= Integer.parseInt(getImmediateString("call", latencyElmnt));
		OperationLatencyConfig.returnLatency= Integer.parseInt(getImmediateString("return", latencyElmnt));
		OperationLatencyConfig.exitLatency= Integer.parseInt(getImmediateString("exit", latencyElmnt));
		
		NodeList nodeLsts = doc.getElementsByTagName("OperationEnergy");
		Node EnergyNode = nodeLsts.item(0);
		Element EnergyElmnt = (Element) latencyNode;
		OperationEnergyConfig.loadEnergy= Integer.parseInt(getImmediateString("load", EnergyElmnt));
		OperationEnergyConfig.storeEnergy= Integer.parseInt(getImmediateString("store", EnergyElmnt));
		OperationEnergyConfig.addressEnergy= Integer.parseInt(getImmediateString("address", EnergyElmnt));
		OperationEnergyConfig.intALUEnergy= Integer.parseInt(getImmediateString("intALU", EnergyElmnt));
		OperationEnergyConfig.intMULEnergy= Integer.parseInt(getImmediateString("intMUL", EnergyElmnt));
		OperationEnergyConfig.intDIVEnergy= Integer.parseInt(getImmediateString("intDIV", EnergyElmnt));
		OperationEnergyConfig.floatALUEnergy= Integer.parseInt(getImmediateString("floatALU", EnergyElmnt));
		OperationEnergyConfig.floatMULEnergy= Integer.parseInt(getImmediateString("floatMUL", EnergyElmnt));
		OperationEnergyConfig.floatDIVEnergy= Integer.parseInt(getImmediateString("floatDIV", EnergyElmnt));
		OperationEnergyConfig.predicateEnergy= Integer.parseInt(getImmediateString("predicate", EnergyElmnt));
		OperationEnergyConfig.branchEnergy= Integer.parseInt(getImmediateString("branch", EnergyElmnt));
		OperationEnergyConfig.callEnergy= Integer.parseInt(getImmediateString("call", EnergyElmnt));
		OperationEnergyConfig.returnEnergy= Integer.parseInt(getImmediateString("return", EnergyElmnt));
		OperationEnergyConfig.exitEnergy= Integer.parseInt(getImmediateString("exit", EnergyElmnt));
				
	}

	private static void setSimulationParameters()
	{
		NodeList nodeLst = doc.getElementsByTagName("Simulation");
		Node simulationNode = nodeLst.item(0);
		Element simulationElmnt = (Element) simulationNode;
		SimulationConfig.MaxNumJavaThreads= Integer.parseInt(getImmediateString("MaxNumJavaThreads", simulationElmnt));
		SimulationConfig.MaxNumBlocksPerJavaThread=Integer.parseInt(getImmediateString("MaxNumBlocksPerJavaThread", simulationElmnt));
		if(getImmediateString("GPUType", simulationElmnt).compareTo("Tesla") == 0)			
		{
			SimulationConfig.GPUType = GpuType.Tesla; 
		}
		SimulationConfig.ThreadsPerCTA=Integer.parseInt(getImmediateString("ThreadsPerCTA", simulationElmnt));	
	}
	
	private static void setSystemParameters()
	{
		SystemConfig.declaredCaches = new Hashtable<String, CacheConfig>(); //Declare the hash table for declared caches
		
		NodeList nodeLst = doc.getElementsByTagName("System");
		Node systemNode = nodeLst.item(0);
		Element systemElmnt = (Element) systemNode;
		
		SystemConfig.NoOfTPC = Integer.parseInt(getImmediateString("NoOfTPC", systemElmnt));
		SystemConfig.mainMemoryLatency = Integer.parseInt(getImmediateString("MainMemoryLatency", systemElmnt));
		SystemConfig.mainMemoryFrequency = Long.parseLong(getImmediateString("MainMemoryFrequency", systemElmnt));
		SystemConfig.mainMemPortType = setPortType(getImmediateString("MainMemoryPortType", systemElmnt));
		SystemConfig.mainMemoryAccessPorts = Integer.parseInt(getImmediateString("MainMemoryAccessPorts", systemElmnt));
		SystemConfig.mainMemoryPortOccupancy = Integer.parseInt(getImmediateString("MainMemoryPortOccupancy", systemElmnt));
		SystemConfig.memControllerToUse=true;
		SystemConfig.cacheBusLatency = Integer.parseInt(getImmediateString("CacheBusLatency", systemElmnt));
		
		Element mainMemElmnt = (Element)(systemElmnt.getElementsByTagName("MainMemory")).item(0);
		SystemConfig.mainMemoryControllerPower = getEnergyConfig(mainMemElmnt);
		
		Element globalClockElmnt = (Element)(systemElmnt.getElementsByTagName("GlobalClock")).item(0);
		SystemConfig.globalClockPower = getEnergyConfig(globalClockElmnt);
		
	
		String interconnect = getImmediateString("Interconnect",systemElmnt); 
		if(interconnect.equalsIgnoreCase("Bus"))
		{
			SystemConfig.interconnect = SystemConfig.Interconnect.Bus;
		}
		else if(interconnect.equalsIgnoreCase("Noc"))
		{
			SystemConfig.interconnect = SystemConfig.Interconnect.Noc;
		}
		SystemConfig.tpc = new TpcConfig[SystemConfig.NoOfTPC];		
		SystemConfig.nocConfig = new NocConfig();
		NodeList NocLst = systemElmnt.getElementsByTagName("NOC");
		Element nocElmnt = (Element) NocLst.item(0);
		SystemConfig.nocConfig.power = getEnergyConfig(nocElmnt);
	
		setNocProperties(nocElmnt, SystemConfig.nocConfig);
		
		NodeList laserList = systemElmnt.getElementsByTagName("Laser");
		Element laserElmnt = (Element) laserList.item(0);
		setLaserProperties(laserElmnt);

		//set Bus Parameters
		NodeList busLst = systemElmnt.getElementsByTagName("BUS");
		Element busElmnt = (Element) busLst.item(0);
		SystemConfig.busEnergy = getEnergyConfig(busElmnt);
		SystemConfig.busConfig = new BusConfig();
		SystemConfig.busConfig.setLatency(Integer.parseInt(getImmediateString("Latency", busElmnt)));
	//	Set tpc parameters
		for(int i =0; i<SystemConfig.NoOfTPC ; i++)
		{
			SystemConfig.tpc[i] = new TpcConfig();
			TpcConfig tpc = SystemConfig.tpc[i];
			NodeList tpcLst = systemElmnt.getElementsByTagName("TPC");
			Element tpcElmnt = (Element) tpcLst.item(0);
			setTpcProperties(tpc, tpcElmnt);
		}
		
//		//Code for remaining Cache configurations
/*		NodeList cacheLst = systemElmnt.getElementsByTagName("Cache");
		Node cacheNode = cacheLst.item(0);
		Element cacheElmnt = (Element) cacheNode;
		
		CacheConfig iCacheConfigEntry = new CacheConfig();
		CacheConfig constantCacheConfigEntry = new CacheConfig();
		CacheConfig sharedConfigEntry = new CacheConfig();
		//newCacheConfigEntry.levelFromTop = Cache.CacheType.Lower;				
		NodeList iCacheList = cacheElmnt.getElementsByTagName("iCache");
		Element iCacheElmnt = (Element) iCacheList.item(0);
		String cacheType = iCacheElmnt.getAttribute("type");
		Element cacheTypeElmnt = searchLibraryForItem(cacheType);
		setCacheProperties(cacheTypeElmnt, iCacheConfigEntry);
		iCacheConfigEntry.nextLevel = cacheElmnt.getAttribute("nextLevel");
		iCacheConfigEntry.levelFromTop=Cache.CacheType.iCache;
		SystemConfig.declaredCaches.put("iCache", iCacheConfigEntry);
		
		NodeList constantCacheList = cacheElmnt.getElementsByTagName("constantCache");
		Element constantCacheElmnt = (Element) constantCacheList.item(0);
		cacheType = constantCacheElmnt.getAttribute("type");
		cacheTypeElmnt = searchLibraryForItem(cacheType);
		setCacheProperties(cacheTypeElmnt, constantCacheConfigEntry);
		constantCacheConfigEntry.nextLevel = cacheElmnt.getAttribute("nextLevel");
		constantCacheConfigEntry.levelFromTop=Cache.CacheType.constantCache;
		SystemConfig.declaredCaches.put("constantCache", constantCacheConfigEntry);
				
		NodeList sharedCacheList = cacheElmnt.getElementsByTagName("sharedCache");
		Element sharedCacheElmnt = (Element) sharedCacheList.item(0);
		cacheType = sharedCacheElmnt.getAttribute("type");
		cacheTypeElmnt = searchLibraryForItem(cacheType);
		setCacheProperties(cacheTypeElmnt,sharedConfigEntry);
		sharedConfigEntry.nextLevel = cacheElmnt.getAttribute("nextLevel");
		sharedConfigEntry.levelFromTop=Cache.CacheType.sharedCache;
		SystemConfig.declaredCaches.put("sharedCache", sharedConfigEntry);	  */		
//		
		
		if(SystemConfig.memControllerToUse==true){
			MainMemoryConfig mainMemoryConfig=new MainMemoryConfig();
			NodeList MemControllerLst = systemElmnt.getElementsByTagName("MainMemoryController");
			Element MemControllerElmnt = (Element) MemControllerLst.item(0);
			setMemControllerProperties(MemControllerElmnt,mainMemoryConfig, SystemConfig.tpc[0].sm[0].frequency);
				}	
	}
	
	
	private static void setLaserProperties(Element laserElmnt) {
		//ArchitecturalComponent.coldBus = false;
		//ArchitecturalComponent.pshare = false;
		//ArchitecturalComponent.probe = false;
		
		ArchitecturalComponent.gpushare = false;
		ArchitecturalComponent.gpuatac = false;
		
		String tempStr = getImmediateString("noctype", laserElmnt);
		
		if(tempStr.equalsIgnoreCase("gpushare"))
			ArchitecturalComponent.gpushare = true;
		else
			ArchitecturalComponent.gpuatac = true;
		
		// common for probe, coldbus and pshare
		int i,j;
		for(i=0;i<4;i++)
			for(j=0;j<4;j++)
			{
				ArchitecturalComponent.PerStationHistoryTable.add(new HashMap<Integer, Integer>());
				ArchitecturalComponent.channel_Available[i][j] = 0;
				ArchitecturalComponent.waitingTime[i][j] = 0;
				ArchitecturalComponent.stationAvailable[i][j] = 0;
				ArchitecturalComponent.requestsReceived[i][j] = 0;
				ArchitecturalComponent.requestsServiced[i][j] = 0;
			}
		
		
		
		ArchitecturalComponent.reconfInterval = Integer.parseInt(getImmediateString("interval", laserElmnt));
		ArchitecturalComponent.threshold = Integer.parseInt(getImmediateString("threshold", laserElmnt));
		ArchitecturalComponent.maxPower = Integer.parseInt(getImmediateString("maxpower", laserElmnt));
		ArchitecturalComponent.laser_tokens = ArchitecturalComponent.maxPower;
		
		for(i=0;i<ArchitecturalComponent.maxPower;i++){
			ArchitecturalComponent.laser_Available[i]=0;
		}
		
//		tempStr = getImmediateString("fullShare", laserElmnt);
//		if(tempStr.equalsIgnoreCase("false"))
//		else
//		{
//			ArchitecturalComponent.fullShare = true;
//			ArchitecturalComponent.swmr = false;
//			int i,j;
//			for(i=0;i<5;i++)
//			{
//				for(j=0;j<4;j++)
//				{
//					ArchitecturalComponent.laserStatus[i][j] = ArchitecturalComponent.maxPower;
//				}
//			}
//		}
	}

	@SuppressWarnings("static-access")
	private static void setTpcProperties(TpcConfig tpc, Element tpcElmnt) {
		
		tpc.NoOfSM = Integer.parseInt(getImmediateString("NoOfSM", tpcElmnt));
		tpc.sm = new SmConfig[TpcConfig.NoOfSM];
		//Set sm parameters
		for (int i = 0; i < TpcConfig.NoOfSM; i++)
		{
			TpcConfig.sm[i] = new SmConfig();
			SmConfig sm = TpcConfig.sm[i]; //To be locally used for assignments
			NodeList smLst = tpcElmnt.getElementsByTagName("SM");
			Element smElmnt = (Element) smLst.item(0);
			setSmProperties(sm, smElmnt);
		}
	}
	
	@SuppressWarnings("static-access")
	private static void setSmProperties(SmConfig sm, Element smElmnt) {
		sm.frequency= Integer.parseInt(getImmediateString("Frequency", smElmnt));
		sm.NoOfWarpSchedulers = Integer.parseInt(getImmediateString("NoOfWarpSchedulers", smElmnt));
		sm.NoOfSP = Integer.parseInt(getImmediateString("NoOfSP", smElmnt));
		sm.WarpSize = Integer.parseInt(getImmediateString("WarpSize", smElmnt));
		sm.sp = new SpConfig[SmConfig.NoOfSP];
		
		
		// set caches for SM
		
		setSMcaches(sm,smElmnt);
		
	/*	//Code for instruction cache configurations for each sm
		NodeList iCacheList = smElmnt.getElementsByTagName("iCache");
		Element iCacheElmnt = (Element) iCacheList.item(0);
		String cacheType = iCacheElmnt.getAttribute("type");
		Element typeElmnt = searchLibraryForItem(cacheType);
		sm.iCache.levelFromTop = CacheType.iCache;
		setCacheProperties(typeElmnt, sm.iCache);
		sm.iCache.nextLevel = iCacheElmnt.getAttribute("nextLevel");
		
		
		//Code for constant cache configurations for each sm
		NodeList constantCacheList = smElmnt.getElementsByTagName("constantCache");
		Element constantCacheElmnt = (Element) constantCacheList.item(0);
		cacheType = constantCacheElmnt.getAttribute("type");
		typeElmnt = searchLibraryForItem(cacheType);
		sm.constantCache.levelFromTop = CacheType.constantCache;
		setCacheProperties(typeElmnt, sm.constantCache);
		sm.constantCache.nextLevel = constantCacheElmnt.getAttribute("nextLevel");
				
				
		//Code for shared cache configurations for each sm
		NodeList sharedCacheList = smElmnt.getElementsByTagName("sharedCache");
		Element sharedCacheElmnt = (Element) sharedCacheList.item(0);
		cacheType = sharedCacheElmnt.getAttribute("type");
		typeElmnt = searchLibraryForItem(cacheType);
		sm.sharedCache.levelFromTop = CacheType.sharedCache;
		setCacheProperties(typeElmnt, sm.sharedCache);
		sm.sharedCache.nextLevel = sharedCacheElmnt.getAttribute("nextLevel"); */
		
		Element decodeElmnt = (Element)(smElmnt.getElementsByTagName("Decode")).item(0);
		sm.decodePower = getEnergyConfig(decodeElmnt);
		
		Element resultsBroadcastBusElmnt = (Element)(smElmnt.getElementsByTagName("ResultsBroadcastBus")).item(0);
		sm.resultsBroadcastBusPower = getEnergyConfig(resultsBroadcastBusElmnt);
		
			
		Element registerFileElmnt = (Element)(smElmnt.getElementsByTagName("RegisterFile")).item(0);
		
		Element BankElmnt = (Element)(registerFileElmnt.getElementsByTagName("Bank")).item(0);
		sm.regconfig.num_banks = Integer.parseInt(getImmediateString("BankSize", BankElmnt));
		sm.BankPower = getEnergyConfig(BankElmnt);

		Element collectorElmnt = (Element)(registerFileElmnt.getElementsByTagName("Collector")).item(0);
		sm.regconfig.num_collector_units = Integer.parseInt(getImmediateString("CollectorSize", collectorElmnt));
		sm.CollectorUnitsDecodePower = getEnergyConfig(collectorElmnt);
		

		Element dispatchElmnt = (Element)(registerFileElmnt.getElementsByTagName("Dispatch")).item(0);
		sm.regconfig.num_dispatch_units= Integer.parseInt(getImmediateString("DispatchSize", dispatchElmnt));
		sm.DispatchPower = getEnergyConfig(dispatchElmnt);
		
			
		Element AllocElmnt = (Element)(registerFileElmnt.getElementsByTagName("Alloc")).item(0);
		sm.AllocDeallocPower = getEnergyConfig(AllocElmnt);
		
		Element ArbiterElmnt = (Element)(registerFileElmnt.getElementsByTagName("Arbiter")).item(0);
		sm.ArbiterPower = getEnergyConfig(ArbiterElmnt);
		
		Element ScoreboardElmnt = (Element)(registerFileElmnt.getElementsByTagName("Scoreboard")).item(0);
		sm.ScoreBoardPower = getEnergyConfig(ScoreboardElmnt);
		
		//Set sp parameters
		for (int i = 0; i < SmConfig.NoOfSP; i++)
		{
			SmConfig.sp[i] = new SpConfig();
			SpConfig sp = SmConfig.sp[i]; //To be locally used for assignments
			NodeList spLst = smElmnt.getElementsByTagName("SP");
			Element spElmnt = (Element) spLst.item(0);
			setSpProperties(sp, spElmnt);
		}
		
	}
	
	private static void setSMcaches(SmConfig sm, Element smElmnt){
	
		CacheConfig iCacheConfigEntry = new CacheConfig();
		CacheConfig constantCacheConfigEntry = new CacheConfig();
		CacheConfig sharedConfigEntry = new CacheConfig();
		
		
		NodeList iCacheList = smElmnt.getElementsByTagName("iCache");
		Element iCacheElmnt = (Element) iCacheList.item(0);
		String cacheType = iCacheElmnt.getAttribute("type");
		Element typeElmnt = searchLibraryForItem(cacheType);
		sm.iCache.levelFromTop = CacheType.iCache;
		setCacheProperties(typeElmnt, sm.iCache);
		sm.iCache.nextLevel = iCacheElmnt.getAttribute("nextLevel");
		SystemConfig.declaredCaches.put("iCache", iCacheConfigEntry);
		
		NodeList dCacheList = smElmnt.getElementsByTagName("iCache");
		Element dCacheElmnt = (Element) iCacheList.item(0);
		cacheType = iCacheElmnt.getAttribute("type");
		typeElmnt = searchLibraryForItem(cacheType);
		sm.dCache.levelFromTop = CacheType.dCache;
		setCacheProperties(typeElmnt, sm.dCache);
		sm.dCache.nextLevel = iCacheElmnt.getAttribute("nextLevel");
		SystemConfig.declaredCaches.put("iCache", iCacheConfigEntry);
		
		//Code for constant cache configurations for each sm
		NodeList constantCacheList = smElmnt.getElementsByTagName("constantCache");
		Element constantCacheElmnt = (Element) constantCacheList.item(0);
		cacheType = constantCacheElmnt.getAttribute("type");
		typeElmnt = searchLibraryForItem(cacheType);
		sm.constantCache.levelFromTop = CacheType.constantCache;
		setCacheProperties(typeElmnt, sm.constantCache);
		sm.constantCache.nextLevel = constantCacheElmnt.getAttribute("nextLevel");
		SystemConfig.declaredCaches.put("constantCache", constantCacheConfigEntry);		
				
		//Code for shared cache configurations for each sm
		NodeList sharedCacheList = smElmnt.getElementsByTagName("sharedCache");
		Element sharedCacheElmnt = (Element) sharedCacheList.item(0);
		cacheType = sharedCacheElmnt.getAttribute("type");
		typeElmnt = searchLibraryForItem(cacheType);
		sm.sharedCache.levelFromTop = CacheType.sharedCache;
		setCacheProperties(typeElmnt, sm.sharedCache);
		sm.sharedCache.nextLevel = sharedCacheElmnt.getAttribute("nextLevel");
		SystemConfig.declaredCaches.put("sharedCache", sharedConfigEntry);		
		
	}

	@SuppressWarnings("static-access")
	private static void setSpProperties(SpConfig sp, Element spElmnt) {
		// TODO Auto-generated method stub
		sp.NoOfThreadsSupported = Integer.parseInt(getImmediateString("NoOfThreadsSupported", spElmnt));
	}

	private static String getImmediateString(String tagName, Element parent) // Get the immediate string value of a particular tag name under a particular parent tag
	{
		NodeList nodeLst = parent.getElementsByTagName(tagName);
		if (nodeLst.item(0) == null)
		{
			System.err.println("XML Configuration error : Item \"" + tagName + "\" not found inside the \"" + parent.getTagName() + "\" tag in the configuration file!!");
			System.exit(1);
		}
	    Element NodeElmnt = (Element) nodeLst.item(0);
	    NodeList resultNode = NodeElmnt.getChildNodes();
	    return ((Node) resultNode.item(0)).getNodeValue();
	}
	
	private static PortType setPortType(String inputStr)
	{
		PortType result = null;
		if (inputStr.equalsIgnoreCase("UL"))
			result = PortType.Unlimited;
		else if (inputStr.equalsIgnoreCase("FCFS"))
			result = PortType.FirstComeFirstServe;
		else if (inputStr.equalsIgnoreCase("PR"))
			result = PortType.PriorityBased;
		else
		{
			System.err.println("XML Configuration error : Invalid Port Type type specified");
			System.exit(1);
		}
		return result;
	}
	
	private static MultiPortingType setMultiPortingType(String inputStr)
	{
		MultiPortingType result = null;
		if (inputStr.equalsIgnoreCase("G"))
			result = MultiPortingType.GENUINE;
		else if (inputStr.equalsIgnoreCase("B"))
			result = MultiPortingType.BANKED;
		else
		{
			System.err.println("XML Configuration error : Invalid Multiporting type specified");
			System.exit(1);
		}
		return result;
	}

	private static Element searchLibraryForItem(String tagName)	//Searches the <Library> section for a given tag name and returns it in Element form
	{															// Used mainly for cache types
		NodeList nodeLst = doc.getElementsByTagName("Library");
		Element libraryElmnt = (Element) nodeLst.item(0);
		NodeList libItemLst = libraryElmnt.getElementsByTagName(tagName);
		
		if (libItemLst.item(0) == null) //Item not found
		{
			System.err.println("XML Configuration error : Item type \"" + tagName + "\" not found in library section in the configuration file!!");
			System.exit(1);
		}
		
		if (libItemLst.item(1) != null) //Item found more than once
		{
			System.err.println("XML Configuration error : More than one definitions of item type \"" + tagName + "\" found in library section in the configuration file!!");
			System.exit(1);
		}
		
		Element resultElmnt = (Element) libItemLst.item(0);
		return resultElmnt;
	}
	
	private static void setCacheProperties(Element CacheType, CacheConfig cache)
	{
		
		cache.cacheName = CacheType.getAttribute("name");
		String tempStr = getImmediateString("WriteMode", CacheType);
		if (tempStr.equalsIgnoreCase("WB"))
			cache.writePolicy = CacheConfig.WritePolicy.WRITE_BACK;
		else if (tempStr.equalsIgnoreCase("WT"))
			cache.writePolicy = CacheConfig.WritePolicy.WRITE_THROUGH;
		else
		{
			System.err.println("XML Configuration error : Invalid Write Mode (please enter WB for write-back or WT for write-through)");
			System.exit(1);
		}
		
		
		cache.blockSize = Integer.parseInt(getImmediateString("BlockSize", CacheType));
		cache.assoc = Integer.parseInt(getImmediateString("Associativity", CacheType));
		
		if(isElementPresent("Size", CacheType)) {
			cache.size = Integer.parseInt(getImmediateString("Size", CacheType));
		} else {
			cache.size = 0;
		}
		
		/*NodeList nodeLst = CacheType.getElementsByTagName("IsDirectory");
		if (nodeLst.item(0) == null) {
			cache.isDirectory = false;
		} else {
			cache.isDirectory = Boolean.parseBoolean(getImmediateString("IsDirectory", CacheType));
		}
		
		if(isElementPresent("NumEntries", CacheType)) {
			cache.numEntries = Integer.parseInt(getImmediateString("NumEntries", CacheType));
			cache.size = cache.numEntries*cache.blockSize;
		} else {
			cache.numEntries = 0;
		}
		
		if(cache.size==0 && cache.numEntries==0) {
			misc.Error.showErrorAndExit("Invalid cache configuration : size=0 and numEntries=0 !!");
		} */
		
		cache.latency = Integer.parseInt(getImmediateString("Latency", CacheType));
		cache.portType = setPortType(getImmediateString("PortType", CacheType));
		cache.accessPorts = Integer.parseInt(getImmediateString("AccessPorts", CacheType));
		cache.portOccupancy = Integer.parseInt(getImmediateString("PortOccupancy", CacheType));
		cache.multiportType = setMultiPortingType(getImmediateString("MultiPortingType", CacheType));
		cache.mshrSize = Integer.parseInt(getImmediateString("MSHRSize", CacheType));
		
		
		
		cache.power=getCacheEnergyConfig(CacheType);
		tempStr = getImmediateString("Coherence", CacheType);
		if (tempStr.equalsIgnoreCase("N"))
			cache.coherence = CoherenceType.None;
		else if (tempStr.equalsIgnoreCase("S"))
			cache.coherence = CoherenceType.Snoopy;
		else if (tempStr.equalsIgnoreCase("D"))
			cache.coherence = CoherenceType.Directory;
		else
		{
			System.err.println("XML Configuration error : Invalid value of 'Coherence' (please enter 'S', D' or 'N')");
			System.exit(1);
		}
		cache.numberOfBuses = Integer.parseInt(getImmediateString("NumBuses", CacheType));
		cache.busOccupancy = Integer.parseInt(getImmediateString("BusOccupancy", CacheType));
		tempStr = getImmediateString("Nuca", CacheType);
		cache.nucaType = NucaType.valueOf(tempStr);
		
		tempStr = getImmediateString("NucaMapping", CacheType);
		if (tempStr.equalsIgnoreCase("S"))
			cache.mapping = Mapping.SET_ASSOCIATIVE;
		else if (tempStr.equalsIgnoreCase("A"))
			cache.mapping = Mapping.ADDRESS;
		else
		{
			System.err.println("XML Configuration error : Invalid value of 'NucaMapping' (please enter 'S'or 'A')");
			System.exit(1);
		}
				
		tempStr = getImmediateString("LastLevel", CacheType);
		if (tempStr.equalsIgnoreCase("Y"))
			cache.isLastLevel = true;
		else if (tempStr.equalsIgnoreCase("N"))
			cache.isLastLevel = false;
		else
		{
			System.err.println("XML Configuration error : Invalid value of 'isLastLevel' (please enter 'Y' for yes or 'N' for no)");
			System.exit(1);
		}
		
	}
	
	
	private static boolean isElementPresent(String tagName, Element parent) // Get the immediate string value of a particular tag name under a particular parent tag
	{
		NodeList nodeLst = parent.getElementsByTagName(tagName);
		if (nodeLst.item(0) == null) {
			return false;
		} else {
			return true;
		}
	}
	
	private static void setNocProperties(Element NocType, NocConfig nocConfig)
	{
		if(SystemConfig.interconnect==SystemConfig.Interconnect.Noc) {
			String nocConfigFilename = getImmediateString("NocConfigFile", NocType);
			nocConfig.NocTopologyFile = nocConfigFilename;
		}
		
		nocConfig.numberOfBuffers = Integer.parseInt(getImmediateString("NocNumberOfBuffers", NocType));
		nocConfig.portType = setPortType(getImmediateString("NocPortType", NocType));
		nocConfig.accessPorts = Integer.parseInt(getImmediateString("NocAccessPorts", NocType));
		nocConfig.portOccupancy = Integer.parseInt(getImmediateString("NocPortOccupancy", NocType));
		nocConfig.latency = Integer.parseInt(getImmediateString("NocLatency", NocType));
		nocConfig.operatingFreq = Integer.parseInt(getImmediateString("NocOperatingFreq", NocType));
		nocConfig.latencyBetweenNOCElements = Integer.parseInt(getImmediateString("NocLatencyBetweenNOCElements", NocType));
		
		String tempStr = getImmediateString("NocTopology", NocType);
		nocConfig.topology = TOPOLOGY.valueOf(tempStr);
		
		tempStr = getImmediateString("NocRoutingAlgorithm", NocType);
		nocConfig.rAlgo = RoutingAlgo.ALGO.valueOf(tempStr);
				
		tempStr = getImmediateString("NocSelScheme", NocType);
		nocConfig.selScheme = RoutingAlgo.SELSCHEME.valueOf(tempStr);
		
		tempStr = getImmediateString("NocRouterArbiter", NocType);
		nocConfig.arbiterType = RoutingAlgo.ARBITER.valueOf(tempStr);
				
		nocConfig.technologyPoint = Integer.parseInt(getImmediateString("TechPoint", NocType));
		
		tempStr = getImmediateString("NocConnection", NocType);
		nocConfig.ConnType = CONNECTIONTYPE.valueOf(tempStr);
	}
	private static void setMemControllerProperties(Element MemControllerElmnt, MainMemoryConfig mainMemConfig, int sm_freq){
		
		mainMemConfig.rowBufferPolicy = setRowBufferPolicy(getImmediateString("rowBufferPolicy", MemControllerElmnt));
		mainMemConfig.schedulingPolicy = setSchedulingPolicy(getImmediateString("schedulingPolicy", MemControllerElmnt));
		mainMemConfig.queuingStructure = setQueuingStructure(getImmediateString("queuingStructure", MemControllerElmnt));
		mainMemConfig.numRankPorts=Integer.parseInt(getImmediateString("numRankPorts", MemControllerElmnt));
		mainMemConfig.rankPortType = setPortType(getImmediateString("rankPortType", MemControllerElmnt));
		mainMemConfig.rankOccupancy=Integer.parseInt(getImmediateString("rankOccupancy", MemControllerElmnt));
		mainMemConfig.rankLatency=Integer.parseInt(getImmediateString("rankLatency", MemControllerElmnt));	//this is not used anywhere as we are modelling the RAM and bus
		mainMemConfig.rankOperatingFrequency=Integer.parseInt(getImmediateString("rankOperatingFrequency", MemControllerElmnt));
		
		mainMemConfig.numChans=Integer.parseInt(getImmediateString("numChans", MemControllerElmnt));
		
		//those related to memory not added
				//calculate later
				
		mainMemConfig.numRanks=Integer.parseInt(getImmediateString("numRanks", MemControllerElmnt));
		mainMemConfig.numBanks=Integer.parseInt(getImmediateString("numBanks", MemControllerElmnt));
		mainMemConfig.numRows=Integer.parseInt(getImmediateString("numRows", MemControllerElmnt));
		mainMemConfig.numCols=Integer.parseInt(getImmediateString("numCols", MemControllerElmnt));
		mainMemConfig.TRANSQUEUE_DEPTH=Integer.parseInt(getImmediateString("TRANSQUEUE_DEPTH", MemControllerElmnt));
		mainMemConfig.TOTAL_ROW_ACCESSES=Integer.parseInt(getImmediateString("TOTAL_ROW_ACCESSES", MemControllerElmnt));

		mainMemConfig.tCK=Double.parseDouble(getImmediateString("tCK", MemControllerElmnt));

		int ram_freq = (int)((1/mainMemConfig.tCK)*1000);
		double cpu_ram_ratio = sm_freq/ram_freq;
		mainMemConfig.sm_ram_ratio = cpu_ram_ratio;

		//timing params
		mainMemConfig.tCCD = (int) Math.round(Integer.parseInt(getImmediateString("tCCD", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tBL = (int) Math.round(Integer.parseInt(getImmediateString("tBL", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tCL = (int) Math.round(Integer.parseInt(getImmediateString("tCL", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tAL = (int) Math.round(Integer.parseInt(getImmediateString("tAL", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tRP = (int) Math.round(Integer.parseInt(getImmediateString("tRP", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tCMD = (int) Math.round(Integer.parseInt(getImmediateString("tCMD", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tRC = (int) Math.round(Integer.parseInt(getImmediateString("tRC", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tRCD = (int) Math.round(Integer.parseInt(getImmediateString("tRCD", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tRAS = (int) Math.round(Integer.parseInt(getImmediateString("tRAS", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tRFC = (int) Math.round(Integer.parseInt(getImmediateString("tRFC", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tRTRS = (int) Math.round(Integer.parseInt(getImmediateString("tRTRS", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tRRD = (int) Math.round(Integer.parseInt(getImmediateString("tRRD", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tRTP = (int) Math.round(Integer.parseInt(getImmediateString("tRTP", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tWTR = (int) Math.round(Integer.parseInt(getImmediateString("tWTR", MemControllerElmnt))*cpu_ram_ratio);
		mainMemConfig.tWR = (int) Math.round(Integer.parseInt(getImmediateString("tWR", MemControllerElmnt))*cpu_ram_ratio);
		
		//for refresh
		mainMemConfig.RefreshPeriod=Integer.parseInt(getImmediateString("RefreshPeriod", MemControllerElmnt));
		mainMemConfig.DATA_BUS_BITS=Integer.parseInt(getImmediateString("DATA_BUS_BITS", MemControllerElmnt));
		
		//dont need to multiply for tFAW as it runs on RAM clock anyway
		mainMemConfig.tFAW=Integer.parseInt(getImmediateString("tFAW", MemControllerElmnt));
		mainMemConfig.BL=Integer.parseInt(getImmediateString("tBL", MemControllerElmnt));  //this is the number of bursts, not scaled to cpu clock
		//used for adressing mapping etc
		
		mainMemConfig.tRL = (mainMemConfig.tCL+mainMemConfig.tAL);
		mainMemConfig.tWL = (int) Math.round(mainMemConfig.tRL-1*cpu_ram_ratio);
		mainMemConfig.ReadToPreDelay = (mainMemConfig.tAL+mainMemConfig.tBL/2+ Math.max(mainMemConfig.tRTP,mainMemConfig.tCCD)-mainMemConfig.tCCD);
		mainMemConfig.WriteToPreDelay = (mainMemConfig.tWL+mainMemConfig.tBL/2+mainMemConfig.tWR);
		mainMemConfig.ReadToWriteDelay = (mainMemConfig.tRL+mainMemConfig.tBL/2+mainMemConfig.tRTRS-mainMemConfig.tWL);
		mainMemConfig.ReadAutopreDelay = (mainMemConfig.tAL+mainMemConfig.tRTP+mainMemConfig.tRP);
		mainMemConfig.WriteAutopreDelay = (mainMemConfig.tWL+mainMemConfig.tBL/2+mainMemConfig.tWR+mainMemConfig.tRP);
		mainMemConfig.WriteToReadDelayB = (mainMemConfig.tWL+mainMemConfig.tBL/2+mainMemConfig.tWTR);
		mainMemConfig.WriteToReadDelayR = (mainMemConfig.tWL+mainMemConfig.tBL/2+mainMemConfig.tRTRS-mainMemConfig.tRL);
		
		
		//bus params
		mainMemConfig.TRANSACTION_SIZE = mainMemConfig.DATA_BUS_BITS/8 * mainMemConfig.BL;
		mainMemConfig.DATA_BUS_BYTES = mainMemConfig.DATA_BUS_BITS/8;
		SystemConfig.mainMemoryConfig=mainMemConfig;
	
	}

	private static RowBufferPolicy setRowBufferPolicy(String inputStr)
	{
		RowBufferPolicy result = null;
		if (inputStr.equalsIgnoreCase("OpenPage"))
			result = RowBufferPolicy.OpenPage;
		else if (inputStr.equalsIgnoreCase("ClosePage"))
			result = RowBufferPolicy.ClosePage;
		else
		{
			System.err.println("XML Configuration error : Invalid Row Buffer Policy specified");
			System.exit(1);
		}
		return result;
	}
	//added by kush
	
	private static SchedulingPolicy setSchedulingPolicy(String inputStr)
	{
		SchedulingPolicy result = null;
		if (inputStr.equalsIgnoreCase("RankThenBankRoundRobin"))
			result = SchedulingPolicy.RankThenBankRoundRobin;
		else if (inputStr.equalsIgnoreCase("BankThenRankRoundRobin"))
			result = SchedulingPolicy.BankThenRankRoundRobin;
		else
		{
			System.err.println("XML Configuration error : Invalid DRAM Scheduling Policy specified");
			System.exit(1);
		}
		return result;
	}
	//added by kush
	
	private static QueuingStructure setQueuingStructure(String inputStr)
	{
		QueuingStructure result = null;
		if (inputStr.equalsIgnoreCase("PerRank"))
			result = QueuingStructure.PerRank;
		else if (inputStr.equalsIgnoreCase("PerRankPerBank"))
			result = QueuingStructure.PerRankPerBank;
		else
		{
			System.err.println("XML Configuration error : Invalid DRAM Queuing Structure specified");
			System.exit(1);
		}
		return result;
	}
	private static EnergyConfig getEnergyConfig(Element parent)
	{
		double leakageEnergy = Double.parseDouble(getImmediateString("LeakageEnergy", parent));
		double dynamicEnergy = Double.parseDouble(getImmediateString("DynamicEnergy", parent));
		
		EnergyConfig energyConfig = new EnergyConfig(leakageEnergy, dynamicEnergy);
		return energyConfig;
	}
	
	private static CacheEnergyConfig getCacheEnergyConfig(Element parent)
	{
		CacheEnergyConfig powerConfig = new CacheEnergyConfig();
		powerConfig.leakageEnergy = Double.parseDouble(getImmediateString("LeakagePower", parent));
		powerConfig.readDynamicEnergy = Double.parseDouble(getImmediateString("ReadDynamicPower", parent));
		powerConfig.writeDynamicEnergy = Double.parseDouble(getImmediateString("WriteDynamicPower", parent));
		return powerConfig;
	}
	
}