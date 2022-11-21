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
package emulatorinterface.translator.x86.instruction;

import java.util.ArrayList;

import emulatorinterface.communication.Packet;
import emulatorinterface.translator.InvalidInstructionException;
import emulatorinterface.translator.x86.instruction.PTXStaticInstructionHandler;
import generic.Instruction;

public class FloatingPointMultiplicationThreeOperands implements PTXStaticInstructionHandler{
	public void handle(long instructionPointer, 
			/*Operand operand1, Operand operand2, Operand operand3, Operand operand4, Operand operand5,*/
			ArrayList<Instruction> instructionArrayList,Packet p ) throws InvalidInstructionException
	{
		instructionArrayList.add(Instruction.getFloatingPointMultiplication(p.insClass.registers));
		
	}
}
