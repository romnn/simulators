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
package traceSimplification;

import emulatorinterface.translator.x86.instruction.InstructionClass;
import emulatorinterface.translator.x86.instruction.InstructionClassTable;
import emulatorinterface.translator.x86.instruction.FullInstructionClass;

import emulatorinterface.translator.x86.objparser.ObjParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import config.SimulationConfig;
import config.XMLParser;
import emulatorinterface.translator.x86.instruction.InstructionClass;
import emulatorinterface.translator.x86.instruction.InstructionClassTable;
import emulatorinterface.translator.x86.objparser.ObjParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import config.SimulationConfig;
import config.XMLParser;

@SuppressWarnings("unused")
public class TraceSimplifier {
	
	public static int totalNumKernels;
	public static BufferedReader  inputBufferedReader;
	public static FileOutputStream fos[];
    public static DataOutputStream dos[];
	private static Hashtable<Integer, FullInstructionClass> kernelInstructionsTable[];
	public static int TYPE_BRANCH = 0;
	public static int TYPE_MEM = 1;
	public static int TYPE_IP = 2;
	public static int TYPE_BLOCK_END = -1;
	public static int TYPE_KERNEL_END = -2;
	public static int MEM_START=-3;
	public static long MEM_END=-4;
	public static long curBlock=1;
	public static long totalIns=0;
	
	@SuppressWarnings({ "unused", "unchecked"})
	public static void main(String arguments[])
	{
		/**
		 * 1. loop over each file for every kernel
		 * 2. read all the blocks and create a hashtable (ip,instruction) 
		 * 3. create a stream of ips of every block
		 * 4. write temporary files for ip stream and hashtable for every kernel
		 * 5. repeat for other kernels
		 * 
		 */
		System.out.println("Compressing the traces !!! ");
		long blockLength=0;
		String configFileName = arguments[0];
		XMLParser.parse(configFileName);
		totalNumKernels = Integer.parseInt(arguments[3]);
		String traceFileFolder = arguments[2] + "/" + SimulationConfig.MaxNumJavaThreads;
		fos=new FileOutputStream[totalNumKernels];
	    dos=new DataOutputStream[totalNumKernels];
		kernelInstructionsTable= new Hashtable[totalNumKernels];
		InstructionClassTable.createInstructionClassTable();
		InstructionClassTable.createRegisterTable();
		String inputLine=new String();
		for(int i=0;i<totalNumKernels;i++)
			kernelInstructionsTable[i] = new Hashtable<Integer, FullInstructionClass>();
		
		int curKernel=-1;
		
		try {
					
			for(int j=0;j<SimulationConfig.MaxNumJavaThreads;j++)
			{
				System.out.println("Conpressed for thread " + j);
				curKernel=-1;
				String inputFileName = traceFileFolder+"/"+j+".txt";
				File inputTraceFile = new File(inputFileName);
				if(j>0)
				{
				for(int i=0;i<totalNumKernels;i++)
				{
					dos[i].close();
					fos[i].close();
				}
				inputBufferedReader.close();
				}
				inputBufferedReader = new BufferedReader(new FileReader(inputTraceFile));
				

				for(int i=0;i<totalNumKernels;i++)
				{
					fos[i] = new FileOutputStream(new File(traceFileFolder+"/"+j+"_"+i+".txt"));
					dos[i] = new DataOutputStream(fos[i]);
				}
				while((inputLine=inputBufferedReader.readLine())!=null)
				{
					if(inputLine.startsWith("KERNEL START"))
					{
						curKernel++;
						dos[curKernel].writeInt(TYPE_KERNEL_END);
						dos[curKernel].writeInt(Integer.parseInt(inputLine.substring(13)));
						
						continue;
					}
					else if(inputLine.contains("&"))
					{
						
						StringTokenizer stringTokenizer = new StringTokenizer(inputLine,"&");						
						int type = Integer.parseInt(stringTokenizer.nextToken());
						int ip = Integer.parseInt(stringTokenizer.nextToken());
						String sCurrentLine = stringTokenizer.nextToken();
						String instructionPrefix = null;
						String operation;
						StringTokenizer lineTokenizer=new StringTokenizer(sCurrentLine,"\t,;{}");
						StringTokenizer temp=new StringTokenizer(sCurrentLine,"\t,;{}");
						if(sCurrentLine.startsWith("@"))
						{
							//contains prefix 
							instructionPrefix=lineTokenizer.nextToken();
							operation=lineTokenizer.nextToken();
							
						}
						else
						{
							//no prefix
							instructionPrefix=null;
							operation=lineTokenizer.nextToken();
							
						}
						
						ObjParser myParser = new ObjParser();
						
						boolean floatingOperation=myParser.checkFloatingOperation(operation);
						
						if(operation.startsWith("ld.shared") || operation.startsWith("ld.const") || 
								operation.startsWith("st.shared") || operation.startsWith("st.const")|| 
									operation.startsWith("ldu.shared") || operation.startsWith("ldu.const"))
						{
							operation = operation.substring(0,operation.indexOf(".", 4));
						}
						else if(operation.contains("."))
						{
							operation=operation.substring(0,operation.indexOf("."));
						}
						InstructionClass instructionClass;
						instructionClass = InstructionClassTable.getInstructionClass(operation,floatingOperation);
						FullInstructionClass fullinstructionclass;
						int NoofRegisters=0;
						while(lineTokenizer.hasMoreTokens())
						{String news=(lineTokenizer.nextToken());
						if(news.startsWith("%r") || news.startsWith("[%r"))
							NoofRegisters++;
						}
//						System.out.println(temp.countTokens());
						temp.nextToken();
						if(NoofRegisters!=0){
//						System.out.println("No of registers are not 0");
						int registers[]=new int[NoofRegisters];
						int i=0;
						while(temp.hasMoreTokens())
						{	String news=(temp.nextToken());
//						
						if(news.startsWith("%r") || news.startsWith("[%r"))
							{registers[i]=Integer.parseInt(news.replaceAll("[\\D]", ""));
//							System.out.println(news);
//							System.out.println(registers[i]);
							i++;
							}
						}
						fullinstructionclass=new FullInstructionClass(instructionClass, registers);
						
						}
						else
							fullinstructionclass=new FullInstructionClass(instructionClass, null);
//						System.out.println("No of registers are not 0");
						// Add registers parsing here for changing the instructionclass files 
						dos[curKernel].writeInt(ip);
						blockLength+=String.valueOf(ip).length();
						
						if(type == TYPE_MEM)
						{	
							if(operation.startsWith("ld.shared") || operation.startsWith("ld.const") || 
									operation.startsWith("st.shared") || operation.startsWith("st.const")|| 
										operation.startsWith("ldu.shared") || operation.startsWith("ldu.const"))
							{
								dos[curKernel].writeInt(MEM_START);						
								Long MemoryAddresses[] = new Long[SimulationConfig.ThreadsPerCTA];
								int bit =0;
								while(stringTokenizer.hasMoreTokens())		
								{
									MemoryAddresses[bit] = Long.parseLong(stringTokenizer.nextToken().trim());
//									System.out.println(MemoryAddresses[bit]);
									dos[curKernel].writeLong(MemoryAddresses[bit]);
									blockLength+=String.valueOf("&"+MemoryAddresses[bit]).length();
									bit++;
								}
								dos[curKernel].writeLong(MEM_END);
							}
						}						
						
						dos[curKernel].flush();
						blockLength+=1;		
				
						kernelInstructionsTable[curKernel].put(ip, fullinstructionclass);

						totalIns++;
					}
					else if(inputLine.equals("BLOCK END"))
					{
						curBlock++;
						
						dos[curKernel].writeInt(TYPE_BLOCK_END);
						blockLength+=10;
						blockLength=0;
					}

						
				}
				
			}
				for(int i=0;i<totalNumKernels;i++)
				{
					dos[i].close();
					fos[i].close();
				}	
				inputBufferedReader.close();					
				System.out.println("reached here");
		for(int i=0;i<totalNumKernels;i++)
		{
			FileOutputStream fos = new FileOutputStream(traceFileFolder+"/hashfile"+ "_" + i);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(kernelInstructionsTable[i]);
			System.out.println("serializing java object");
			oos.close();
		}
	
	
		}
		 catch (Exception e) {
			e.printStackTrace();
		}
		
				
	}
}
