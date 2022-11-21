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
import java.io.BufferedInputStream;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.*;
import misc.Error;
import misc.ShutDownHook;
import config.SimulationConfig;
//import jsr166y.Phaser;
import config.TpcConfig;
import config.XMLParser;


import emulatorinterface.DramRunnableThread;
import emulatorinterface.KernelState;

import emulatorinterface.SimplerRunnableThread;
import emulatorinterface.communication.filePacket.SimplerFilePacket;
import emulatorinterface.translator.x86.instruction.InstructionClass;
import emulatorinterface.translator.x86.instruction.FullInstructionClass;

import emulatorinterface.translator.x86.instruction.InstructionClassTable;
import generic.Statistics;


public class Main {
	public final static int SynchClockDomainsCycles = 1000;

	// the reader threads. Each thread reads from EMUTHREADS
	public static SimplerRunnableThread [] runners;
	public static DramRunnableThread dram;
	public static boolean allfinished=false;
	public static SimplerFilePacket ipcBase[];
	public static volatile boolean statFileWritten = false;
	public static int [] totalBlocks;
	
	public static  String traceFileFolder = " ";
	
	public static String getTraceFileFolder()
	{
		return traceFileFolder;
	}
	public static int totalNumKernels;
	
	
	
	@SuppressWarnings("unused")
	public static void main(String[] arguments)
	{
		
		System.out.println("\n\nPerforming the initial setup !!!");
	
		//register shut down hook
		Runtime.getRuntime().addShutdownHook(new ShutDownHook());
		
		checkCommandLineArguments(arguments);
		
		// Read the command line arguments
		String configFileName = arguments[0];
		SimulationConfig.outputFileName = arguments[1];
		
		totalNumKernels = Integer.parseInt(arguments[3]);
		
		// Parse the command line arguments
		System.out.println("Reading the configuration file");
		XMLParser.parse(configFileName);
		totalBlocks = new int[totalNumKernels];
		
		
		traceFileFolder = arguments[2] + "/" + SimulationConfig.MaxNumJavaThreads; 
		initializeBlocksPerKernel();
		initializeKernelHashfiles();
	
		final Phaser kernelEnd=new Phaser(SimulationConfig.MaxNumJavaThreads) {
			protected boolean onAdvance(int phase, int registeredParties) {
				System.err.println("In onadvance "+phase+ "with registered parties :"+registeredParties);

				for(int i=0;i<SimulationConfig.MaxNumJavaThreads;i++)
				{
					try {
						runners[i].initialize();
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
			     }
		};
		
		
		final Phaser epochEnd=new Phaser(SimulationConfig.MaxNumJavaThreads) {
			protected boolean onAdvance(int phase, int registeredParties) {
				for(int i=0;i<SimulationConfig.MaxNumJavaThreads;i++)
				{
					try {
						runners[i].epochCount=0;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return false;
			     }
		};

		runners = new SimplerRunnableThread[SimulationConfig.MaxNumJavaThreads];

		initializeArchitecturalComponents();
				
		long startTime, endTime;
		
		
		ipcBase=new SimplerFilePacket[SimulationConfig.MaxNumJavaThreads];
		String name;

		
	
		InstructionClassTable.createInstructionClassHandlerTable();
		InstructionClassTable.createRegisterTable();
		for (int i=0; i<SimulationConfig.MaxNumJavaThreads; i++) {
			
			
		name = Integer.toString(i);
		ipcBase[i]=new SimplerFilePacket(i);
		int tpc_id=i/TpcConfig.NoOfSM;
		int sm_id=i%TpcConfig.NoOfSM;
		System.out.println(tpc_id+"TPC id"+"java thread"+i);
		System.out.println(sm_id+"SM id"+"java thread"+i);

		runners[i] = new SimplerRunnableThread(name,i, ipcBase[i], ArchitecturalComponent.getCores()[tpc_id][sm_id], epochEnd);
		
		}

		System.out.println("\n\nRunning the simulation !!!");
		startTime = System.currentTimeMillis();
		dram=new DramRunnableThread("dram");
		dram.t.start();
		startRunnerThreads();
        Statistics.initStatistics();
		waitForJavaThreads();
		
		endTime = System.currentTimeMillis();
        allfinished=true;      
		Statistics.printAllStatistics(traceFileFolder,startTime, endTime);
		int TotalblocksExecuted=0, TotalblocksGiven=0;
		for(int i=0;i<SimulationConfig.MaxNumJavaThreads;i++){
			TotalblocksExecuted+=runners[i].blocksExecuted;
//			System.out.println("NO. of blocks completed are for thread "+i+"blocks executed are"+runners[i].blocksExecuted);
		}
		statFileWritten = true;
		System.out.println("\n\nSimulation completed !!");
		
		System.exit(0);
	}
	
	public static Hashtable<Long, FullInstructionClass> kernelInstructionsTables[];
	@SuppressWarnings("unchecked")
	private static void initializeKernelHashfiles() {
		kernelInstructionsTables=new Hashtable[totalNumKernels];
		for(int i=0;i<totalNumKernels;i++){
			FileInputStream fos=null;;
			ObjectInputStream is=null;
			try {
				fos = new FileInputStream(Main.traceFileFolder+"/hashfile"+ "_" +i);
				is = new ObjectInputStream(fos);
				kernelInstructionsTables[i] = new Hashtable<Long, FullInstructionClass>();
				try {
					Hashtable<Long, FullInstructionClass> readObject = (Hashtable<Long,FullInstructionClass >)is.readObject();
					kernelInstructionsTables[i].putAll(readObject);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
					
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			finally{
				try {
					is.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
		}
		
	}


	public static void waitForJavaThreads() {
		
		try {		
			
			for (int i=0; i<SimulationConfig.MaxNumJavaThreads; i++) {
				ipcBase[i].free.acquire();	
			}
			
			
		} catch (InterruptedException ioe) {
			misc.Error.showErrorAndExit("Wait for java threads interrupted !!");
		}
	}


	private static void startRunnerThreads() {
		for(int i=0;i<SimulationConfig.MaxNumJavaThreads;i++)
			runners[i].t.start();
		System.out.println("runners started for thread");
	//	runners[0].t.start();
	}
	
	public static KernelState currKernel = new KernelState();
	
	public static void initializeBlocksPerKernel() {
		
		FileInputStream fis;
		DataInputStream dis;
		for(int i =0 ; i < totalNumKernels ; i++)
		{
			try {
				File inputTraceFile = new File(Main.getTraceFileFolder()+"/"+0+"_"+i+".txt");
				fis=new FileInputStream(inputTraceFile);
				dis=new DataInputStream(new BufferedInputStream(fis, 64*1024));
			
		
				dis.readInt();
				totalBlocks[i]=dis.readInt();
				dis.close();
				fis.close();
			} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		}
	}

	
	public static void initializeArchitecturalComponents() {
		ArchitecturalComponent.createChip();
	}

	private static void checkCommandLineArguments(String arguments[]) {
		if (arguments.length !=4) {
			Error.showErrorAndExit("\n\tIllegal number of arguments !!\n" +
					"Usage java main <config-file> <output-file> <trace-file-folder> <num-of-kernels>");
		}
	}
	/**
	 * @author Moksh
	 * For real-time printing of the running time, when program exited on request
	 */
	public static void printSimulationTime(long time)
	{
		long seconds = time/1000;
		long minutes = seconds/60;
		seconds = seconds%60;
			System.out.println("\n");
			System.out.println("[Simulator Time]\n");
			
			System.out.println("Time Taken\t=\t" + minutes + " : " + seconds + " minutes");
			System.out.println("\n");
	}
	
	
	
	}

