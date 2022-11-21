package pipeline.Registerfiles;
import pipeline.*;
import pipeline.Registerfiles.Arbiter.*;
import generic.SM;
import java.lang.Math.*;
import config.SmConfig;
import java.util.*;
public class OpndColl { // operand collector based register file unit
// After the operand collector finishes the allocate reads then only we can drain in the event queue 
	// Have to change the current event queue execution to match this first
    // opndcoll_rfu_t data members
    boolean m_initialized;
 
    int m_num_collector_sets;
    //int m_num_collectors;
    int m_num_banks;
    int m_bank_warp_shift;
    int m_warp_size;
    Vector <collector_unit_t> m_cu;
    arbiter_t m_arbiter;
   Vector<input_port_t> m_in_ports;  
    Map<Integer, Vector<collector_unit_t>> m_cus;
    Vector<dispatch_unit_t> m_dispatch_units;
    SM sm;
	GPUExecutionEngine containingExecutionEngine;


    
	public OpndColl()
       {
          m_num_banks=0;
          sm=null;
          m_initialized=false;
          containingExecutionEngine=null;
       }
       void add_cu_set(int set_id, int num_cu, int num_dispatch)
       {
    	   // TODO may have to check this here
//        m_cus.get(set_id).reserve(num_cu); //this is necessary to stop pointers in m_cu from being invalid do to a resize;
        for (int i = 0; i < num_cu; i++) {
            m_cus.get(set_id).add(new collector_unit_t());
            m_cu.add(m_cus.get(set_id).lastElement());
        }
        // for now each collector set gets dedicated dispatch units.
        for (int i = 0; i < num_dispatch; i++) {
            m_dispatch_units.add(new dispatch_unit_t(m_cus.get(set_id)));
        }

       }
      
       void add_port( Vector<register_set>  input, Vector<register_set>  output, Vector<Integer> cu_sets)
       {
        m_in_ports.add(new input_port_t(input,output,cu_sets));

       }
       void init( int num_banks, SM sm, GPUExecutionEngine containExecutionEngine)
       {
    	this.containingExecutionEngine=containExecutionEngine;
        this.sm=sm;
        m_arbiter.init(m_cu.size(),num_banks);
        //for( int n=0; n<m_num_ports;n++ ) 
        //    m_dispatch_units[m_output[n]].init( m_num_collector_units[n] );
        m_num_banks = num_banks;
        m_bank_warp_shift = 0; 
        m_warp_size = SmConfig.WarpSize;
        m_bank_warp_shift = (int)(Math.log(m_warp_size+0.5) / Math.log(2.0)); // Check if base e or base 2
        assert( (m_bank_warp_shift == 5) || (m_warp_size != 32) );
     
        for( int j=0; j<m_cu.size(); j++) {
            m_cu.get(j).init(j,num_banks,m_bank_warp_shift,this);
        }
        m_initialized=true;
      }
       // TODO Check this
       // modifiers
       boolean writeback(Warp inst ) // might cause stall 
        {
//            assert(!inst.empty());
            List<Integer> regs = containingExecutionEngine.get_regs_written(inst);
            Iterator r=regs.iterator();
            int n=0;
           while(r.hasNext()) {
               int reg = (Integer) r.next();
               int bank = containingExecutionEngine.register_bank(reg,inst.warp_id(),m_num_banks,m_bank_warp_shift);
               if( m_arbiter.bank_idle(bank) ) {
                   m_arbiter.allocate_bank_for_write(bank,new op_t(inst,reg,m_num_banks,m_bank_warp_shift));
               } else {
                   return false;
               }
               for(int m=0;m<(int)regs.size();m++){
                if(SmConfig.gpgpu_clock_gated_reg_file!=0){
                    int active_count=0;
                    for(int i=0;i<SmConfig.WarpSize;i=i+SmConfig.n_regfile_gating_group){
                        for(int j=0;j<SmConfig.n_regfile_gating_group;j++){
                            if(inst.get_active_mask().get(i+j)){
                                active_count+=SmConfig.n_regfile_gating_group;
                                break;
                            }
                        }
                    }
                    containingExecutionEngine.incregfile_writes(active_count);
                }else{
                    containingExecutionEngine.incregfile_writes(SmConfig.WarpSize);//inst.active_count());
                }
        }
         return true;
         }

         return true;
        }
   void step()
       {
            dispatch_ready_cu();   
            allocate_reads();
            for( int p = 0 ; p < m_in_ports.size(); p++ ) 
                allocate_cu( p );
            process_banks();
       }
    
   private
   void process_banks()
       {
          m_arbiter.reset_alloction();
       }
    
       void dispatch_ready_cu()
       {
    	   // Write your own code here
        for( int p=0; p < m_dispatch_units.size(); ++p ) {
            dispatch_unit_t du = m_dispatch_units.get(p);
            collector_unit_t cu = du.find_ready();
            if( cu != null ) {
               for(int m=0;m<(cu.get_num_operands()-cu.get_num_regs());m++){
                   if(SmConfig.gpgpu_clock_gated_reg_file!=0){
                       int active_count=0;
                       for(int i=0;i<SmConfig.WarpSize;i=i+SmConfig.n_regfile_gating_group){
                           for(int j=0;j<SmConfig.n_regfile_gating_group;j++){
                               if(cu.get_active_mask().get(i+j)){
                                   active_count+=SmConfig.n_regfile_gating_group;
                                   break;
                               }
                           }
                       }
                       containingExecutionEngine.incnon_rf_operands(active_count);
                   }else{
                   containingExecutionEngine.incnon_rf_operands(SmConfig.WarpSize);//cu.get_active_count());
                   }
              }
               cu.dispatch();
         }
        }
       }
       void allocate_cu( int port_num )
       {
        input_port_t inp = m_in_ports.get(port_num);
        for (int i = 0; i < inp.m_in.size(); i++) {
            if( (inp.m_in.get(i)).has_ready() ) {
               //find a free cu 
               for (int j = 0; j < inp.m_cu_sets.size(); j++) {
                  Vector <collector_unit_t>  cu_set = m_cus.get(inp.m_cu_sets.get(j));
               boolean allocated = false;
                   for (int k = 0; k < cu_set.size(); k++) {
                       if(cu_set.get(k).is_free()) {
                          collector_unit_t cu = cu_set.get(k);
                          allocated = cu.allocate(inp.m_in.get(i),inp.m_out.get(i));
                          m_arbiter.add_read_requests(cu);
                          break;
                       }
                   }
                   if (allocated) break; //cu has been allocated, no need to search more.
               }
               break; // can only service a single input, if it failed it will fail for others.
            }
        }

       }
   @SuppressWarnings("unchecked")
   
void allocate_reads(){
	   // process read requests that do not have conflicts
	   Deque<op_t> allocated = m_arbiter.allocate_reads();
	   Map<Integer,op_t> read_ops = null;
	   Iterator r=((Vector<collector_unit_t>) read_ops).iterator();
	   while(r.hasNext()) {
	       op_t rr = (op_t) r.next();
	      int reg = rr.get_reg();
	      int wid = rr.get_wid();
	      int bank = containingExecutionEngine.register_bank(reg,wid,m_num_banks,m_bank_warp_shift);
	      m_arbiter.allocate_for_read(bank,rr);
	      read_ops.put(bank,rr);
	   }
	  
	   r=((Vector<collector_unit_t>) read_ops).iterator();
	   while(r.hasNext()) {
	      op_t op = (op_t) r.next();
	      int cu = op.get_oc_id();
	      int operand = op.get_operand();
	      m_cu.get(cu).collect_operand(operand);
	      if(SmConfig.gpgpu_clock_gated_reg_file!=0){
	    	  int active_count=0;
	    	  for(int i=0;i<SmConfig.WarpSize;i=i+SmConfig.n_regfile_gating_group){
	    		  for(int j=0;j<SmConfig.n_regfile_gating_group;j++){
	    			  if(op.get_active_mask().get(i+j)){
	    				  active_count+=SmConfig.n_regfile_gating_group;
	    				  break;
	    			  }
	    		  }
	    	  }
	    	  containingExecutionEngine.incregfile_reads(active_count);
	      }else{
	    	  containingExecutionEngine.incregfile_reads(SmConfig.WarpSize);//op.get_active_count());
	      }
	}
   }
       
}

