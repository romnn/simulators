package pipeline.RF;
import java.util.*;

public class Dispatch {
    public
       Dispatch(Vector<Collector> cus) 
       { 
          m_last_cu=0;
          m_collector_units=cus;
          m_num_collectors = (cus).size();
          m_next_cu=0;
       }
 
       Collector find_ready()
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
       Vector <Collector> m_collector_units;
       int m_last_cu; // dispatch ready cu's rr
       int m_next_cu;  // for initialization
    }