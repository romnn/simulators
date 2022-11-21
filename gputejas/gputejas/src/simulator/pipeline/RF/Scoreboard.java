package pipeline.RF;
import java.util.*;
public class Scoreboard {    
	private int m_sid;
    // keeps track of pending writes to registers
    // indexed by warp_inst_t id, reg_id => pending write count
    Vector< Set<Integer> > reg_table;   
    private int get_sid(){ return m_sid; }
    public Scoreboard( int sid, int n_warp_inst_ts )
        {
        	m_sid = sid;
        	reg_table=new Vector<Set<Integer>>(n_warp_inst_ts);
        	for(int i=0;i<n_warp_inst_ts;i++)
        		reg_table.add(i, new HashSet<Integer>());
        }
    public void reserveRegisters(Warp inst)
    {
//    	reserveRegister(inst.warp_id(), inst.out);
//    for( int r=0; r < 4; r++) {
//        if(inst.out[r] > 0) {
//            reserveRegister(inst.warp_id(), inst.out[r]);
//        System.out.println( "Reserved register");
//        }
//    }
    }
    public void releaseRegisters( Warp inst)
    {
//    	 releaseRegister(inst.warp_id(), inst.out);
//        for( int r=0; r < 4; r++) {
//            if(inst.out[r] > 0) {
//                releaseRegister(inst.warp_id(), inst.out[r]);
//            }
//        }
//    	 System.out.println("Release Register"+inst.out);

    }
    public void releaseRegister(int wid, int regnum)
    {
       if(regnum!=0)
    	{if( !(reg_table.get(wid).contains(regnum))) 
        return;
       reg_table.get(wid).remove(regnum);
//       System.out.println("register removed");
       }
    }
        public boolean pendingWrites(int wid) 
        {
            return !reg_table.get(wid).isEmpty();
        }
        private void reserveRegister(int wid, int regnum)
        {	
//        	System.out.println("the wid is "+wid);
    
        	if(regnum!=0){
        	if(reg_table.get(wid)!=null)
        	{
            if(reg_table.get(wid).contains(regnum) ){
               System.out.println("Error: trying to reserve an already reserved register");
            }
        	}
        	
           reg_table.get(wid).add(regnum);
        }  
}
    }
    