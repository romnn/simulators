package pipeline.Registerfiles;
import java.util.BitSet;

import pipeline.*;
import generic.SM;
// Have to track each operand through before deciding to put in the event queue
public class op_t {
	private
	SM sm;
	GPUExecutionEngine containingExecutionEngine;
   boolean m_valid;
   collector_unit_t  m_cu; 
    warp_inst_t m_warp;
   int  m_operand; // operand offset in instruction. e.g., add r1,r2,r3; r2 is oprd 0, r3 is 1 (r1 is dst)
   int  m_register;
   int  m_bank;
public

   op_t() { m_valid = false; }
   op_t(collector_unit_t cu, int op, int reg, int num_banks, int bank_warp_shift )
   {
      m_valid = true;
      m_warp=null;
      m_cu = cu;
      m_operand = op;
      m_register = reg;
      m_bank = containingExecutionEngine.register_bank(reg,cu.get_warp_id(),num_banks,bank_warp_shift);
   }
   op_t(warp_inst_t warp, int reg, int num_banks, int bank_warp_shift )
   {
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

int get_active_count() 
   {	// TODO abort 
       if( m_warp != null ) return m_warp.active_count();
       else if( m_cu != null ) return m_cu.get_active_count();
       else return 0;
   }
    BitSet get_active_mask()
   {
       if( m_warp != null ) return m_warp.get_active_mask();
       else if( m_cu != null ) return m_cu.get_active_mask();
       else return null;
   }
   public int get_oc_id()  { return m_cu.get_id(); }
   public int get_bank()  { return m_bank; }
   int get_operand()  { return m_operand; }
   void reset() { m_valid = false; }

};