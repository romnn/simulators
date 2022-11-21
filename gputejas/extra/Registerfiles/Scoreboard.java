package pipeline.Registerfiles;
import java.util.*;
public class Scoreboard {    
	private int m_sid;
    // keeps track of pending writes to registers
    // indexed by warp_inst_t id, reg_id => pending write count
    Vector< Set<Integer> > reg_table;
    //Register that depend on a long operation (global, local or tex memory)
//    Vector< Set<Integer> > longopregs;
	    
	    
    private int get_sid(){ return m_sid; }
    public Scoreboard( int sid, int n_warp_inst_ts )
        {
        	m_sid = sid;
        	//Initialize size of table
//        	reg_table.resize(n_warp_inst_ts);
//        	longopregs.resize(n_warp_inst_ts);
        }
    public void reserveRegisters(warp_inst_t inst)
    {
    for( int r=0; r < 4; r++) {
        if(inst.out[r] > 0) {
            reserveRegister(inst.warp_id(), inst.out[r]);
        System.out.println( "Reserved register");
        }
    }
    }
    // Decide for long operand registers
    //Keep track of long operations
//    if (inst.is_load()){
////    	&&
////            (	inst.space.get_type() == global_space ||
////                inst.space.get_type() == local_space ||
////                inst.space.get_type() == param_space_kernel ||
////                inst.space.get_type() == param_space_local ||
////                inst.space.get_type() == param_space_unclassified ||
////                inst.space.get_type() == tex_space))
//        for ( int r=0; r<4; r++) {
//            if(inst.out[r] > 0) {
////                SHADER_DSystem.out.println( SCOREBOARD,
////                                "New longopreg marked - warp_inst_t:%d, reg: %d\n",
////                                inst.warp_inst_t_id(),
////                                inst.out[r] );
//        longopregs.get(inst.warp_id()).add(inst.out[r]);
//            }
//        }
//        }
//   }
    public void releaseRegisters( warp_inst_t inst)
    {
        for( int r=0; r < 4; r++) {
            if(inst.out[r] > 0) {
                releaseRegister(inst.warp_id(), inst.out[r]);
//                longopregs.get(inst.warp_id()).remove(inst.out[r]);
            }
        }

    }
    public void releaseRegister(int wid, int regnum)
    {
        if( !(reg_table.get(wid).contains(regnum))) 
        return;
       reg_table.get(wid).remove(regnum);
    }
        public boolean checkCollision(int wid, inst_t inst) 
        {
           // Get list of all input and output registers
            Set <Integer> inst_regs=new HashSet<Integer>();

            if(inst.out[0] > 0) inst_regs.add(inst.out[0]);
            if(inst.out[1] > 0) inst_regs.add(inst.out[1]);
            if(inst.out[2] > 0) inst_regs.add(inst.out[2]);
            if(inst.out[3] > 0) inst_regs.add(inst.out[3]);
            if(inst.in[0] > 0) inst_regs.add(inst.in[0]);
            if(inst.in[1] > 0) inst_regs.add(inst.in[1]);
            if(inst.in[2] > 0) inst_regs.add(inst.in[2]);
            if(inst.in[3] > 0) inst_regs.add(inst.in[3]);
            if(inst.pred > 0) inst_regs.add(inst.pred);
            if(inst.ar1 > 0) inst_regs.add(inst.ar1);
            if(inst.ar2 > 0) inst_regs.add(inst.ar2);

            // Check for collision, get the intersection of reserved registers and instruction registers
            Iterator<Integer> it2=inst_regs.iterator();
            while(it2.hasNext())
                if(reg_table.get(wid).contains(it2.next())) {
                    return true;
                }
            return false;

        }
        public boolean pendingWrites(int wid) 
        {
            return !reg_table.get(wid).isEmpty();
        }
//      private boolean islongop(int warp_inst_t_id, int regnum)
//        {   return longopregs.get(warp_inst_t_id).contains(regnum) ;  }
  
        private void reserveRegister(int wid, int regnum)
        {
            if(reg_table.get(wid).contains(regnum) ){
               System.out.println("Error: trying to reserve an already reserved register");
            }
           reg_table.get(wid).add(regnum);
        }  
    }
    