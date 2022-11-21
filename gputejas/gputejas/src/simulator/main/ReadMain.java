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
import config.SimulationConfig;
import config.XMLParser;


public class ReadMain {
	public static  String traceFileFolder = " ";
	public static readThread [] readRunners;
	public static int bytesRead;
	public static int totalNumKernels;
	public static void main(String args[])
	{
		String configFileName = args[0];
		SimulationConfig.outputFileName = args[1];
		traceFileFolder = args[2];
		totalNumKernels=Integer.parseInt(args[3]);
		
		// Parse the command line arguments
		System.out.println("Reading the configuration file");
		XMLParser.parse(configFileName);
		long startTime, endTime;
		startTime = System.currentTimeMillis();
		readRunners = new readThread[SimulationConfig.MaxNumJavaThreads];
		for(int i =0 ; i <SimulationConfig.MaxNumJavaThreads; i++)
		{
			readRunners[i] = new readThread(i);
		}
		
		for(int javaTid=0;javaTid<SimulationConfig.MaxNumJavaThreads;javaTid++)
		{
			try {
				readRunners[javaTid].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		endTime = System.currentTimeMillis();
		long ins=0;
		for(int i =0 ; i <SimulationConfig.MaxNumJavaThreads; i++)
		{
			ins+=readRunners[i].ins;
		}
		System.out.println("Instructions :"+(float)(ins));
		System.out.println("Time :"+(float)(endTime-startTime)/1000.0);
		System.out.println("KIPS :"+(ins/(float)(endTime-startTime)));
		System.out.println("MBPS :"+(bytesRead/(float)(1000*(endTime-startTime))));
		System.out.println("\n\nSimulation completed !!");
	}

}
