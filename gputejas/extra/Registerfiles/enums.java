package pipeline.Registerfiles;
enum op_type {
    NO_OP,
    ALU_OP,
    SFU_OP,
    ALU_SFU_OP,
    LOAD_OP,
    STORE_OP,
    BRANCH_OP,
    BARRIER_OP,
    MEMORY_BARRIER_OP,
    CALL_OPS,
    RET_OPS
 };

 
 
 enum barrier_type{
    NOT_BAR,
    SYNC,
    ARRIVE,
    RED
 };

 
 enum reduction_type {
    NOT_RED,
    POPC_RED,
    AND_RED,
    OR_RED
 };
 
 
 enum types_of_operands {
     UN_OP,
     INT_OP,
     FP_OP
 };

 enum special_ops {
     OTHER_OP,
     INT__OP,
     INT_MUL24_OP,
     INT_MUL32_OP,
     INT_MUL_OP,
     INT_DIV_OP,
     FP_MUL_OP,
     FP_DIV_OP,
     FP__OP,
     FP_SQRT_OP,
     FP_LG_OP,
     FP_SIN_OP,
     FP_EXP_OP
 };

 enum operation_pipeline {
     UNKOWN_OP,
     SP__OP,
     SFU__OP,
     MEM__OP
 };
 enum mem_operation{
     NOT_TEX,
     TEX
 };
 enum _memory_op_t {
     no_memory_op,
     memory_load,
     memory_store
 };

enum divergence_support_t {
  POST_DOMINATOR,
  NUM_SIMD_MODEL
};

//
enum scheduler_prioritization_type
{
  SCHEDULER_PRIORITIZATION_LRR,// Loose Round Robin
  SCHEDULER_PRIORITIZATION_SRR, // Strict Round Robin
  SCHEDULER_PRIORITIZATION_GTO, // Greedy Then Oldest
  SCHEDULER_PRIORITIZATION_GTLRR, // Greedy Then Loose Round Robin
  SCHEDULER_PRIORITIZATION_GTY, // Greedy Then Youngest
  SCHEDULER_PRIORITIZATION_OLDEST, // Oldest First
  SCHEDULER_PRIORITIZATION_YOUNGEST, // Youngest First
};
