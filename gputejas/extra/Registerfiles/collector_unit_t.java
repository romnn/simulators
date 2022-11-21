package pipeline.Registerfiles;
import java.util.*;
// This is one collector unit
import config.SmConfig;
public class collector_unit_t {
	  private static int MAX_REG_OPERANDS;
	private
      boolean m_free;
      int collector_id; // collector unit hw id
	  int m_warp_id;
	  warp_inst_t  m_warp;
	  register_set m_output_register; // pipeline register to issue to when ready
	  op_t m_src_op[];
	  BitSet m_not_ready; // MAXREGOPERANDS2
	  int m_num_banks;
	  int m_bank_warp_shift;
	  OpndColl m_rfu;
	  SmConfig config;
	  
public
	static int MAX_REG_OPERANDS2;
// constructors
  collector_unit_t()
  { 
     m_free = true;
     m_warp = null;
     m_output_register = null;
     m_src_op = new op_t[MAX_REG_OPERANDS2];
     m_not_ready=new BitSet(MAX_REG_OPERANDS2);
     m_not_ready.clear();
     m_warp_id = -1;
     m_num_banks = 0;
     m_bank_warp_shift = 0;
  }
//accessors
public boolean ready() 
{ 
   return (!m_free)&& m_not_ready.isEmpty()&&(m_output_register).has_free(); 
}

   public op_t[] get_operands()  { return m_src_op; }

  int get_warp_id()  { return m_warp_id; }
//  int get_active_count()  { return m_warp.active_count(); }
//   BitSet get_active_mask()  { return m_warp.get_active_mask(); }
  int get_id()  { return collector_id; } // returns CU hw id

  // modifiers
  void collect_operand( int op )
  {
     m_not_ready.clear(op);
  }
  int get_num_operands() {
	  return m_warp.get_num_operands();
  }
  int get_num_regs() {
	  return m_warp.get_num_regs();
  }
 boolean is_free(){return m_free;}
public void  init( int n,  int num_banks,  int log2_warp_size, OpndColl rfu ) 
{ 
	m_rfu=rfu;
	collector_id=n; 
	m_num_banks=num_banks;
	assert(m_warp==null); 
	// TODO 
	// Check what is the config here
	m_warp = new warp_inst_t();
	m_bank_warp_shift=log2_warp_size;
}

 public boolean allocate(register_set pipeline_reg_set,register_set output_reg_set ) 
  {
     assert(m_free);
     assert(m_not_ready.isEmpty());
     m_free = false;
     m_output_register = output_reg_set;
     warp_inst_t pipeline_reg = pipeline_reg_set.get_ready();
     if( (pipeline_reg!=null) && !((pipeline_reg).empty())) {
        m_warp_id = (pipeline_reg).warp_id();
        for( int op=0; op < MAX_REG_OPERANDS; op++ ) {
        	// TODO
           int reg_num = (pipeline_reg).src[op]; // this math needs to match that used in function_info::ptx_decode_inst
           if( reg_num >= 0 ) { // valid register
              m_src_op[op] = new op_t( this, op, reg_num, m_num_banks, m_bank_warp_shift );
              m_not_ready.set(op);
           } else 
              m_src_op[op] = new op_t();
        }
        //move_warp(m_warp,pipeline_reg);
//        pipeline_reg_set.move_out_to(m_warp);
// TODO
        // Mark warp for Event Queue
        return true;
     }
     return false;
  }
  public void dispatch()
  {
     assert( m_not_ready.isEmpty() );
     //move_warp(m_output_register,m_warp);
//     m_output_register.move_in(m_warp);
     m_free=true;
     m_output_register = null;
     for( int i=0; i<MAX_REG_OPERANDS2;i++)
        m_src_op[i].reset();
  }

 }
