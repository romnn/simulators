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

public class SmConfig {

	public static int NoOfWarpSchedulers;
	public static int NoOfSP;
	public static SpConfig[] sp;
	public static int WarpSize;
	public int IntRegFileSize;
	public static int frequency;
	public CacheConfig iCache = new CacheConfig();
	public CacheConfig dCache = new CacheConfig();
	public CacheConfig constantCache = new CacheConfig();
	public CacheConfig sharedCache = new CacheConfig();
	public RegConfig regconfig=new RegConfig();
	public static EnergyConfig decodePower;
	// NOT needed if each operation power not available 
	public static EnergyConfig intSpPower;
	public static EnergyConfig floatSpPower;
	public static EnergyConfig complexSpPower;
	public static EnergyConfig resultsBroadcastBusPower;
	///////////////////////////////////////////////////////////////////
	public static EnergyConfig AllocDeallocPower;
	public static EnergyConfig BankPower;
	public static EnergyConfig ArbiterPower;
	public static EnergyConfig CollectorUnitsDecodePower;
	public static EnergyConfig DispatchPower;
	public static EnergyConfig ScoreBoardPower;

}
