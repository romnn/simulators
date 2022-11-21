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
package generic;

import java.io.Serializable;
// 
// TODO here we have to incorporate the registers
@SuppressWarnings("serial")
public class Instruction implements Serializable
{
	public OperationType type;	
	public Long MemoryAddresses[];
	public int registers[];
	private long ciscProgramCounter;
	public int blockID;
	
	public Instruction()
	{
		this.MemoryAddresses = null;
	}
	
	public void clear()
	{
		this.type = null;
		this.MemoryAddresses = null;
	}
	
	public Instruction(OperationType type, Long MemoryAddresses[])
	
	{
		this.type = type;
		this.MemoryAddresses = MemoryAddresses;
	}
	
	private void set(OperationType type)
	{
		this.type = type;
		this.MemoryAddresses = null;
	}
	
	private void set(OperationType type, Long MemoryAddresses[])
	{
		this.type = type;
		this.MemoryAddresses = MemoryAddresses;
	}
	
	//all properties of sourceInstruction is copied to the current instruction
	public void copy(Instruction sourceInstruction)
	{
		this.type=sourceInstruction.type;

		this.ciscProgramCounter = sourceInstruction.ciscProgramCounter;
		this.MemoryAddresses = sourceInstruction.MemoryAddresses;
	}
	
	public static Instruction getInvalidInstruction()
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.inValid);
		return ins;
	}
	
	public static Instruction getLoadInstruction(int [] regs,Long MemoryAddresses[])
	{
		
		Instruction ins = new Instruction();
		ins.set(OperationType.load, MemoryAddresses);
		ins.setRegisters(regs);
		return ins;
	}
	
	public static Instruction getConstantLoadInstruction(int [] regs,Long MemoryAddresses[])
	{
		
		Instruction ins = new Instruction();
		ins.set(OperationType.load_const, MemoryAddresses);
		ins.setRegisters(regs);
		return ins;
	}
	
	public static Instruction getSharedLoadInstruction(int [] regs,Long MemoryAddresses[])
	{
		
		Instruction ins = new Instruction();
		ins.set(OperationType.load_shared, MemoryAddresses);
		ins.setRegisters(regs);
		return ins;
	}
	
	
	public static Instruction getStoreInstruction(int [] regs,Long MemoryAddresses[])
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.store, MemoryAddresses);
		ins.setRegisters(regs);
		return ins;
		
	}
	
	public static Instruction getConstantStoreInstruction(int [] regs,Long MemoryAddresses[])
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.store_const, MemoryAddresses);
		ins.setRegisters(regs);
		return ins;
	}
	
	public static Instruction getSharedStoreInstruction(int [] regs,Long MemoryAddresses[])
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.store_shared, MemoryAddresses);
		ins.setRegisters(regs);
		return ins;
	}
	
	public static Instruction getAddressInstruction(int [] regs)
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.address);
		ins.setRegisters(regs);
		return ins;
	}
	
	public static Instruction getIntALUInstruction(int [] regs)
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.integerALU);
		ins.setRegisters(regs);
		return ins;
	}
	
	
	public static Instruction getIntegerMultiplicationInstruction(int [] regs)
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.integerMul);
		ins.setRegisters(regs);
		return ins;
	}
	
	public static Instruction getIntegerDivisionInstruction(int [] regs)
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.integerDiv);
		ins.setRegisters(regs);
		return ins;
	}

	public static Instruction getFloatingPointALU(int [] regs)
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.floatALU);
		ins.setRegisters(regs);
		return ins;
	}
	
	public static Instruction getFloatingPointMultiplication(int [] regs)
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.floatMul);
		ins.setRegisters(regs);
		return ins;
			}
	
	public static Instruction getFloatingPointDivision(int [] regs)
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.floatDiv);
		ins.setRegisters(regs);
		return ins;
	}
	
	public static Instruction getPredicateInstruction(int [] regs)
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.predicate);
		ins.setRegisters(regs);
		return ins;
	}
	
	public static Instruction getBranchInstruction()
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.branch);
		return ins;
	}

	public static Instruction getCallInstruction()
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.call);
		return ins;
	}
	
	public static Instruction getReturnInstruction()
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.Return);
		return ins;
	}
	
	public static Instruction getExitInstruction()
	{
		Instruction ins = new Instruction();
		ins.set(OperationType.exit);
		return ins;
	}

	
	public long getCISCProgramCounter()
	{
		return ciscProgramCounter;
	}
	
	public void setCISCProgramCounter(long programCounter) 
	{
		this.ciscProgramCounter = programCounter;
	}
	
	public OperationType getOperationType()
	{
		return type;
	}
	
	public void setOperationType(OperationType operationType)
	{
		this.type = operationType;
	}
	

	public int getBlockID() {
		return blockID;
	}
	
	public void setBlockID(int b) {
		blockID=b;
	}

	public Long[] getMemoryAddresses() {
		return MemoryAddresses;
	}

	public void setMemoryAddresses(Long[] MemoryAddresses) {
		this.MemoryAddresses = MemoryAddresses;
	}

	public void setRegisters(int regs[])
	{
		registers=regs;
			
	}
	/**
	 * strInstruction method returns the instruction information in a string.
	 * @return String describing the instruction
	 */
	public String toString()
	{
		return 
		(
			String.format("%-20s", "IP = " + Long.toHexString(ciscProgramCounter)) +
			String.format("%-20s", "Op = " + type) 
		);
	}

	

}