package pipeline.Registerfiles.Arbiter;
import pipeline.Registerfiles.*;;
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
//      void dump(FILE fp)  {
//         if( m_allocation == NO_ALLOC ) { fprintf(fp,"<free>"); }
//         else if( m_allocation == READ_ALLOC ) { fprintf(fp,"rd: "); m_op.dump(fp); }
//         else if( m_allocation == WRITE_ALLOC ) { fprintf(fp,"wr: "); m_op.dump(fp); }
//         fprintf(fp,"\n");
//      }
      void alloc_read(  op_t op )  { assert(is_free()); m_allocation=alloc_t.READ_ALLOC; m_op=op; }
      void alloc_write(  op_t op ) { assert(is_free()); m_allocation=alloc_t.WRITE_ALLOC; m_op=op; }
      void reset() { m_allocation = alloc_t.NO_ALLOC; }
  
}

