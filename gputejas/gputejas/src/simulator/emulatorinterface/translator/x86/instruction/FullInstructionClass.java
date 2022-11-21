package emulatorinterface.translator.x86.instruction;
import java.io.*;
public class FullInstructionClass implements Serializable
{
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public InstructionClass instructionclass;
int registers[];
public FullInstructionClass(InstructionClass insclass, int regs[]) {
	this.instructionclass=insclass;
	this.registers=regs;
	// TODO Auto-generated constructor stub
}

}