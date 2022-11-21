package pipeline.Registerfiles.Arbiter;
import pipeline.Registerfiles.*;
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
         m_allocator_rr_head = new int[num_cu];
         for( int n=0; n<num_cu;n++ ) 
            m_allocator_rr_head[n] = n%num_banks;
         reset_alloction();
      }

  // accessors
  // modifiers
	  public Deque<op_t> allocate_reads()
	  {
		  Deque<op_t> result = null;  // a list of registers that (a) are in different register banks, (b) do not go to the same operand collector
		  
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
		           assert( i < (int)_inputs );
		           assert( j < (int)_outputs );
		           _request[i][j] = 0;
		        }
		        if( !m_queue[i].isEmpty() ) {
		           op_t op = m_queue[i].peekFirst();
		           int oc_id = op.get_oc_id();
		           assert( i < (int)_inputs );
		           assert( oc_id < _outputs );
		           _request[i][oc_id] = 1;
		        }
		        if( m_allocated_bank[i].is_write() ) {
		           assert( i < (int)_inputs );
		           _inmatch[i] = 0; // write gets priority
		        }
		     }
		  
		     ///// wavefront allocator from booksim... --.
		     
		     // Loop through diagonals of request matrix
		  
		     for ( int p = 0; p < _square; ++p ) {
		        output = ( _pri + p ) % _square;
		  
		        // Step through the current diagonal
		        for ( input = 0; input < _inputs; ++input ) {
		            assert( input < _inputs );
		            assert( output < _outputs );
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
		  
		     /// <--- end code from booksim
		  
		     m_last_cu = _pri;
		     for( int i=0; i < m_num_banks; i++ ) {
		        if( _inmatch[i] != -1 ) {
		           if( !m_allocated_bank[i].is_write() ) {
		              int bank = (int)i;
		              op_t op = m_queue[bank].peekFirst();
		              // TODO 
		              result.addLast(op);  // Check initialization later
		              m_queue[bank].removeFirst();
		           }
		        }
		     }
		  
		     return result;
		  }
	
	  public void add_read_requests( collector_unit_t cu ) 
	  {
	      op_t src[] = cu.get_operands();
	     for( int i=0; i<MAX_REG_OPERANDS2; i++) {
	         op_t op = src[i];
	        if( op.valid() ) {
	           int bank = op.get_bank();
	           m_queue[bank].addLast(op);
	        }
	     }
	  }
	  public boolean bank_idle( int bank ) 
	  {
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
