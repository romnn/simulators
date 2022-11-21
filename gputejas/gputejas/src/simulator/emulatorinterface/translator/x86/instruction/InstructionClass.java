package emulatorinterface.translator.x86.instruction;

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

public enum InstructionClass {
INVALID,


INTEGER_LOAD,
INTEGER_STORE,
INTEGER_LOAD_CONSTANT,
INTEGER_STORE_CONSTANT,
INTEGER_LOAD_SHARED,
INTEGER_STORE_SHARED,
FLOATING_POINT_LOAD,
FLOATING_POINT_STORE, 
FLOATING_POINT_LOAD_CONSTANT,
FLOATING_POINT_STORE_CONSTANT, 
FLOATING_POINT_LOAD_SHARED,
FLOATING_POINT_STORE_SHARED, 

INTEGER_PREFETCH,
INTEGER_QUERY_ADDRESS,
INTEGER_ADDRESS_CONVERT,
INTEGER_ALU_NO_IMPLICIT_DESTINATION_TWO_OPERANDS,
INTEGER_ALU_NO_IMPLICIT_DESTINATION_THREE_OPERANDS,
INTEGER_ALU_NO_IMPLICIT_DESTINATION_FOUR_OPERANDS,
INTEGER_ALU_NO_IMPLICIT_DESTINATION_FIVE_OPERANDS,
INTEGER_MULTIPLICATION_THREE_OPERANDS,
INTEGER_MULTIPLICATION_FOUR_OPERANDS,
INTEGER_DIVISION_THREE_OPERANDS,

FLOATING_POINT_PREFETCH,
FLOATING_POINT_ALU_TWO_OPERANDS,
FLOATING_POINT_ALU_THREE_OPERANDS,
FLOATING_POINT_ALU_FOUR_OPERANDS,
FLOATING_POINT_MULTIPLICATION_THREE_OPERANDS,
FLOATING_POINT_MULTIPLICATION_FOUR_OPERANDS,
FLOATING_POINT_DIVISION_THREE_OPERANDS,
FLOATING_POINT_DIVISION_TWO_OPERANDS,
FLOATING_POINT_COMPLEX_OPERATION_TWO_OPERANDS,  


GUARD_PREDICATE,
BRANCH,
CALL,
RETURN,
EXIT,
TYPE_BRANCH,
 TYPE_MEM,
 TYPE_IP,
 TYPE_BLOCK_END,
 TYPE_KERNEL_END,
 MEM_START,
 MEM_END
}
