package misc;
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
import generic.Statistics;
import main.Main;

public class ShutDownHook extends Thread {
	
	public void run()
	{	{
			System.out.println("shut down");
			
			if(Main.statFileWritten == false)
			{
				Statistics.printAllStatistics(Main.getTraceFileFolder(), -1, -1);
			}
			Runtime.getRuntime().halt(0);
		}
	}

}
