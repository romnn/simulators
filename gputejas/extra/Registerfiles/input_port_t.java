package pipeline.Registerfiles;
import java.util.*;


public class input_port_t {
   input_port_t(Vector<register_set> input, Vector<register_set> output, Vector<Integer> cu_sets)
		      {
		           assert(input.size() == output.size());
//		           assert(not m_cu_sets.empty());
		       }
		   //private:
   Vector<register_set> m_in,m_out;
   Vector<Integer> m_cu_sets;
		   };
