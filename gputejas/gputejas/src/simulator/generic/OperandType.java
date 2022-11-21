package generic;
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
public enum OperandType 
{
	integerRegister,			//registers of int type signed or unsigned(.reg .s or .u)
	floatRegister,				//registers of float type(.reg .f)
	specialRegister,			//special registers(.sreg , predefined)
	predRegister,				//predicate type registers (.reg .pred)
	untypedRegister,			//untyped register (.reg .b)
	
	immediate,					//immediate operand
	parameter,					//function parameter
	
	sharedMemory,
	constantMemory,					//memory which take 0 clock cycles to access
	
	globalMemory,
	localMemory,						//these memory variables take >100 clock cycles to access
	
	inValid,					//invalid
	label,						//label in the case of branch etcw
	
	memory						//this is of the form [abc +[0-9]+]
}