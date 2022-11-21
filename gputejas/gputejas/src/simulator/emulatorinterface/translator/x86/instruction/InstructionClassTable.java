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



import java.util.Hashtable;

public class InstructionClassTable {
	private static final Hashtable<String, InstructionClass> integerInstructionClassTable= new Hashtable<String, InstructionClass>(), floatInstructionClassTable= new Hashtable<String, InstructionClass>();
	private static final Hashtable<InstructionClass, Integer> RegisterTable= new Hashtable<InstructionClass, Integer>();
	private static final Hashtable<InstructionClass, PTXStaticInstructionHandler> instructionClassHandlerTable = new Hashtable<InstructionClass, PTXStaticInstructionHandler>();

	public static InstructionClass getInstructionClass(String operation, boolean floatingOperation) {
		if (operation == null)
			return InstructionClass.INVALID;
		
		InstructionClass instructionClass;
		if(floatingOperation)
		{
			instructionClass = floatInstructionClassTable.get(operation);
		}
			
		else
		{
			instructionClass = integerInstructionClassTable.get(operation);
		}

		if (instructionClass == null)
			return InstructionClass.INVALID;
		else
			return instructionClass;

	}
	
	public static void createRegisterTable()
	{ 
		RegisterTable.put(InstructionClass.INVALID,0);
		RegisterTable.put(InstructionClass.INTEGER_LOAD,0);
		RegisterTable.put(InstructionClass.INTEGER_STORE,0);
		RegisterTable.put(InstructionClass.INTEGER_LOAD_CONSTANT,0);
		RegisterTable.put(InstructionClass.INTEGER_STORE_CONSTANT,0);
		RegisterTable.put(InstructionClass.INTEGER_LOAD_SHARED,0);
		RegisterTable.put(InstructionClass.INTEGER_STORE_SHARED,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_LOAD,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_STORE,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_LOAD_CONSTANT,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_STORE_CONSTANT,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_LOAD_SHARED,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_STORE_SHARED,0);
		RegisterTable.put(InstructionClass.INTEGER_PREFETCH,0);
		RegisterTable.put(InstructionClass.INTEGER_QUERY_ADDRESS,0);
		RegisterTable.put(InstructionClass.INTEGER_ADDRESS_CONVERT,0);
		RegisterTable.put(InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_TWO_OPERANDS,0);
		RegisterTable.put(InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_THREE_OPERANDS,0);
		RegisterTable.put(InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_FOUR_OPERANDS,0);
		RegisterTable.put(InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_FIVE_OPERANDS,0);
		RegisterTable.put(InstructionClass.INTEGER_MULTIPLICATION_THREE_OPERANDS,0);
		RegisterTable.put(InstructionClass.INTEGER_MULTIPLICATION_FOUR_OPERANDS,0);
		RegisterTable.put(InstructionClass.INTEGER_DIVISION_THREE_OPERANDS,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_PREFETCH,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_ALU_TWO_OPERANDS,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_ALU_THREE_OPERANDS,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_ALU_FOUR_OPERANDS,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_MULTIPLICATION_THREE_OPERANDS,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_MULTIPLICATION_FOUR_OPERANDS,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_DIVISION_THREE_OPERANDS,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_DIVISION_TWO_OPERANDS,0);
		RegisterTable.put(InstructionClass.FLOATING_POINT_COMPLEX_OPERATION_TWO_OPERANDS,0);
		RegisterTable.put(InstructionClass.GUARD_PREDICATE,0);
		RegisterTable.put(InstructionClass.BRANCH,0);
		RegisterTable.put(InstructionClass.CALL,0);
		RegisterTable.put(InstructionClass.RETURN,0);
		RegisterTable.put(InstructionClass.EXIT,0);
		RegisterTable.put(InstructionClass.TYPE_BRANCH,0);
		RegisterTable.put(InstructionClass.TYPE_MEM,0);
		RegisterTable.put(InstructionClass.TYPE_IP,0);
		RegisterTable.put(InstructionClass.TYPE_BLOCK_END,0);
		RegisterTable.put(InstructionClass.TYPE_KERNEL_END,0);
		RegisterTable.put(InstructionClass.MEM_END,0);
		RegisterTable.put(InstructionClass.MEM_START,0);
		
	}
	public static void createInstructionClassHandlerTable() {
		// create an empty hash-table for storing object references.
	
		//TODO: ADD HANDLERS HERE
		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_LOAD,
				new IntegerLoad());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_STORE,
				new IntegerStore());
		
		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_LOAD_CONSTANT,
				new IntegerLoadConstant());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_STORE_CONSTANT,
				new IntegerStoreConstant());
		
		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_LOAD_SHARED,
				new IntegerLoadShared());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_STORE_SHARED,
				new IntegerStoreShared());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_PREFETCH,
				new IntegerPrefetch());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_QUERY_ADDRESS,
				new IntegerQueryAddress());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_ADDRESS_CONVERT,
				new IntegerAdressConvert());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_TWO_OPERANDS, 
				new IntegerAluNoImplicitDestinationTwoOperands());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_THREE_OPERANDS,
				new IntegerAluNoImplicitDestinationThreeOperands());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_FOUR_OPERANDS,
				new IntegerAluNoImplicitDestinationFourOperands());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_FIVE_OPERANDS,
				new IntegerAluNoImplicitDestinationFiveOperands());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_MULTIPLICATION_THREE_OPERANDS,
				new IntegerMultiplicationThreeOperands());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_MULTIPLICATION_FOUR_OPERANDS,
				new IntegerMultiplicationFourOperands());

		instructionClassHandlerTable.put(
				InstructionClass.INTEGER_DIVISION_THREE_OPERANDS, 
				new IntegerDivisionThreeOperands());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_LOAD, 
				new FloatingPointLoad());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_STORE, 
				new FloatingPointStore());
		
		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_LOAD_CONSTANT, 
				new FloatingPointLoadConstant());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_STORE_CONSTANT, 
				new FloatingPointStoreConstant());
		
		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_LOAD_SHARED, 
				new FloatingPointLoadShared());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_STORE_SHARED, 
				new FloatingPointStoreShared());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_PREFETCH, 
				new FloatingPointPrefetch());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_ALU_TWO_OPERANDS,
				new FloatingPointAluTwoOperands());
		
		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_ALU_THREE_OPERANDS,
				new FloatingPointAluThreeOperands());
		
		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_ALU_FOUR_OPERANDS,
				new FloatingPointAluFourOperands());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_MULTIPLICATION_THREE_OPERANDS,
				new FloatingPointMultiplicationThreeOperands());
		
		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_MULTIPLICATION_FOUR_OPERANDS,
				new FloatingPointMultiplicationFourOperands());
		
		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_DIVISION_TWO_OPERANDS,
				new FloatingPointDivisionTwoOperands());
		
		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_DIVISION_THREE_OPERANDS,
				new FloatingPointDivisionThreeOperands());

		instructionClassHandlerTable.put(
				InstructionClass.FLOATING_POINT_COMPLEX_OPERATION_TWO_OPERANDS, 
				new FloatingPointComplexOperationTwoOperands());
				
		instructionClassHandlerTable.put(
				InstructionClass.GUARD_PREDICATE,
				new GuardPredicate());
		
		instructionClassHandlerTable.put(
				InstructionClass.BRANCH,
				new Branch());
		
		instructionClassHandlerTable.put(
				InstructionClass.CALL,
				new Call());
		
		instructionClassHandlerTable.put(
				InstructionClass.RETURN,
				new Return());
		
		instructionClassHandlerTable.put(
				InstructionClass.EXIT,
				new Exit());
	}
public static void createInstructionClassTable() {
		createIntegerInstructionClassTable();
		createFloatInstructionClassTable();
		
	}	
	
	
	private static void createFloatInstructionClassTable() {
		String floatingPointAluTwoOperands[] = "testp|abs|neg|mov"
				.split("\\|");
		for (int i = 0; i < floatingPointAluTwoOperands.length; i++)
			floatInstructionClassTable.put(floatingPointAluTwoOperands[i],
					InstructionClass.FLOATING_POINT_ALU_TWO_OPERANDS);
		
		String floatingPointAluThreeOperands[] = "copysign|add|sub|min|max|set|setp"
				.split("\\|");
		for (int i = 0; i < floatingPointAluThreeOperands.length; i++)
			floatInstructionClassTable.put(floatingPointAluThreeOperands[i],
					InstructionClass.FLOATING_POINT_ALU_THREE_OPERANDS);
		
		String floatingPointAluFourOperands[] = "selp|slct"
				.split("\\|");
		for (int i = 0; i < floatingPointAluFourOperands.length; i++)
			floatInstructionClassTable.put(floatingPointAluFourOperands[i],
					InstructionClass.FLOATING_POINT_ALU_FOUR_OPERANDS);
		
		
		String floatingPointMultiplicationThreeOperands[] = "mul"
				.split("\\|");
		for (int i = 0; i < floatingPointMultiplicationThreeOperands.length; i++)
			floatInstructionClassTable.put(floatingPointMultiplicationThreeOperands[i],
					InstructionClass.FLOATING_POINT_MULTIPLICATION_THREE_OPERANDS);
		

		String floatingPointMultiplicationFourOperands[] = "fma|mad"
				.split("\\|");
		for (int i = 0; i < floatingPointMultiplicationFourOperands.length; i++)
			floatInstructionClassTable.put(floatingPointMultiplicationFourOperands[i],
					InstructionClass.FLOATING_POINT_MULTIPLICATION_FOUR_OPERANDS);
		
		String floatingPointDivisionThreeOperands[] = "div"
				.split("\\|");
		for (int i = 0; i < floatingPointDivisionThreeOperands.length; i++)
			floatInstructionClassTable.put(floatingPointDivisionThreeOperands[i],
					InstructionClass.FLOATING_POINT_DIVISION_THREE_OPERANDS);
		
		String floatingPointDivisionTwoOperands[] = "rcp"
				.split("\\|");
		for (int i = 0; i < floatingPointDivisionTwoOperands.length; i++)
			floatInstructionClassTable.put(floatingPointDivisionTwoOperands[i],
					InstructionClass.FLOATING_POINT_DIVISION_TWO_OPERANDS);
		
		String floatingPointComplexOperationTwoOperands[] = "sqrt|rsqrt|sin|cos|lg2|ex2"
				.split("\\|");
		for (int i = 0; i < floatingPointComplexOperationTwoOperands.length; i++)
			floatInstructionClassTable.put(floatingPointComplexOperationTwoOperands[i],
					InstructionClass.FLOATING_POINT_COMPLEX_OPERATION_TWO_OPERANDS);
		
		String floatingPointLoad[] = "ld|ldu"
				.split("\\|");
		for (int i = 0; i < floatingPointLoad.length; i++)
			floatInstructionClassTable.put(floatingPointLoad[i],
					InstructionClass.FLOATING_POINT_LOAD);
		
		String floatingPointLoadConstant[] = "ld.const|ldu.const"
				.split("\\|");
		for (int i = 0; i < floatingPointLoadConstant.length; i++)
			floatInstructionClassTable.put(floatingPointLoadConstant[i],
					InstructionClass.FLOATING_POINT_LOAD_CONSTANT);
		
		String floatingPointLoadShared[] = "ld.shared|ldu.shared"
				.split("\\|");
		for (int i = 0; i < floatingPointLoadShared.length; i++)
			floatInstructionClassTable.put(floatingPointLoadShared[i],
					InstructionClass.FLOATING_POINT_LOAD_SHARED);
		
		
		
		String integerAddressConvert[] = "cvta|cvt"
				.split("\\|");
		for (int i = 0; i < integerAddressConvert.length; i++)
			floatInstructionClassTable.put(integerAddressConvert[i],
					InstructionClass.INTEGER_ADDRESS_CONVERT);
		String floatingPointStore[] = "st"
				.split("\\|");
		for (int i = 0; i < floatingPointStore.length; i++)
			floatInstructionClassTable.put(floatingPointStore[i],
					InstructionClass.FLOATING_POINT_STORE);
		
		String floatingPointStoreConstant[] = "st.const"
				.split("\\|");
		for (int i = 0; i < floatingPointStoreConstant.length; i++)
			floatInstructionClassTable.put(floatingPointStoreConstant[i],
					InstructionClass.FLOATING_POINT_STORE_CONSTANT);
		
		String floatingPointStoreShared[] = "st.shared"
				.split("\\|");
		for (int i = 0; i < floatingPointStoreShared.length; i++)
			floatInstructionClassTable.put(floatingPointStoreShared[i],
					InstructionClass.FLOATING_POINT_STORE_SHARED);
		
		

String floatingPointPrefetch[] = "prefetch"
				.split("\\|");
		for (int i = 0; i < floatingPointPrefetch.length; i++)
			floatInstructionClassTable.put(floatingPointPrefetch[i],
					InstructionClass.FLOATING_POINT_PREFETCH);
		
		
	}
	private static void createIntegerInstructionClassTable() {
		
String guardPredicate[] = "@"
				.split("\\|");
		for (int i = 0; i < guardPredicate.length; i++)
			integerInstructionClassTable.put(guardPredicate[i],
					InstructionClass.GUARD_PREDICATE);


String branch[] = "bra"
				.split("\\|");
		for (int i = 0; i < branch.length; i++)
			integerInstructionClassTable.put(branch[i],
					InstructionClass.BRANCH);


String call[] = "call"
				.split("\\|");
		for (int i = 0; i < call.length; i++)
			integerInstructionClassTable.put(call[i],
					InstructionClass.CALL);


String ret[] = "ret"
				.split("\\|");
		for (int i = 0; i < ret.length; i++)
			integerInstructionClassTable.put(ret[i],
					InstructionClass.RETURN); //seep


String exit[] = "exit"
				.split("\\|");
		for (int i = 0; i < exit.length; i++)
			integerInstructionClassTable.put(exit[i],
					InstructionClass.EXIT);
		



String integerLoad[] = "ld|ldu"
				.split("\\|");
		for (int i = 0; i < integerLoad.length; i++)
			integerInstructionClassTable.put(integerLoad[i],
					InstructionClass.INTEGER_LOAD);
		
		String integerLoadConstant[] = "ld.const|ldu.const"
				.split("\\|");
		for (int i = 0; i < integerLoadConstant.length; i++)
			integerInstructionClassTable.put(integerLoadConstant[i],
					InstructionClass.INTEGER_LOAD_CONSTANT);
		
		String integerLoadShared[] = "ld.shared|ldu.shared"
				.split("\\|");
		for (int i = 0; i < integerLoadShared.length; i++)
			integerInstructionClassTable.put(integerLoadShared[i],
					InstructionClass.INTEGER_LOAD_SHARED);

		String integerQueryAddress[] = "isspacep"
				.split("\\|");
		for (int i = 0; i < integerQueryAddress.length; i++)
			integerInstructionClassTable.put(integerQueryAddress[i],
					InstructionClass.INTEGER_QUERY_ADDRESS);



		String integerAddressConvert[] = "cvta|cvt"
				.split("\\|");
		for (int i = 0; i < integerAddressConvert.length; i++)
			integerInstructionClassTable.put(integerAddressConvert[i],
					InstructionClass.INTEGER_ADDRESS_CONVERT);


		String integerPrefetch[] = "prefetch"
				.split("\\|");
		for (int i = 0; i < integerPrefetch.length; i++)
			integerInstructionClassTable.put(integerPrefetch[i],
					InstructionClass.INTEGER_PREFETCH);
		
		String integerStore[] = "st"
				.split("\\|");
		for (int i = 0; i < integerStore.length; i++)
			integerInstructionClassTable.put(integerStore[i],
					InstructionClass.INTEGER_STORE);
		
		String integerStoreConstant[] = "st.const"
				.split("\\|");
		for (int i = 0; i < integerStoreConstant.length; i++)
			integerInstructionClassTable.put(integerStoreConstant[i],
					InstructionClass.INTEGER_STORE_CONSTANT);
		
		String integerStoreShared[] = "st.shared"
				.split("\\|");
		for (int i = 0; i < integerStoreShared.length; i++)
			integerInstructionClassTable.put(integerStoreShared[i],
					InstructionClass.INTEGER_STORE_SHARED);
		
		
		String integerAluNoImplicitDestinationTwoOperands[] = "abs|neg|popc|clz|bfind|brev|not|cnot|mov"
				.split("\\|");
		for (int i = 0; i < integerAluNoImplicitDestinationTwoOperands.length; i++)
			integerInstructionClassTable.put(integerAluNoImplicitDestinationTwoOperands[i],
					InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_TWO_OPERANDS);
		
		String integerAluNoImplicitDestinationThreeOperands[] = "add|sub|min|max|addc|subc|set|setp|and|or|xor|shl|shr"
				.split("\\|");
		for (int i = 0; i < integerAluNoImplicitDestinationThreeOperands.length; i++)
			integerInstructionClassTable.put(integerAluNoImplicitDestinationThreeOperands[i],
					InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_THREE_OPERANDS);
		
		String integerAluNoImplicitDestinationFourOperands[] = "sad|bfe|selp|slct|shf|shfl|prmt"
				.split("\\|");
		for (int i = 0; i < integerAluNoImplicitDestinationFourOperands.length; i++)
			integerInstructionClassTable.put(integerAluNoImplicitDestinationFourOperands[i],
					InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_FOUR_OPERANDS);
		
		String integerAluNoImplicitDestinationFiveOperands[] = "bfi"
				.split("\\|");
		for (int i = 0; i < integerAluNoImplicitDestinationFiveOperands.length; i++)
			integerInstructionClassTable.put(integerAluNoImplicitDestinationFiveOperands[i],
					InstructionClass.INTEGER_ALU_NO_IMPLICIT_DESTINATION_FIVE_OPERANDS);
		
		
		String integerMultiplicationThreeOperands[] = "mul|mul24"
				.split("\\|");
		for (int i = 0; i < integerMultiplicationThreeOperands.length; i++)
			integerInstructionClassTable.put(integerMultiplicationThreeOperands[i],
					InstructionClass.INTEGER_MULTIPLICATION_THREE_OPERANDS);
		
		String integerMultiplicationFourOperands[] = "mad|mad24|madc"
				.split("\\|");
		for (int i = 0; i < integerMultiplicationFourOperands.length; i++)
			integerInstructionClassTable.put(integerMultiplicationFourOperands[i],
					InstructionClass.INTEGER_MULTIPLICATION_FOUR_OPERANDS);
		
		String integerDivisionThreeOperands[] = "div|rem"
				.split("\\|");
		for (int i = 0; i < integerDivisionThreeOperands.length; i++)
			integerInstructionClassTable.put(integerDivisionThreeOperands[i],
					InstructionClass.INTEGER_DIVISION_THREE_OPERANDS);
		
		
		
	}
	
	public static PTXStaticInstructionHandler getInstructionClassHandler(
			InstructionClass instructionClass) {

		if (instructionClass == InstructionClass.INVALID)
			return null;

		PTXStaticInstructionHandler handler;
		handler = instructionClassHandlerTable.get(instructionClass);

		return handler;
	}
	public static InstructionClass getInstructionClass(String operation,
			boolean floatingOperation, int[] registers) {
		// TODO Auto-generated method stub
		if (operation == null)
			return InstructionClass.INVALID;
		
		InstructionClass instructionClass;
		if(floatingOperation)
		{
			instructionClass = floatInstructionClassTable.get(operation);
		}
			
		else
		{
			instructionClass = integerInstructionClassTable.get(operation);
		}

		if (instructionClass == null)
			return InstructionClass.INVALID;
		else
			return instructionClass;
		}

	public static int getNofRegisters(InstructionClass instructionClass) {
		// TODO Auto-generated method stub
	
		return RegisterTable.get(instructionClass);
	}

}