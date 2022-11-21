package pipeline.Registerfiles;
//#define MAX_CTA_PER_SHADER 32
//#define MAX_BARRIERS_PER_CTA 16
public class inst_t {
    int pc;        // program counter address of instruction
    int isize;         // size of instruction in bytes 
    op_type op;             // opcode (uarch visible)

    barrier_type bar_type;
    reduction_type red_type;
    int bar_id;
    int bar_count;

    types_of_operands oprnd_type;     // code (uarch visible) identify if the operation is an interger or a floating point
    special_ops sp_op;           // code (uarch visible) identify if int_alu, fp_alu, int_mul ....
    operation_pipeline op_pipe;  // code (uarch visible) identify the pipeline of the operation (SP, SFU or MEM)
    mem_operation mem_op;        // code (uarch visible) identify memory type
    _memory_op_t memory_op; // memory_op used by ptxplus 
    int num_operands;
    int num_regs; // count vector operand as one register operand

    int reconvergence_pc; // -1 => not a branch, -2 => use function return address
    
    int out[];
    int in[];
    char is_vectorin;
    char is_vectorout;
    int pred; // predicate register number
    int ar1, ar2;
    // register number for bank conflict evaluation
       int dst[];
      int src[];

    int latency; // operation latency 
    int initiation_interval;

    int data_size; 
	
	
public
    inst_t()
    {
        m_decoded=false;
        pc=(int)-1;
        reconvergence_pc=(int)-1;
        op=op_type.NO_OP;
        bar_type=barrier_type.NOT_BAR;
        red_type=reduction_type.NOT_RED;
        bar_id=(int)-1;
        bar_count=(int)-1;
        oprnd_type=types_of_operands.UN_OP;
        sp_op=special_ops.OTHER_OP;
        op_pipe=operation_pipeline.UNKOWN_OP;
        mem_op=mem_operation.NOT_TEX;
        num_operands=0;
        num_regs=0;
        // TODO Initialize arrays to zero 
//        memset(out, 0, sizeof(int)); 
//        memset(in, 0, sizeof(int)); 
        is_vectorin=0; 
        is_vectorout=0;
//        space = memory_space_t();
//        cache_op = CACHE_UNDEFINED;
        latency = 1;
        initiation_interval = 1;
        // TODO
//        for( int i=0; i < MAX_REG_OPERANDS; i++ ) {
//            arch_reg.src[i] = -1;
//            arch_reg.dst[i] = -1;
//        }
        isize=0;
    }
    boolean valid()  { return m_decoded; }
    boolean is_load()  { return (op == op_type.LOAD_OP || memory_op == _memory_op_t.memory_load); }
    boolean is_store()  { return (op == op_type.STORE_OP || memory_op == _memory_op_t.memory_store); }
    int get_num_operands()  {return num_operands;}
    int get_num_regs()  {return num_regs;}
    void set_num_regs(int num) {num_regs=num;}
    void set_num_operands(int num) {num_operands=num;}
    void set_bar_id(int id) {bar_id=id;}
    void set_bar_count(int count) {bar_count=count;}

// what is the size of the word being operated on?
//    memory_space_t space;
//    cache_operator_type cache_op;

protected
    boolean m_decoded;
    void pre_decode() {}
}



