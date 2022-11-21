package pipeline.RF;
import pipeline.RF.Arbiter.*;
import pipeline.*;
import generic.SM;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math.*;

import config.EnergyConfig;
import config.SmConfig;
import config.RegConfig;
import java.util.*;
public class OperandCollector { 
// operand collector based register file unit
// After the operand collector finishes the allocate reads then only we can drain in the event queue 
// Have to change the current event queue execution to match this first
// opndcoll_rfu_t data members
	GPUpipeline gpupipeline;
    boolean m_initialized;
    int m_num_collector_sets;
    int m_num_dispatch_units;
    int m_num_banks;
    int m_bank_warp_shift;
    int m_warp_size;
    Vector <Collector> m_cu;
    arbiter_t m_arbiter;
    Map<Integer, Vector<Collector>> m_cus;
    Vector<Dispatch> m_dispatch_units;
    SM sm;
	GPUExecutionEngine containingExecutionEngine;
	int AllocDeallocAccesses;
	int BankAccesses;
	int DispatchUnitAccesses;
	int ScoreboardAccesses;
	int CollectorAccesses;
	int ArbiterAccesses;

    
	public OperandCollector()
       {
          m_num_banks=RegConfig.num_banks;
          sm=null;
          m_initialized=false;
          containingExecutionEngine=null;
          m_num_collector_sets=RegConfig.num_collector_units;
          m_num_dispatch_units=RegConfig.num_dispatch_units;
      	m_dispatch_units=new Vector<Dispatch>(RegConfig.num_dispatch_units);
    	m_cu=new Vector<Collector>(m_num_collector_sets);
       	for(int i=0;i<m_num_collector_sets;i++)
    			m_cu.add(i, new Collector(this));
		for(int i=0;i<m_num_dispatch_units;i++)
			m_dispatch_units.add(i, new Dispatch(m_cu));
		m_arbiter=new arbiter_t();
	     AllocDeallocAccesses=0;
        BankAccesses=0;
        DispatchUnitAccesses=0;
        ScoreboardAccesses=0;
        CollectorAccesses=0;
        ArbiterAccesses=0;
       }
       /// more than one collector sets possible due to sp, sfu, here only one taken for ease
       void add_cu_set(int set_id, int num_cu, int num_dispatch)
       {
    	   m_cus.put(set_id,new Vector<Collector>(num_cu));
           for (int i = 0; i < num_cu; i++) {
            m_cus.get(set_id).add(new Collector(this));
            m_cu.add(m_cus.get(set_id).lastElement());
        }
        // for now each collector set gets dedicated dispatch units.
        for (int i = 0; i < num_dispatch; i++) {
            m_dispatch_units.add(new Dispatch(m_cus.get(set_id)));
        }
        
   
       }
      
       public void init( int num_banks, SM sm, GPUExecutionEngine containExecutionEngine)
       {
    	this.containingExecutionEngine=containExecutionEngine;
        this.sm=sm;
        m_arbiter.init(m_cu.size(),num_banks);
        m_bank_warp_shift = 0; 
        m_warp_size = SmConfig.WarpSize;  
        for( int j=0; j<m_cu.size(); j++) {
            m_cu.get(j).init(j,num_banks,this);
        }
        m_initialized=true;
      }
       // TODO Check this
       // modifiers
       
       public boolean writeback(Warp inst ) // might cause stall 
        {
//            assert(!inst.empty());
            List<Integer> regs = containingExecutionEngine.get_regs_written(inst);
            Iterator r=regs.iterator();
            int n=0;
           while(r.hasNext()) {
               int reg = (Integer) r.next();
               int bank = containingExecutionEngine.register_bank(reg,inst.warp_id(),m_num_banks,m_bank_warp_shift);
               if( m_arbiter.bank_idle(bank) ) {
            	   if(m_arbiter==null)
            		   System.out.println("arbiter is null");
                   m_arbiter.allocate_bank_for_write(bank,new op_t(inst,reg,m_num_banks,m_bank_warp_shift,this));
               } else {
                   return false;
               }
          containingExecutionEngine.incregfile_writes(SmConfig.WarpSize);//inst.active_count());
         return true;
         }

         return true;
        }
   public void step(GPUpipeline pipeline)
       {
//            this.gpupipeline=pipeline;
//	   System.out.println("In the operand collector");
	   		dispatch_ready_cu();   
	        allocate_cu();
            allocate_reads();       
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
            Dispatch du = m_dispatch_units.get(p);
            Collector cu = du.find_ready();
            if(cu!=null)
            	cu.dispatch();
//            else
//            	System.out.println("Not dispatched");
       }
        
       }
       void allocate_cu()
       {
         Vector <Collector>  cu_set = m_cu;
         boolean allocated = false;
//         System.out.println("collector sets size is"+cu_set.size());
	       for (int k = 0; k < cu_set.size(); k++) {
	           if(cu_set.get(k).is_free()) {
//	        	   System.out.println("found collector free");
	              Collector cu = cu_set.get(k);
	              allocated = cu.allocate(containingExecutionEngine.WarpTable.pollFirst());
	              m_arbiter.add_read_requests(cu);
	              break;
	           }
	       }
//	       if(allocated==false)
//           System.out.println("Has been allocated"+allocated);      //cu has been allocated, no need to search more.
      }
   @SuppressWarnings("unchecked")
   
void allocate_reads(){
	   // process read requests that do not have conflicts
	   Deque<op_t> allocated = m_arbiter.allocate_reads();
//	   System.out.println(allocated.size() + "size of allocated reads" );
	   Iterator r=allocated.iterator();
	   while(r.hasNext()) {
	       op_t rr = (op_t) r.next();
	      if(rr.m_operand!=-1)
	      rr.m_cu.collect_operand(rr.m_operand);
	      m_arbiter.allocate_for_read(rr.m_bank,rr);
	   }
	  containingExecutionEngine.incregfile_reads(SmConfig.WarpSize);//op.get_active_count());
   }
       
	public EnergyConfig calculateAndPrintEnergy(FileWriter outputFileWriter, String componentName) throws IOException
	{
		EnergyConfig totalPower = new EnergyConfig(0, 0);
		EnergyConfig AllocDeallocPower = new EnergyConfig(sm.getAllocPower(), AllocDeallocAccesses);
		totalPower.add(totalPower,AllocDeallocPower);
		EnergyConfig BankPower = new EnergyConfig(sm.getBankPower(), BankAccesses);
		totalPower.add(totalPower, BankPower);
		EnergyConfig ArbiterPower = new EnergyConfig(sm.getArbiterPower(),ArbiterAccesses);
		totalPower.add(totalPower, ArbiterPower);
		EnergyConfig CollectorUnitsDecodePower = new EnergyConfig(sm.getCollectorPower(), CollectorAccesses);
		totalPower.add(totalPower, CollectorUnitsDecodePower);
		EnergyConfig DispatchPower = new EnergyConfig(sm.getDispatchPower(), DispatchUnitAccesses);
		totalPower.add(totalPower, DispatchPower);
		EnergyConfig ScoreBoardPower = new EnergyConfig(sm.getScoreboardPower(), ScoreboardAccesses);
		totalPower.add(totalPower, ScoreBoardPower);
		totalPower.printEnergyStats(outputFileWriter, componentName);
		return totalPower;
	}

}

