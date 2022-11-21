package pipeline.Registerfiles;
import java.util.*;

public class dispatch_unit_t {
    public
       dispatch_unit_t(Vector<collector_unit_t> cus) 
       { 
          m_last_cu=0;
          m_collector_units=cus;
          m_num_collectors = (cus).size();
          m_next_cu=0;
       }
 
       collector_unit_t find_ready()
       {
          for( int n=0; n < m_num_collectors; n++ ) {
             int c=(m_last_cu+n+1)%m_num_collectors;
             if( (m_collector_units).get(c).ready() ) {
                m_last_cu=c;
                return ((m_collector_units).get(c));
             }
          }
          return null;
       }
 
    private
       int m_num_collectors;
      Vector <collector_unit_t> m_collector_units;
       int m_last_cu; // dispatch ready cu's rr
       int m_next_cu;  // for initialization
    }