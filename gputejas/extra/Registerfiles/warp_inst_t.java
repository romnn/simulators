package pipeline.Registerfiles;
import java.util.* ;
import config.SmConfig;

public class warp_inst_t extends inst_t {
	

    private static final int MAX_WarpSize = 0;
	int m_uid;
    boolean m_empty;
    boolean m_cache_hit;
    long issue_cycle;
    int cycles; // used for implementing initiation interval delay
    int m_warp_id;
    int m_dynamic_warp_id; 
    BitSet m_warp_active_mask; // dynamic active mask for timing model (after predication)
    BitSet m_warp_issued_mask; // active mask at issue (prior to predication test) -- for instruction counting
    // TODO Check initialization of all variables
    public int dst[];
    public int src[];
    boolean m_per_scalar_thread_valid;
    boolean m_mem_accesses_created;
    static int sm_next_uid;
    
public
    // ructors
    warp_inst_t() 
    {
        m_uid=0;
        m_empty=true; 
        // TODO
//        SmConfig=null; 
    }
    warp_inst_t(SmConfig config)  // Input should be the configuration
    { 
        m_uid=0;
        assert(config.WarpSize<=MAX_WarpSize);
        // TODO
//        SmConfig=config;
        m_empty=true; 
        m_per_scalar_thread_valid=false;
        m_mem_accesses_created=false;
        m_cache_hit=false;

    }
    // modifiers

    void clear() 
    { 
        m_empty=true; 
    }
        BitSet  get_active_mask() 
    {
    	return m_warp_active_mask;
    }

    // accessors
    boolean active( int thread )  { return m_warp_active_mask.get(thread); }
    int active_count()  { return m_warp_active_mask.cardinality(); }
    int issued_count()  { assert(m_empty == false); return m_warp_issued_mask.cardinality(); }  // for instruction counting 
    boolean empty()  { return m_empty; }
    int warp_id()  
    { 
        assert( !m_empty );
        return m_warp_id; 
    }
    int dynamic_warp_id()  
    { 
        assert( !m_empty );
        return m_dynamic_warp_id; 
    }
    boolean dispatch_delay()
    { 
        if( cycles > 0 ) 
            cycles--;
        return cycles > 0;
    }

    boolean has_dispatch_delay(){
    	return cycles > 0;
    }

//    void print( FILE fout ) ;
    int get_uid()  { return m_uid; }


void completed( long cycle )  
{
   long latency = cycle - issue_cycle; 
   assert(latency <= cycle); // underflow detection 
}

}
