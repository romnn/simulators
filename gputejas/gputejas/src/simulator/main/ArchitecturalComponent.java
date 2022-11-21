package main;
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


import config.SystemConfig;
import config.TpcConfig;
import config.CacheConfig;
import memorysystem.SMMemorySystem;
import memorysystem.directory.CentralizedDirectoryCache;
import memorysystem.NucaCache;
import memorysystem.MemorySystem;
import generic.SM;
import dram.MainMemoryDRAMController;
import emulatorinterface.communication.IpcBase;
import generic.CommunicationInterface;
//import generic.Core;
import generic.CoreBcastBus;
import generic.EventQueue;
import generic.GlobalClock;
import generic.LocalClockperSm;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Collection;
import config.NetworkDelay;
import config.CacheConfig;
import config.MainMemoryConfig;
import config.SystemConfig;
import memorysystem.Cache;
//import memorysystem.CoreMemorySystem;
import dram.MainMemoryDRAMController;
import memorysystem.MemorySystem;
//import memorysystem.coherence.Coherence;
//import memorysystem.nuca.NucaCache;
import net.Bus;
import net.BusInterface;
import net.InterConnect;
import net.NOC;
import net.Router;

public class ArchitecturalComponent {

	//public static Vector<Vector<SM>> sm= new Vector<Vector<SM>>(); 
	public static SM[][] cores;
	public static long tomemory;
	public static Vector<Cache> sharedCaches = new Vector<Cache>();
	public static Vector<Cache> caches = new Vector<Cache>();
//	public static HashMap<String, NucaCache> nucaList=  new HashMap<String, NucaCache>();
	private static InterConnect interconnect;
	public static CoreBcastBus coreBroadcastBus;
	public static HashMap<String, NucaCache> nucaList=  new HashMap<String, NucaCache>();
	public static HashMap<Long, Long> missList = new HashMap<Long, Long>();
	public static Vector<MainMemoryDRAMController> memoryControllers = new Vector<MainMemoryDRAMController>();

	public static Bus bus = new Bus();
	public static void setInterConnect(InterConnect i) {
	interconnect = i;
}
	public static InterConnect getInterConnect() {
		return interconnect;
	}
	
public static void createChip() {
		// Interconnect
			// Core
			// Coherence
			// Shared Cache
			// Main Memory Controller

		if(SystemConfig.interconnect ==  SystemConfig.Interconnect.Bus) {
			ArchitecturalComponent.setInterConnect(new Bus());
			createElementsOfBus();
		} else if(SystemConfig.interconnect == SystemConfig.Interconnect.Noc) {
			ArchitecturalComponent.setInterConnect(new NOC(SystemConfig.nocConfig));
			createElementsOfNOC();			
			((NOC)interconnect).ConnectNOCElements();
		}
		
		
		initMemorySystem(getCores());
		initCoreBroadcastBus();
		MemorySystem.createLinkBetweenCaches();
		GlobalClock.setCurrentTime(0);
		
	}
	public static SM[][] initCores()
	{
		System.out.println("initializing cores...");
		
		
		SM[][] sms = new SM[SystemConfig.NoOfTPC][TpcConfig.NoOfSM];
		for (int i=0; i<SystemConfig.NoOfTPC; i++)
		{
			for(int j =0; j<TpcConfig.NoOfSM; j++)
			{
				sms[i][j] = new SM(i, j);
			}
		
		}
		return sms;
	}
	
	public static SM[][] getCores() {
		return cores;
	}

	public static void setCores(SM[][] cores) {
		ArchitecturalComponent.cores = cores;
	}

	public static long getNoOfInstsExecuted()
	{
		long noOfInstsExecuted = 0;
		for(int i = 0; i < ArchitecturalComponent.getCores().length; i++)
		{
			for(int j =0 ; j<ArchitecturalComponent.getCores()[i].length ; j++ )
			{
				noOfInstsExecuted += ArchitecturalComponent.getCores()[i][j].getNoOfInstructionsExecuted();
			}
			
		}
		return noOfInstsExecuted;
	}

		
	private static SMMemorySystem[][] coreMemSysArray;
	public static SMMemorySystem[][] getCoreMemSysArray()
	{
		return coreMemSysArray;
	}
private static void createElementsOfBus() {
	
	
		
		
		BusInterface busInterface;
		SM[][] sms = initCores();
		for(int i=0;i<SystemConfig.NoOfTPC;i++){
			for(int j=0;j<TpcConfig.NoOfSM;j++){
				busInterface= new BusInterface(bus);
				sms[i][j].setComInterface(busInterface);
			}
		}
		setCores(sms);
		
		
		for(CacheConfig cacheConfig : SystemConfig.sharedCacheConfigs) {
			busInterface = new BusInterface(bus);
			Cache c = MemorySystem.createSharedCache(cacheConfig.cacheName, busInterface);
		}
		
		

//		System.out.println(SystemConfig.mainMemoryConfig.numChans);
		if(SystemConfig.memControllerToUse==true){
			for(int i=0;i<SystemConfig.mainMemoryConfig.numChans;i++){
				MainMemoryDRAMController mainMemController = new MainMemoryDRAMController(SystemConfig.mainMemoryConfig);
				mainMemController.setChannelNumber(i);
				busInterface = new BusInterface(bus);
				mainMemController.setComInterface(busInterface);
				memoryControllers.add(mainMemController);
			}
		}
		else{
			MainMemoryDRAMController mainMemController = new MainMemoryDRAMController(SystemConfig.mainMemoryConfig);
				mainMemController.setChannelNumber(0);
				busInterface = new BusInterface(bus);
				mainMemController.setComInterface(busInterface);
				memoryControllers.add(mainMemController);
		}
		
		}
//	
@SuppressWarnings("unused")
private static void createElementsOfNOC() {

	
	Collection<CacheConfig> c= SystemConfig.declaredCaches.values();
	CacheConfig cc;
	
	int TPC_number=-1;
	int SM_number_withinTPC=-1;    
	setCores(initCores());
	//create elements mentioned as topology file
	BufferedReader readNocConfig = NOC.openTopologyFile(SystemConfig.nocConfig.NocTopologyFile);
	
	// Skip the first line. It contains numrows/cols information
	try {
		readNocConfig.readLine();
	} catch (IOException e1) {
		misc.Error.showErrorAndExit("Error in reading noc topology file !!");
	}
	
	int numRows = ((NOC)interconnect).getNumRows();
	int numColumns = ((NOC)interconnect).getNumColumns();
	for(int i=0;i<numRows;i++)
	{
		String str = null;
		try {
			str = readNocConfig.readLine();
		} catch (IOException e) {
			misc.Error.showErrorAndExit("Error in reading noc topology file !!");
		}
		
		StringTokenizer st = new StringTokenizer(str);
		
		for(int j=0;j<numColumns;j++)
		{
			String nextElementToken = (String)st.nextElement();
			
			//System.out.println("NOC [" + i + "][" + j + "] = " + nextElementToken);
			
			CommunicationInterface comInterface = ((NOC)interconnect).getNetworkElements()[i][j];
			if(nextElementToken.equals("TPC")){
				TPC_number++;
				SM_number_withinTPC=-1;
			}
			else if(nextElementToken.equals("SM")){
				if(TPC_number==-1){
					misc.Error.showErrorAndExit("There should be a TPC before any SM !!");
				}else{
					SM_number_withinTPC++;
					//System.out.println("( "+TPC_number+" "+SM_number_withinTPC+" )");
					cores[TPC_number][SM_number_withinTPC].setComInterface(comInterface);
				}
			} else if(nextElementToken.equals("M")) {
				MainMemoryDRAMController mainMemController = new MainMemoryDRAMController(SystemConfig.mainMemoryConfig);
				memoryControllers.add(mainMemController);
				mainMemController.setComInterface(comInterface);
			} else if(nextElementToken.equals("-")) {
				//do nothing
			} 
			else if(nextElementToken.equals("CD")) {
				Iterator<CacheConfig> it= c.iterator();
				while(it.hasNext()) {
					cc= it.next();
					if(cc.levelFromTop.equals(Cache.CacheType.Directory)) {
						CentralizedDirectoryCache cache= new CentralizedDirectoryCache(cc, null,NetworkDelay.networkDelay);
						cache.setComInterface(comInterface);
						break;
					}
				}
			}
			else if (nextElementToken.equals("L2")){
				
				Cache cc1 = MemorySystem.createSharedCache(nextElementToken, comInterface);
				
				//Cache c = MemorySystem.createSharedCache(cacheConfig.cacheName, busInterface);
				
					
				}
					else {
						misc.Error.showErrorAndExit("Noc config file has an issue");
						} 
		}
	}
}	
	public static void initMemorySystem(SM[][] sms) {
		coreMemSysArray = MemorySystem.initializeMemSys(ArchitecturalComponent.getCores());		
	}
	private static ArrayList<Router> nocRouterList = new ArrayList<Router>();
	
	public static void addNOCRouter(Router router) {
		nocRouterList.add(router);		
	}
	
	public static SM createSM(int tpc_number, int sm_number){
		return new SM(tpc_number, sm_number);
	}
	
	public static ArrayList<Router> getNOCRouterList() {
		return nocRouterList;
	}

	public static void initCoreBroadcastBus() {
		coreBroadcastBus = new CoreBcastBus();		
	}

	public static MainMemoryDRAMController getMainMemoryDRAMController(CommunicationInterface comInterface,int chanNum) {
		//TODO : return the nearest memory controller based on the location of the communication interface
		return memoryControllers.get(chanNum);
	}
	
	public static Vector<Cache> getCacheList() {
		return caches;
	}
	
	public static Vector<Cache> getSharedCacheList() {
		return sharedCaches;
	}

	//for optical noc
	public static long reconfInterval=100;// = 100; epoch size
	public static int laser_tokens;    // no of tokens send
	public static long[][] channel_Available = new long[4][4];   // whether the MWSR channel is available or not
	public static long[] laser_Available = new long[16];   // Whether the laser is available or not
	public static int[][] waitingTime   = new int[4][4];//waiting time at each station due to non availability of laser
	public static long[][] stationAvailable = new long[4][4]; // for coherence only 8 stations use this
	public static long[][] requestsReceived = new long[4][4];  // requests received by each station in current epoch
	public static long[][] requestsServiced = new long[4][4];  // requests serviced by each station in current epoch
	public static boolean gpushare;   //gpupshare with laser modulation
	public static boolean gpuatac;   //gpupshare with no laser modulation
	public static long waitLaser = 0;  // no of requests waited due to non availability of laser
	public static long waitStation = 0; // no of requests waited because statin was busy
	public static long waitchannel = 0; // no of requets waiting because of non availability of channel
	public static int[][] waitingTimeStation   = new int[4][4];//waiting time at each station due to non availability of station or channel
	public static long laseron = 0;
	public static long requestsThreshold = 32;
	public static long waitingThreshold = 500;
	public static long[][] laser_Status_prev = new long[4][4]; 
	public static int previoushistoryindex;
	public static int previousSMpower;
	public static HashMap<Integer,Integer> historyTable = new HashMap<Integer, Integer>();
	
	
	//variables from cpu optical
	public static int threshold;// = 5; // Threshold for 
	public static int maxPower;// = 16; //max power from outside
	
	public static List<HashMap<Integer,Integer>> PerStationHistoryTable = new ArrayList<HashMap<Integer, Integer>>();
	public static int[][] stationActive = new int[5][5]; // 25 stations considered, which station is active
    public static int[][] laserStatus   = new int[5][5]; // laser status for each station whether available or not
    public static int[][] laserStatus_History = new int[5][5];  
    public static int laserStatus_bigbus;
    public static int laserStatus_cshare =2;
	public static int waitingTime_bigbus;
	public static int waitingTime_cshare;
	public static int[][] stationHistory= new int[5][5];//history of station in last epoch
	public static int stationHistory_bigbus;
	public static int stationHistory_cshare;
	public static int pendingEvents;
	public static long[] laserAvailable;// = new long[20][maxPower];
	
	public static long[][] laserON = new long[5][5];//dimension changed for probe
	public static long laserOff = 0;
	public static long reconf = 0;
	public static long waitTime;
	public static long actualReq;
	public static long prevReq;
	public static long totalReq;
	public static long waitOFF = 0; 
	public static long waitTraffic = 0;
	public static boolean atac;// = false;
	public static boolean swmr;// = false;
	public static boolean probe;
	public static boolean coldBus;
	public static boolean pshare;
	public static boolean cshare_coldbus;
	public static long ideal_Power;
	
	
	public static boolean fullShare;
	public static long[][] trafficused = new long[5][5];
	public static HashMap<Long,Long> reqDistribution = new HashMap<Long, Long>();
	
	public static long[][] numStationRequests = new long[5][5];
	public static long[][] numPrevStationRequests = new long[5][5];
	public static HashMap<Long,Long> stationAccess = new HashMap<Long, Long>();
	public static boolean staggered;
	public static long throughHub=0;
	public static boolean limitedBroadcast;
	
	public static int probeLoss;
	public static HashMap<Integer,Long> activeStations = new HashMap<Integer, Long>();
	public static long[][] hubRequests = new long[2][2];
	public static long hubOverflow0=0;
	public static long hubOverflow1=0;
	public static long hubOverflow2=0;
	public static long hubOverflow3=0;
	public static long hubOverflow4=0;
	
	public static int[][][] loadStoreCounter = new int[5][5][1024];
	//public static int[] EAT = new int[1048576];
	public static int[][][] EPT = new int[5][5][1024];   //false means it requires light, true means it does not require light
	public static long totalhit;
	public static long totalmiss;
	public static long correctpredictionsm;// epoch prediction for miss only
	public static long correctpredictions;  //epoch prediction combine hit and miss
	public static long correcthitpredictions;
	public static long correctmisspredictions;
	public static long[][][] loadstoreTime = new long[5][5][1024];
	public static long[][][] loadTime = new long[5][5][1024];
	public static long[][][] storeTime = new long[5][5][1024];
	
	
	public static HashMap<Vector<Long>,Long> fetchTime = new HashMap<Vector<Long>, Long>();
	public static long extra;
	public static long[] extraAvailable = new long[16];
	public static long totalExtraUsed = 0;
	public static long[][] TotalStationRequests = new long[5][5];
	
	public static void laserReconfigurationProbe()
	{
		int i,j;
		for(i=0;i<5;i++)
		{
			for(j=0;j<5;j++)
			{
				//training
				int currentHistory = stationHistory[i][j];
				int value=laserStatus[i][j];
				
				PerStationHistoryTable.get(i*4+j).put(currentHistory, value);
				stationHistory[i][j] = (currentHistory*10+value)%(10^8);
				
				//prediction
				int history = stationHistory[i][j];
				int prediction = 0;
				if(PerStationHistoryTable.get(i*4+j).containsKey(history))
					prediction = PerStationHistoryTable.get(i*4+j).get(history);
				laserStatus[i][j] = prediction;
				if(waitingTime[i][j] > 0 )
				{
					laserStatus[i][j]=1;
				}
				else if(numStationRequests[i][j]==0)
				{
					laserStatus[i][j]=0;
				}
				numStationRequests[i][j]=0;
				waitingTime[i][j]=0;
				laserON[i][j] += laserStatus[i][j];
				probeLoss+= laserStatus[i][j];
			}
		}
		//for(i=0;i<5;i++)
		//{
			//for(j=0;j<5;j++)
			//{
				if(activeStations.containsKey(probeLoss))
					activeStations.put(probeLoss, activeStations.get(probeLoss)+1);
				else
					activeStations.put(probeLoss,(long) 0);
				probeLoss=0;
			//}
		//}
		
	}
	
	
	public static void laserReconfigurationGpu()
	{
		int i,j,currenthistoryindex = 0;
		
		int power = 0;
		int Lpower=0;
		int waitcount = 0;
		
		int preLpower;
		laseron += laser_tokens;
		int reqRe =0 , reqser =0, wait = 0;
		long time = GlobalClock.getCurrentTime();
		preLpower = laser_tokens - previousSMpower;
		
		for(i=0;i<maxPower;i++)
			laser_Available[i] = time;
		
		
		for(i=0;i<4;i++)
		{
			for(j=0;j<4;j++)
			{
				
				if(j==0 || j == 3)  // for cache optical stations
				{
					if(waitingTime[i][j] == (int)reconfInterval)
						waitcount++;
				
					if(requestsReceived[i][j] > requestsThreshold || waitingTime[i][j] > waitingThreshold || 
							(requestsReceived[i][j] - requestsServiced[i][j]) > requestsThreshold/4	
							)	
					{
						currenthistoryindex = currenthistoryindex*2 + 1;
						Lpower++;
					}
					
					else
						currenthistoryindex = currenthistoryindex*2;
				
				requestsReceived[i][j] = requestsReceived[i][j] - requestsServiced[i][j];
				requestsServiced[i][j] = 0;
				waitingTime[i][j] = 0;
				waitingTimeStation[i][j] = 0;
					
				}
				else //for SM optical stations
				{
					reqRe +=requestsReceived[i][j];
					reqser +=requestsServiced[i][j];
					wait = (int) Math.max(waitingTime[i][j],  wait);
					
					
					if(
						(requestsReceived[i][j] >  requestsThreshold && requestsServiced[i][j] < requestsThreshold/2) || 
							
						waitingTime[i][j] > waitingThreshold || 
						
						(requestsReceived[i][j] <  requestsThreshold && requestsServiced[i][j] < requestsReceived[i][j]/2)
						
						)
					{
					 power++;
					}
					else
					{
					// no power required	
					}
					
					requestsReceived[i][j] = requestsReceived[i][j] - requestsServiced[i][j];
					requestsServiced[i][j] = 0;
					waitingTime[i][j] = 0;
					waitingTimeStation[i][j] = 0;
				}
			}
		}
		
		if(power > maxPower/2)
			power = maxPower/2;
		
		if(historyTable.get(currenthistoryindex)!=null){
			Lpower = historyTable.get(currenthistoryindex);
			if(waitcount > 2){
				historyTable.put(previoushistoryindex, preLpower+1);
			}
			else if(waitcount == 0)
				historyTable.put(previoushistoryindex, preLpower);
			else{
				
			}
		}
		else
		{
			
			historyTable.put(currenthistoryindex,Lpower);
		}
		
		previoushistoryindex = currenthistoryindex;
		previousSMpower = power;
		laser_tokens = Lpower + power;
		
		if(laser_tokens > maxPower)
			laser_tokens = maxPower;
		
	}
	
	public static void laserReconfiguration()
	{
		if(gpuatac)
		{
			laseron +=maxPower;
			return;
		}
		reconf++;
		
		if(gpushare)
		{
			laserReconfigurationGpu();
			return;
		}
		if(probe)
		{
			laserReconfigurationProbe();
			return;
		}
		
		
		int i,j;

		//statistics
		long access = actualReq-prevReq;
		if(reqDistribution.containsKey(access))
			reqDistribution.put(access, reqDistribution.get(access)+1);
		else
			reqDistribution.put(access, (long) 1);
		prevReq = actualReq;
		
		//training
		int currentHistory = stationHistory_bigbus;
		int value=laserStatus_bigbus;
				
		historyTable.put(currentHistory, value);
		stationHistory_bigbus = (currentHistory*10+value)%(10^8);
				
		//prediction
		int history = stationHistory_bigbus;
		int prediction = 0;
		if(historyTable.containsKey(history))
			prediction = historyTable.get(history);
		if(waitingTime_bigbus > threshold || prediction > laserStatus_bigbus)
		{
			if(laserStatus_bigbus < maxPower)
				laserStatus_bigbus++;
		}
		else if(laserStatus_bigbus > 1 )
		{
			laserStatus_bigbus = laserStatus_bigbus/2;
		}
		else if(laserStatus_bigbus == 1 && prediction == 0)
		{
			laserStatus_bigbus = 0;
		}
		if(pendingEvents/(reconfInterval/2) > laserStatus_bigbus)
		{
			laserStatus_bigbus = (int) Math.min(maxPower,pendingEvents/(reconfInterval/2));
		}
		laserON[0][0] += laserStatus_bigbus;
			
		
		
	}


	public static long availableTime(int i, int j, int k, int l,boolean coherence)
	{
		long time = GlobalClock.getCurrentTime();
		int idx = i/2;int idy = j/2;
		 trafficused[idx][idy]++;
		if(gpushare || gpuatac )
			return findTimegpu(i,j,k,l, coherence);   //for both gpuatac and spupower share
		
		
		if(swmr && (stationAvailable[idx][idy] > time)) //if station is busy go back
		{
			waitStation++;
			numStationRequests[idx][idy]++;
			waitingTime_bigbus = (int) (stationAvailable[idx][idy] - time);
			return -1;
		}
				
		if(laserStatus_bigbus==0) //if laser is off, go back
		{
			waitOFF++;
			waitingTime_bigbus = (int) reconfInterval;
			return -1;
		}
		long min = Long.MAX_VALUE;
		int minIndex=-1, m, latency=SystemConfig.nocConfig.latencyBetweenNOCElements;
		for(m=0;m<laserStatus_bigbus;m++) //find the earliest token
		{
			if(laserAvailable[m] < min)
			{
				min = laserAvailable[m];
				minIndex = m;
			}
		}
		if(min/reconfInterval > time/reconfInterval) //if station is not available in this epoch, go back
		{
			waitTraffic++;
			waitingTime_bigbus = (int) reconfInterval;
			return -1;
		}
		if(min<time) 
		{
			waitingTime_bigbus = Math.max(waitingTime_bigbus, latency);
			laserAvailable[minIndex] = time + latency;
		}
		else
		{
			laserAvailable[minIndex] = laserAvailable[minIndex]+latency;
			waitingTime_bigbus = (int) Math.max(waitingTime_bigbus, (laserAvailable[minIndex] - time));
		}
		stationAvailable[idx][idy] = laserAvailable[minIndex];
		if(swmr){
			long stAccess = 0;
			if(numStationRequests[idx][idy]>0)
			{
				if(numPrevStationRequests[idx][idy] > 0)
					stAccess = numStationRequests[idx][idy] - (numPrevStationRequests[idx][idy] - 1);
				else
					stAccess = numStationRequests[idx][idy];
			}
			numPrevStationRequests[idx][idy] = numStationRequests[idx][idy];
			numStationRequests[idx][idy] = 0;
			if(stationAccess.get(stAccess) == null)
				stationAccess.put(stAccess,(long) 1);
			else
				stationAccess.put(stAccess,stationAccess.get(stAccess)+1);
		}
		return laserAvailable[minIndex];
	}
	
	
	
	private static long findTimegpu(int i, int j, int k, int l, boolean coherence){
		
		//System.out.println("yes it is going in");
		long time = GlobalClock.getCurrentTime();
		int idx = i/2;int idy = j/2;
		int dx = k/2; int dy = l/2;
		
		long min = Long.MAX_VALUE;
		int minIndex=-1, m, latency=SystemConfig.nocConfig.latencyBetweenNOCElements;
		for(m=0;m<laser_tokens;m++) //find the earliest token
		{
			if(laser_Available[m] < min)
			{
				min = laser_Available[m];
				minIndex = m;
			}
		}
		
		if(min/reconfInterval > time/reconfInterval) //if laser is not available in this epoch
		{
			
			
			waitLaser++;
			waitingTime[idx][idy] = (int) reconfInterval;
			return -1;
		}
		
		if(min<time)  //laser is currently available 
		{
			
			// check for the availability of channel
			//if non coherence message
			if(!coherence){
				if(channel_Available[dx][dy]<=time){
					laser_Available[minIndex] = time + latency;
					channel_Available[dx][dy] = time + latency;
				}
				else
				{
					waitchannel++;
					waitingTimeStation[dx][dy] = (int) Math.max(waitingTimeStation[dx][dy] , (channel_Available[dx][dy] - time));
					return -1;   // TODO fix this
				}
			}
			else{
				if(stationAvailable[idx][idy]<=time)
				{
					laser_Available[minIndex] = time + latency;
					stationAvailable[idx][idy] = time + latency;
				}
				else 
				{
					waitchannel++;
					waitingTimeStation[dx][dy] = (int) Math.max(waitingTimeStation[dx][dy] , (stationAvailable[idx][idy] - time));
					return -1;
				}
			}	
			
		}
		else
		{	
			if(!coherence){
				if(channel_Available[dx][dy]<=time)
				{
					laser_Available[minIndex] = laser_Available[minIndex] + latency;
					channel_Available[dx][dy] = time + latency;
				}
				else
				{
					
					waitchannel++;
					waitingTimeStation[dx][dy] = (int) Math.max(waitingTimeStation[dx][dy] , (channel_Available[dx][dy] - time));
					return -1;   // TODO fix this
				}
			}
			else{
				if(stationAvailable[idx][idy]<=time)
				{
					laser_Available[minIndex] = laser_Available[minIndex] + latency;
					stationAvailable[idx][idy] = time + latency;
				}	
				else 
				{
					waitchannel++;
					waitingTimeStation[dx][dy] = (int) Math.max(waitingTimeStation[dx][dy] , (stationAvailable[idx][idy] - time));
					return -1;
					
				}
			}
			
		}
		
		
		return laser_Available[minIndex];
	}
	
	
	private static long findTimeProbe(int i, int j) {
		int a = i/8; int b=j/8;
		int index = a*4+b; //p-cluster
		int pos = (((i)/2) *5) + ((j)/2); //position inside a p-cluster
		long time = GlobalClock.getCurrentTime();
		//System.out.println(i + " and " + j);
		
		if(stationAvailable[i/2][j/2] > time) //if station is busy go back
		{
			waitStation++;
			waitingTime[i/2][j/2] = Math.max((int) (stationAvailable[i/2][j/2] - time), waitingTime[i/2][j/2]);
			return -1;
		}
		
		numStationRequests[i/2][j/2]++;
		if(laserStatus[i/2][j/2]>0)
		{
			if(laserAvailable[pos] > time)
			{
				if(laserAvailable[pos]/reconfInterval > time/reconfInterval)//not in this epoch
				{
					waitingTime[i/2][j/2] = (int) reconfInterval;
					return -1;
				}
				laserAvailable[pos] += SystemConfig.nocConfig.latencyBetweenNOCElements;
			}
			else
				laserAvailable[pos] = time+SystemConfig.nocConfig.latencyBetweenNOCElements;
			stationAvailable[i/2][j/2] = laserAvailable[pos];
			return laserAvailable[pos];	
		}
		else
		{
			waitingTime[i/2][j/2] = (int) reconfInterval;
			return -1;
		}
		
		
	}

}



