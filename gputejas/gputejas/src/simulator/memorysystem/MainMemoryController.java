package memorysystem;
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
import java.io.FileWriter;
import java.io.IOException;

import config.EnergyConfig;
import config.SystemConfig;
import dram.MainMemoryDRAMController;
import generic.EventQueue;
import generic.PortType;
import generic.SimulationElement;
import generic.Event;
import generic.RequestType;

public class MainMemoryController extends SimulationElement
{
	public int numberOfMemoryControllers;
	public int[] mainmemoryControllersLocations;
	long numAccesses;
		
	public MainMemoryController() {
			super(SystemConfig.mainMemPortType,	SystemConfig.mainMemoryAccessPorts,
					SystemConfig.mainMemoryPortOccupancy,SystemConfig.mainMemoryLatency,SystemConfig.mainMemoryFrequency
					);	
	}
	
		public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig power = new EnergyConfig(SystemConfig.mainMemoryControllerPower, numAccesses);
		power.printEnergyStats(outputFileWriter, componentName);
		return power;
	}
	
	public EnergyConfig calculateEnergy(FileWriter outputFileWriter) throws IOException
	{
		EnergyConfig power = new EnergyConfig(SystemConfig.mainMemoryControllerPower, numAccesses);
		return power;
	}
	public void handleEvent(EventQueue eventQ, Event event)
	{ 
		System.out.println("purana vaala memory controller");
		}
	

}
