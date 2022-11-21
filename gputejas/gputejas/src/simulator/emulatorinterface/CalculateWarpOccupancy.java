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
package emulatorinterface;

import config.SmConfig;

public class CalculateWarpOccupancy {

	//seep
	static int warpOccupance[]=new int[SmConfig.WarpSize + 1]; //+1 for 0 size warp
	public static void calculateOccupancy(Boolean[] threadMask)
	{
		if(threadMask == null)
		{
			return;
		}
		//seep
		for(int warpNumber=0; warpNumber < (threadMask.length / SmConfig.WarpSize); warpNumber++)
		{
			int count=0;
			//seep
			for(int threadNumber=warpNumber*SmConfig.WarpSize; (threadNumber<warpNumber*SmConfig.WarpSize+SmConfig.WarpSize) && (threadNumber < threadMask.length); threadNumber++)
			{
				if(threadMask[threadNumber]!=null && threadMask[threadNumber] == true)
				{
					count++;
				}
			}
			warpOccupance[count]++; //here we are trying to capture the number of warps with occupance = count
		}
	}
	
	public static void display()
	{
		float total=0, warp[]=new float[8];		
		for(int i=0;i<8;i++)
		{
			warp[i]=0;			
			for(int j=4*i+1;j<=4*(i+1);j++)	
			{
				warp[i]+=warpOccupance[j];
				total+=warpOccupance[j];
			}			
		}
		System.out.println("**************WARP OCCUPANCE**************************");
		for(int i=0;i<8;i++)
		{
			//System.out.println("For warp ("+(4*i+1)+"-"+4*(i+1)+") :"+(warp[i]/total)*100);
			
			System.out.println((warp[i]/total)*100);
		}
		System.out.println("*****************************************************");
	}
}
