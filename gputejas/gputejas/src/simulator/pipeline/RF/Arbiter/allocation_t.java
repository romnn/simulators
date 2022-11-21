package pipeline.RF.Arbiter;
import pipeline.RF.*;;
//import pipeline.op_t;

enum alloc_t {
    NO_ALLOC,
    READ_ALLOC,
    WRITE_ALLOC,
 }
public class allocation_t {
     alloc_t m_allocation;
     private
      op_t m_op;
   public
      allocation_t() { m_allocation = alloc_t.NO_ALLOC; }
      boolean is_read()  { return m_allocation==alloc_t.READ_ALLOC; }
     boolean is_write()  {return m_allocation==alloc_t.WRITE_ALLOC; }
      boolean is_free()  {return m_allocation==alloc_t.NO_ALLOC; }
      void alloc_read(  op_t op )  { assert(is_free()); m_allocation=alloc_t.READ_ALLOC; m_op=op; }
      void alloc_write(  op_t op ) { assert(is_free()); m_allocation=alloc_t.WRITE_ALLOC; m_op=op; }
      void reset() { m_allocation = alloc_t.NO_ALLOC; }
  
}

