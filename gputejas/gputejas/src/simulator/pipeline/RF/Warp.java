package pipeline.RF;
import generic.Instruction;

import java.util.*;
public class Warp
{
int warp_id;
Instruction ins;
public int in[];
public int out;
public int src[];
public int dst;
public Warp(Instruction newInstruction,int id) {
	// TODO Auto-generated constructor stub
	warp_id=id;
	ins= newInstruction;
	if(newInstruction.registers!=null)
	{ if(newInstruction.registers.length>1)
		in=src=new int[newInstruction.registers.length];
		dst=newInstruction.registers[0];
		out=dst;
		
		
	}
}
public int get_num_regs() {
	// TODO Auto-generated method stub
	if(in!=null)
	return in.length;
	else
		return 0;
}

public int warp_id() {
	// TODO Auto-generated method stub
	return warp_id;
}
public int get_num_write_regs() {
	// TODO Auto-generated method stub
	return 1;
}
   
}