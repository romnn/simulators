package pipeline.RF;
import generic.Instruction;

import java.util.*;
import pipeline.GPUExecutionEngine;
import config.RegConfig;
// This is one collector unit
import config.SmConfig;
public class Collector {
private
boolean free;
int collector_id; // collector unit hw id
Warp  warp;
int warp_id;
BitSet m_not_ready; 
int m_num_banks;
OperandCollector opc;
public Collector(OperandCollector OpndColl)
{
	this.opc=OpndColl;
	free=true;

}
public Collector(OperandCollector OpndColl,Warp inst)
{
warp=inst;
warp_id=inst.warp_id;
m_not_ready=new BitSet(warp.get_num_regs());
this.opc=OpndColl;
free=false;
}
public void init (Warp inst)
{
warp=inst;
warp_id=inst.warp_id;
m_not_ready=new BitSet(warp.get_num_regs());
free=false;
}
public boolean ready() 
{ 
   if(m_not_ready!=null)
 
	   { 
//	   System.out.println("In collector ready" + m_not_ready);
	   return (!free)&& m_not_ready.isEmpty(); 
	   }
   
   else
	   return true;
}
int get_warp_id()  { return warp_id; }
int get_id()  { return collector_id; } // returns CU hw id


void collect_operand( int op )
{
//   System.out.println("cleared bit"+op);
	m_not_ready.clear(op);
}
int get_num_regs() {
	  return warp.get_num_regs();
}
boolean is_free(){return free;}
public void  init( int n,  int num_banks, OperandCollector rfu ) 
{ 
	opc=rfu;
	collector_id=n; 
	m_num_banks=num_banks;
	assert(warp==null); 

}

public boolean allocate(Warp inst) 
{
//	System.out.println("Warp id is here "+inst.warp_id);
	init(inst);
   assert(free);
   assert(m_not_ready.isEmpty());
   free = false;
  for( int op=0; op < warp.get_num_regs(); op++ ) 
        m_not_ready.set(op);
      return true;
}

public void dispatch()
{
   assert( m_not_ready.isEmpty() );
//   System.out.println("in dispatch here");
   if(warp!=null)
   {	
//	   System.out.println("warp enqueued here");
	   this.opc.containingExecutionEngine.getScheduleUnit().completeWarpPipeline.enqueue(warp.ins);
   this.opc.containingExecutionEngine.Warp_write.enqueue(warp);
   this.opc.containingExecutionEngine.m_scoreboard.reserveRegisters(warp);
  free=true;  }
}
public op_t[] get_operands() {
	if(warp!=null)
		if(warp.get_num_regs()>0)
			{
			
			op_t op[]=new op_t[warp.in.length];
			for(int i=0;i<warp.get_num_regs();i++)
				op[i]=new op_t(this,i,warp.in[i],RegConfig.num_banks,0);
			return op;
			}
	
	return null;
}

}


