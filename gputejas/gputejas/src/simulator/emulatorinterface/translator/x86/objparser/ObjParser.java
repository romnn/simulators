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
package emulatorinterface.translator.x86.objparser;

import java.util.ArrayList;
import java.util.StringTokenizer;


import emulatorinterface.EmulatorPacketList;
import emulatorinterface.communication.Packet;
import emulatorinterface.translator.x86.instruction.InstructionClassTable;
import emulatorinterface.translator.x86.instruction.PTXStaticInstructionHandler;
import generic.GenericCircularQueue;
import generic.Instruction;


public class ObjParser {
private  int riscifyInstruction(ArrayList<Instruction> instructionList,Packet p) {
try
	{
		PTXStaticInstructionHandler handler;
		int register_numbers[];
		handler = InstructionClassTable.getInstructionClassHandler(p.insClass.instructionclass);
		
		if(handler!=null)
		{
			handler.handle(p.ip,instructionList,p);
		}
		else
		{
		}

	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	
	
	return 0;
}

public  boolean checkFloatingOperation(String operation) {
	if(operation==null)
	{
		return false;
	}
	if(operation.contains("f32")||operation.contains("f64"))
		return true;
	return false;
}

@SuppressWarnings("unused")
private  String[] tokenizeObjDumpAssemblyCode(String sCurrentLine) {
	
	String instructionPrefix;
	String operation;
	
	instructionPrefix=operation=null;
	sCurrentLine=sCurrentLine.trim();
	StringTokenizer lineTokenizer=new StringTokenizer(sCurrentLine,"\t,;{}");
	
	if(sCurrentLine.startsWith("@"))
	{
		//contains prefix 
		instructionPrefix=lineTokenizer.nextToken();
		operation=lineTokenizer.nextToken();
		System.out.println(operation);
		
	}
	else
	{
		//no prefix
		instructionPrefix=null;
		operation=lineTokenizer.nextToken();
		
	}
	
	return new String[] {  instructionPrefix, operation};
}

	
	public void fuseInstruction(
			long startInstructionPointer,
			EmulatorPacketList arrayListPacket, GenericCircularQueue<Instruction> inputToPipeline)
	{
		
		Packet p = arrayListPacket.get(0);
		ArrayList<Instruction> insList=new ArrayList<Instruction>();
		riscifyInstruction(insList,p);
	while(!insList.isEmpty())
		{
			Instruction tmp =insList.remove(0);
			inputToPipeline.enqueue(tmp);
			
		}	
	}
	
}