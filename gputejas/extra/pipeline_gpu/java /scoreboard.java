import java.util.*;
public class Scoreboard {

        public Scoreboard( unsigned sid, unsigned n_warps );
    
        public void reserveRegisters(const warp_inst_t inst)
        {
        for( unsigned r=0; r < 4; r++) {
            if(inst->out[r] > 0) {
                reserveRegister(inst->warp_id(), inst->out[r]);
                SHADER_DSystem.out.println( SCOREBOARD,
                                "Reserved register - warp:%d, reg: %d\n",
                                inst->warp_id(),
                                inst->out[r] );
            }
        }
    
        //Keep track of long operations
        if (inst->is_load() &&
                (	inst->space.get_type() == global_space ||
                    inst->space.get_type() == local_space ||
                    inst->space.get_type() == param_space_kernel ||
                    inst->space.get_type() == param_space_local ||
                    inst->space.get_type() == param_space_unclassified ||
                    inst->space.get_type() == tex_space)){
            for ( unsigned r=0; r<4; r++) {
                if(inst->out[r] > 0) {
                    SHADER_DSystem.out.println( SCOREBOARD,
                                    "New longopreg marked - warp:%d, reg: %d\n",
                                    inst->warp_id(),
                                    inst->out[r] );
                    longopregs[inst->warp_id()].insert(inst->out[r]);
                }
            }
            }
       }
        public void releaseRegisters(const warp_inst_t *inst)
        {
            for( unsigned r=0; r < 4; r++) {
                if(inst->out[r] > 0) {
                    SHADER_DSystem.out.println( SCOREBOARD,
                                    "Register Released - warp:%d, reg: %d\n",
                                    inst->warp_id(),
                                    inst->out[r] );
                    releaseRegister(inst->warp_id(), inst->out[r]);
                    longopregs[inst->warp_id()].erase(inst->out[r]);
                }
            }

        }
        public void releaseRegister(unsigned wid, unsigned regnum)
        {
            if( !(reg_table[wid].find(regnum) != reg_table[wid].end()) ) 
            return;
        SHADER_DSystem.out.println( SCOREBOARD,
                        "Release register - warp:%d, reg: %d\n", wid, regnum );
        reg_table[wid].erase(regnum);


        }
        public bool checkCollision(unsigned wid, const inst_t *inst) const
        {
                        // Get list of all input and output registers
            Set<int> inst_regs;

            if(inst->out[0] > 0) inst_regs.insert(inst->out[0]);
            if(inst->out[1] > 0) inst_regs.insert(inst->out[1]);
            if(inst->out[2] > 0) inst_regs.insert(inst->out[2]);
            if(inst->out[3] > 0) inst_regs.insert(inst->out[3]);
            if(inst->in[0] > 0) inst_regs.insert(inst->in[0]);
            if(inst->in[1] > 0) inst_regs.insert(inst->in[1]);
            if(inst->in[2] > 0) inst_regs.insert(inst->in[2]);
            if(inst->in[3] > 0) inst_regs.insert(inst->in[3]);
            if(inst->pred > 0) inst_regs.insert(inst->pred);
            if(inst->ar1 > 0) inst_regs.insert(inst->ar1);
            if(inst->ar2 > 0) inst_regs.insert(inst->ar2);

            // Check for collision, get the intersection of reserved registers and instruction registers
            Set<int>::const_iterator it2;
            for ( it2=inst_regs.begin() ; it2 != inst_regs.end(); it2++ )
                if(reg_table[wid].find(*it2) != reg_table[wid].end()) {
                    return true;
                }
            return false;

        }
        public bool pendingWrites(unsigned wid) const
        {
            return !reg_table[wid].empty();
        }
        public void printContents() const
        {	
            
        System.out.println("scoreboard contents (sid=%d): \n", m_sid);
        for(unsigned i=0; i<reg_table.size(); i++) {
            if(reg_table[i].size() == 0 ) continue;
            System.out.println("  wid = %2d: ", i);
            Set<unsigned>::const_iterator it;
            for( it=reg_table[i].begin() ; it != reg_table[i].end(); it++ )
                System.out.println("%u ", *it);
            System.out.println("\n");
        }
    }
        const bool islongop(unsigned warp_id, unsigned regnum)
        {   return longopregs[warp_id].find(regnum) != longopregs[warp_id].end();  }
  
        private void reserveRegister(unsigned wid, unsigned regnum)
        {
            if( !(reg_table[wid].find(regnum) == reg_table[wid].end()) ){
                System.out.println("Error: trying to reserve an already reserved register (sid=%d, wid=%d, regnum=%d).", m_sid, wid, regnum);
                abort();
            }
            SHADER_DSystem.out.println( SCOREBOARD,
                            "Reserved Register - warp:%d, reg: %d\n", wid, regnum );
            reg_table[wid].insert(regnum);


        }
        private int get_sid() const { return m_sid; }
    
        private int m_sid;
    
        // keeps track of pending writes to registers
        // indexed by warp id, reg_id => pending write count
        Vector< Set<unsigned> > reg_table;
        //Register that depend on a long operation (global, local or tex memory)
        Vector< Set<unsigned> > longopregs;
    };
    