package pipeline.Registerfiles;
import java.util.*;
public class register_set {
private
	Vector<warp_inst_t> regs;
	static char m_name[];
public
register_set(int num, char name[]){
	regs=new Vector<warp_inst_t>(num);
	for( int i = 0; i < num; i++ ) {
		regs.add(new warp_inst_t());
	}
	m_name = name;
}
boolean has_free(){
	for( int i = 0; i < regs.size(); i++ ) {
		if( regs.get(i).empty() ) {
			return true;
		}
	}
	return false;
}
boolean has_ready(){
	for( int i = 0; i < regs.size(); i++ ) {
		if(!regs.get(i).empty() ) {
			return true;
		}
	}
return false;
}

void move_in( warp_inst_t src ){
	warp_inst_t free = get_free();
	move_warp(free, src);
}
void move_out_to( warp_inst_t dest ){
	warp_inst_t ready=get_ready();
	move_warp(dest, ready);
}

void move_warp(warp_inst_t src, warp_inst_t dst) {
	// TODO Auto-generated method stub
	 assert( dst.empty() );
	   warp_inst_t temp = dst;
	   dst = src;
	   src = temp;
	   src.clear();
}
warp_inst_t get_ready(){
	warp_inst_t ready;
	ready = null;
	for( int i = 0; i < regs.size(); i++ ) {
		if( !regs.get(i).empty()) {
			if( ready!=null && ((ready).get_uid() < regs.get(i).get_uid()) ) {
				// ready is oldest
			} else {
				ready = regs.get(i);
			}
		}
	}
	return ready;
}

warp_inst_t  get_free(){
	for( int i = 0; i < regs.size(); i++ ) {
		if( regs.get(i).empty() ) {
			return regs.get(i);
		}
	}
//	assert(0 && "No free registers found");
		return null;
	}


}
