package pipeline.RF;
import java.util.BitSet;

import pipeline.*;
import generic.SM;
// Have to track each operand through before deciding to put in the event queue
public class op_t {
	private
	SM sm;
	GPUExecutionEngine containingExecutionEngine;
   boolean m_valid;
   Collector  m_cu; 
    Warp m_warp;
   int  m_operand; // operand offset in instruction. e.g., add r1,r2,r3; r2 is oprd 0, r3 is 1 (r1 is dst)
   int  m_register;
   int  m_bank;
public

   op_t(Collector cu, int op, int reg, int num_banks, int bank_warp_shift )
   {
      m_valid = true;
      m_warp=null;
      m_cu = cu;
      m_operand = op;
      m_register = reg;
      containingExecutionEngine=cu.opc.containingExecutionEngine;
      m_bank = containingExecutionEngine.register_bank(reg,cu.get_warp_id(),num_banks,bank_warp_shift);
   }
   op_t(Warp warp, int reg, int num_banks, int bank_warp_shift, OperandCollector opc )
   {
	  this.containingExecutionEngine=opc.containingExecutionEngine;
	  m_valid=true;
      m_warp=warp;
      m_register=reg;
      m_cu=null;
      m_operand = -1;
      m_bank = containingExecutionEngine.register_bank(reg,warp.warp_id(),num_banks,bank_warp_shift);
   }
   
   // accessors
   public boolean valid()  { return m_valid; }
   int get_reg() 
   {
      assert( m_valid );
      return m_register;
   }
   int get_wid() 
   {   // TODO abort 
       if( m_warp != null ) return m_warp.warp_id();
       else if( m_cu != null ) return m_cu.get_warp_id();
       else return 0;

   }
   public int get_oc_id()  { return m_cu.get_id(); }
   public int get_bank()  { return m_bank; }
   int get_operand()  { return m_operand; }
   void reset() { m_valid = false; }

};