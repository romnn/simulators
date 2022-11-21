package pipeline.Registerfiles;
import pipeline.*;
import java.util.*;
import java.util.Map.Entry;

import config.SmConfig;
import generic.SM;

public class barrier{
       static final int WARP_PER_CTA_MAX = 48;
	private static final int MAX_BARRIERS_PER_CTA = 0;
       int m_max_cta_per_core;
       int m_max_warps_per_core;
       int m_max_barriers_per_cta;
       int m_warp_size;
       Map<Integer,BitSet> m_cta_to_warps;
       Map<Integer,BitSet> m_bar_id_to_warps;
       BitSet m_warp_active;
       BitSet m_warp_at_barrier;
       SM sm;
       GPUExecutionEngine containingExecutionEngine;
   
    public barrier()
    {
    	
    	m_warp_active=new BitSet(WARP_PER_CTA_MAX);
        m_warp_at_barrier=new BitSet(WARP_PER_CTA_MAX);
    	
    	
    }
	public barrier(SM sm, int max_warps_per_core, int max_cta_per_core, int max_barriers_per_cta, int warp_size)
       {  
        m_max_warps_per_core = max_warps_per_core;
        m_max_cta_per_core = max_cta_per_core;
        m_max_barriers_per_cta = max_barriers_per_cta;
        m_warp_size = warp_size;
        m_warp_active=new BitSet(WARP_PER_CTA_MAX);
        m_warp_at_barrier=new BitSet(WARP_PER_CTA_MAX);
        this.sm = sm;
     // TODO Checks whether config is correct is or not 
        if( max_warps_per_core > WARP_PER_CTA_MAX ) {
//           System.out.printf("ERROR ** increase WARP_PER_CTA_MAX in shader.h from %u to >= %u or warps per cta in gpgpusim.config\n", WARP_PER_CTA_MAX, max_warps_per_core );
           System.exit(1);
        }
        if(max_barriers_per_cta > MAX_BARRIERS_PER_CTA){
//            System.out.println("ERROR ** increase MAX_BARRIERS_PER_CTA in abstract_hardware_model.h from %u to >= %u or barriers per cta in gpgpusim.config\n", MAX_BARRIERS_PER_CTA, max_barriers_per_cta );
            System.exit(1);
        }
        m_warp_active.clear();
        m_warp_at_barrier.clear();
        for(int i=0; i<max_barriers_per_cta; i++){
            m_bar_id_to_warps.get(i).clear();
        }

       }
      // during cta allocation
       void allocate_barrier( int cta_id, BitSet warps )
       {

        assert( cta_id < m_max_cta_per_core );
//        @SuppressWarnings("rawtypes")
        // TODO No iterator is available in java for BitSet
//		Iterator w=m_cta_to_warps.get(cta_id).iterator();
//        assert( w == m_cta_to_warps.end() ); // cta should not already be active or allocated barrier resources
//        m_cta_to_warps.put(cta_id,warps);
        assert( m_cta_to_warps.size() <= m_max_cta_per_core ); // catch cta's that were not properly deallocated
        m_warp_active.or(warps);
        m_warp_at_barrier.andNot(warps);
        for(int i=0; i<m_max_barriers_per_cta; i++){
            m_bar_id_to_warps.get(i).andNot(warps);
        }

       }
       // during cta deallocation
       void deallocate_barrier( int cta_id )
      {
    	   // TODO
//        Iterator w = m_cta_to_warps.get(cta_id);
//        if( w == m_cta_to_warps.end())
//           return;
        BitSet warps = m_cta_to_warps.get(cta_id);
        BitSet at_barrier = (BitSet) m_cta_to_warps.get(cta_id).clone();
        at_barrier.and(m_warp_at_barrier);
        assert( at_barrier.cardinality() ==0 ); // no warps stuck at barrier
        BitSet active = (BitSet) m_cta_to_warps.get(cta_id).clone();
        active.and(m_warp_active);
        assert(at_barrier.cardinality() ==0 ); // no warps in CTA still running
        m_warp_active.andNot(warps);
        m_warp_at_barrier.andNot(warps);
        BitSet at_a_specific_barrier = m_cta_to_warps.get(cta_id);
        for(int i=0; i<m_max_barriers_per_cta; i++){
           at_a_specific_barrier.and(m_bar_id_to_warps.get(i));
            assert( at_a_specific_barrier.cardinality()==0 ); // no warps stuck at barrier
            m_bar_id_to_warps.get(i).andNot(warps);
        }
        m_cta_to_warps.remove(cta_id);
       }
    
  /*set of warps reached a specific barrier id*/
    
 // individual warp hits barrier
       void warp_reaches_barrier( int cta_id, int warp_id, warp_inst_t inst)
       {
        barrier_type bar_type = inst.bar_type;
        int bar_id = inst.bar_id;
        int bar_count = inst.bar_count;
        assert(bar_id!=(int)-1);
//       Iterator w=m_cta_to_warps.get(cta_id).iterator();
//    
//       if( w == m_cta_to_warps.end() ) { // cta is active
////          System.out.println("ERROR ** cta_id %u not found in barrier set on cycle %llu+%llu...\n", cta_id, gpu_tot_sim_cycle, gpu_sim_cycle );
////          dump();
////          abort();
//    	   System.exit(1);
//       }
//       assert( w.second.test(warp_id) == true ); // warp is in cta
//    
//       m_bar_id_to_warps[bar_id].set(warp_id);
//       if(bar_type==SYNC || bar_type==RED){
//           m_warp_at_barrier.set(warp_id);
//       }
       BitSet warps_in_cta = m_cta_to_warps.get(cta_id);
       BitSet at_barrier = (BitSet) warps_in_cta.clone(); 
       at_barrier.and(m_bar_id_to_warps.get(bar_id));
       BitSet active = (BitSet) warps_in_cta.clone();
       active.and(m_warp_active);
       if(bar_count==(int)-1){
           if( at_barrier == active ) {
               // all warps have reached barrier, so release waiting warps...
               m_bar_id_to_warps.get(bar_id).andNot(at_barrier);
               m_warp_at_barrier.andNot(at_barrier);
               if(bar_type==barrier_type.RED){
            	   // TODO Check this also 
                   containingExecutionEngine.broadcast_barrier_reduction(cta_id, bar_id,at_barrier);
               }
           }
      }else{
          // TODO: check on the hardware if the count should include warp that exited
          if ((at_barrier.cardinality() * m_warp_size) == bar_count){
               // required number of warps have reached barrier, so release waiting warps...
               m_bar_id_to_warps.get(bar_id).andNot(at_barrier);
               m_warp_at_barrier.andNot(at_barrier);
               if(bar_type==barrier_type.RED){
                   containingExecutionEngine.broadcast_barrier_reduction(cta_id, bar_id,at_barrier);
               }
          }
      }

       }
       // warp reaches exit 
       void warp_exit( int warp_id )
       {
        // caller needs to verify all threads in warp are done, e.g., by checking PDOM stack to 
        // see it has only one entry during exit_impl()
        m_warp_active.clear(warp_id);

        // test for barrier release
        // TODO
        Iterator w=m_cta_to_warps.entrySet().iterator(); 
        Map.Entry<Integer,BitSet> entry = null;
        while(w.hasNext()){
        	entry=  (Entry<Integer, BitSet>) w.next();
            if (entry.getValue().get(warp_id) == true) break; 
        }
       
        BitSet warps_in_cta = entry.getValue();
        BitSet active = (BitSet) warps_in_cta.clone();
        active.and(m_warp_active);

        for(int i=0; i<m_max_barriers_per_cta; i++){
            BitSet at_a_specific_barrier = (BitSet) warps_in_cta.clone();
            at_a_specific_barrier.and(m_bar_id_to_warps.get(i));
            if( at_a_specific_barrier == active ) {
                // all warps have reached barrier, so release waiting warps...
                m_bar_id_to_warps.get(i).andNot(at_a_specific_barrier);
                m_warp_at_barrier.andNot(at_a_specific_barrier);
            }
        }

       }
    
       // assertions
       boolean warp_waiting_at_barrier( int warp_id ) 
       {
        return m_warp_at_barrier.get(warp_id);

       }
    
   }