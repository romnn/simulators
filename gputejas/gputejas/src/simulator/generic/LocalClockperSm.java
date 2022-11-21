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
package generic;


import memorysystem.Cache;
import config.SystemConfig;
import config.TpcConfig;

public class LocalClockperSm {
	
	 long currentTime;
	static int stepSize, smId,tpcId;

	public LocalClockperSm(int id, int Tid) {
	
	
		smId = id;
		tpcId=Tid;
		currentTime = 0;
		stepSize = 1;
	}

	@SuppressWarnings("unused")
	public static void systemTimingSetUp(SM[][] cores)
	{   System.out.println("IN SystemTimingSEtup");
//		int[] time_periods = new int[SystemConfig.NoOfTPC*TpcConfig.NoOfSM];
//		int i = 0,j=0,k=0;
//		int seed = Integer.MAX_VALUE;
//		String cacheName;
//		Cache cache;
//		
//		
//		int[][] freq_list = new int[SystemConfig.NoOfTPC][TpcConfig.NoOfSM];
//		boolean flag = false;
//		for(i = 0; i < SystemConfig.NoOfTPC; i++)
//		{	for(j = 0; j < TpcConfig.NoOfSM; j++)
//			freq_list[i][j] = Math.round(cores[i][j].getFrequency()/100);
//		}
//		
//		boolean HCFFound = false;
//		int HCF = 1;
//		for(i = 1; ; i++)
//		{
//			flag = true;
//			for(j = 0; j < SystemConfig.NoOfTPC*TpcConfig.NoOfSM; j++)
//			{
//				if(freq_list[j]%i != 0)
//				{
//					flag = false;
//					break;
//				}
//				
//				if(freq_list[j] == i)
//				{
//					HCFFound = true;
//				}					
//			}
//			
//			if(flag == true)
//			{
//				HCF = i;
//			}
//			
//			if(HCFFound == true)
//				break;
//		}
//		
//		System.out.println("HCF = " + HCF);
//		
//		for(i = 0; i < SystemConfig.NoOfTPC*TpcConfig.NoOfSM; i++)
//		{
//			freq_list[i] = freq_list[i]/HCF;
//		}
//		
//		int LCM, cur = freq_list[0];
//		
//		while(true)
//		{
//			flag = true;
//			for(i = 0; i < SystemConfig.NoOfTPC*TpcConfig.NoOfSM ; i++)
//			{
//				if(cur%freq_list[i] != 0)
//				{
//					flag = false;
//					break;
//				}
//			}
//			if(flag == true)
//			{
//				LCM = cur;
//				break;
//			}
//			cur = cur + freq_list[0];
//		}
//		
//		System.out.println("LCM = " + LCM);
		
		//set step sizes of components
		int i,j;
		for(i = 0; i < SystemConfig.NoOfTPC; i++)
			for(j=0;j<TpcConfig.NoOfSM;j++)
		{
			cores[i][j].setStepSize(1);

			
		}
	}

	public long getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(long currenttime) {
		currentTime = currenttime;
	}
	
	public void incrementClock()
	{
		currentTime += stepSize;
	}

	public int getStepSize() {
		return this.stepSize;
	}

	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
	}
	
	}
