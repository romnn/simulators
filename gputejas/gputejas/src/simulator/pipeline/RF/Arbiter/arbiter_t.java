package pipeline.RF.Arbiter;
import pipeline.RF.*;
import java.util.*;

public class arbiter_t {
   private
      int m_num_banks;
      int m_num_collectors;
      static int MAX_REG_OPERANDS2;
      allocation_t m_allocated_bank[]; // bank # . register that wins
     Deque<op_t> m_queue[];
	
	  int m_allocator_rr_head[]; // cu # . next bank to check for request (rr-arb)
	  int  m_last_cu; // first cu to check while arb-ing banks (rr)
	
	  int _inmatch[];
	  int _outmatch[];
	  int _request[][];
	  Deque<op_t> result;
   public
      // ructors
      arbiter_t()
      {
         m_queue=null;
         m_allocated_bank=null;
         m_allocator_rr_head=(int[]) null;
         _inmatch=(int[]) null;
         _outmatch=(int[]) null;
         _request=(int[][]) null;
         m_last_cu=0;
      }
      @SuppressWarnings("unchecked")
	public
	void init( int num_cu, int num_banks ) 
      { 
         assert(num_cu > 0);
         assert(num_banks > 0);
         m_num_collectors = num_cu;
         m_num_banks = num_banks;
         _inmatch = new int[ m_num_banks ];
         _outmatch = new int[ m_num_collectors ];
         _request = new int[ m_num_banks ][m_num_collectors];
         m_queue = (Deque<op_t>[])new Deque[num_banks];
         
         m_allocated_bank = new allocation_t[num_banks];
         for(int i=0;i<num_banks;i++)
        	 {m_allocated_bank[i]=new allocation_t();
         	 m_queue[i]=new ArrayDeque<op_t>();
        	 }
         m_allocator_rr_head = new int[num_cu];
         for( int n=0; n<num_cu;n++ ) 
            m_allocator_rr_head[n] = n%num_banks;
         reset_alloction();
         
      }

  // accessors
  // modifiers
	  public Deque<op_t> allocate_reads()
	  {
		   // a list of registers that (a) are in different register banks, (b) do not go to the same operand collector
//		  System.out.println("In allocated reads");
		  	result=new ArrayDeque<op_t>();
		     int input;
		     int output;
		     int _inputs = m_num_banks;
		     int _outputs = m_num_collectors;
		     int _square = ( _inputs > _outputs ) ? _inputs : _outputs;
		     assert(_square > 0);
		     int _pri = (int)m_last_cu;
		  
		     // Clear matching
		     for ( int i = 0; i < _inputs; ++i ) 
		        _inmatch[i] = -1;
		     for ( int j = 0; j < _outputs; ++j ) 
		        _outmatch[j] = -1;
		  
		     for( int i=0; i<m_num_banks; i++) {
		        for( int j=0; j<m_num_collectors; j++) {
		           _request[i][j] = 0;
		        }
		        if( !m_queue[i].isEmpty() ) {
		           op_t op = m_queue[i].peekFirst();
		           int oc_id = op.get_oc_id();
		           _request[i][oc_id] = 1;
		        }
//		        System.out.println(m_queue[i].size()+"is the size of banks queues"+i);
		        if( m_allocated_bank[i].is_write() ) {
		           assert( i < (int)_inputs );
		           _inmatch[i] = 0; // write gets priority
		        }
		     }
		  
		     for ( int p = 0; p < _square; ++p ) {
		        output = ( _pri + p ) % _square;
		        // Step through the current diagonal
		        for ( input = 0; input < _inputs; ++input ) {
		           if ( ( output < _outputs )  &&
		                ( _inmatch[input] == -1 ) && 
		                ( _outmatch[output] == -1 ) &&
		                ( _request[input][output])!=0 ) {
		              // Grant!
		              _inmatch[input] = output;
		              _outmatch[output] = input;
		           }
		  
		           output = ( output + 1 ) % _square;
		        }
		     }
		  
		     // Round-robin the priority diagonal
		     _pri = ( _pri + 1 ) % _square;  
		     m_last_cu = _pri;
		     for( int i=0; i < m_num_banks; i++ ) {
		        if( _inmatch[i] != -1 ) {
//		        	System.out.println("what the fuck is here");
		           if( !m_allocated_bank[i].is_write() ) {
		        				              int bank = (int)i;
		              op_t op = m_queue[bank].peekFirst();
		              result.addLast(op);  
//		              System.out.println("result increments and bank decrements");// Check initialization later
		              m_queue[bank].removeFirst();
		           }
		        }
		     }
		  
		     return result;
		  }
	
	  public void add_read_requests( Collector cu ) 
	  {
	      op_t src[] = cu.get_operands();
	      if(src!=null)
	      {for(int i=0;i<src.length;i++)
	    	  m_queue[src[i].get_bank()].addLast(src[i]);
//	      System.out.println("Added to banks queue");
	      }
	    
         
	  }
	  public boolean bank_idle( int bank ) 
	  {
//	      System.out.println("bank id is " +bank);
		  return m_allocated_bank[bank].is_free();
	  }
	  public void allocate_bank_for_write( int bank,  op_t op )
	  {
	     assert( bank < m_num_banks );
	     m_allocated_bank[bank].alloc_write(op);
	  }
	  public void allocate_for_read( int bank,  op_t op )
	  {
	     assert( bank < m_num_banks );
	     m_allocated_bank[bank].alloc_read(op);
	  }
	  public void reset_alloction()
	  {
	     for( int b=0; b < m_num_banks; b++ ) 
	        m_allocated_bank[b].reset();
	  }

	
}
