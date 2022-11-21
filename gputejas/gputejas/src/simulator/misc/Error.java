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

package misc;

import main.Main;
import emulatorinterface.translator.InvalidInstructionException;

public class Error 
{
	public static void showErrorAndExit(String message)
	{
		System.out.flush();
		System.err.flush();
		System.err.println(message);
		new Exception().printStackTrace();
		System.exit(1);
	}


	public static void invalidOperand(String operandString) throws InvalidInstructionException
	{
		String msg;
		
		msg=("\n\tInvalid operand string : " + operandString + ".");
		
		throw new InvalidInstructionException(msg, false);
	}

}