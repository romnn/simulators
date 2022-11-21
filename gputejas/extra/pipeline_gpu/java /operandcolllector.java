
import java.util.*;
class opndcoll_rfu_t { // operand collector based register file unit
    public
       // constructors
    public opndcoll_rfu_t()
       {
          m_num_banks=0;
          m_shader=NULL;
          m_initialized=false;
       }
       void add_cu_set(int cu_set, int num_cu, int num_dispatch)
       {
        m_cus[set_id].reserve(num_cu); //this is necessary to stop pointers in m_cu from being invalid do to a resize;
        for (int i = 0; i < num_cu; i++) {
            m_cus[set_id].push_back(collector_unit_t());
            m_cu.push_back(&m_cus[set_id].back());
        }
        // for now each collector set gets dedicated dispatch units.
        for (int i = 0; i < num_dispatch; i++) {
            m_dispatch_units.push_back(dispatch_unit_t(&m_cus[set_id]));
        }

       }
       typedef std::vector<register_set*> port_vector_t;
       typedef std::vector<int int> uint_vector_t;
       void add_port( port_vector_t & input, port_vector_t & ouput, uint_vector_t cu_sets)
       {
        //m_num_ports++;
        //m_num_collectors += num_collector_units;
        //m_input.resize(m_num_ports);
        //m_output.resize(m_num_ports);
        //m_num_collector_units.resize(m_num_ports);
        //m_input[m_num_ports-1]=input_port;
        //m_output[m_num_ports-1]=output_port;
        //m_num_collector_units[m_num_ports-1]=num_collector_units;
        m_in_ports.push_back(input_port_t(input,output,cu_sets));

       }
       void init( int num_banks, shader_core_ctx *shader )
       {
        m_shader=shader;
        m_arbiter.init(m_cu.size(),num_banks);
        //for( int n=0; n<m_num_ports;n++ ) 
        //    m_dispatch_units[m_output[n]].init( m_num_collector_units[n] );
        m_num_banks = num_banks;
        m_bank_warp_shift = 0; 
        m_warp_size = shader->get_config()->warp_size;
        m_bank_warp_shift = (int)(int) (log(m_warp_size+0.5) / log(2.0));
        assert( (m_bank_warp_shift == 5) || (m_warp_size != 32) );
     
        for( int j=0; j<m_cu.size(); j++) {
            m_cu[j]->init(j,num_banks,m_bank_warp_shift,shader->get_config(),this);
        }
        m_initialized=true;
      }
    
       // modifiers
       bool writeback( const warp_inst_t &warp ) // might cause stall 
        {
            assert( !inst.empty() );
            std::list<int> regs = m_shader->get_regs_written(inst);
            std::list<int>::iterator r;
            int n=0;
            for( r=regs.begin(); r!=regs.end();r++,n++ ) {
               int reg = *r;
               int bank = register_bank(reg,inst.warp_id(),m_num_banks,m_bank_warp_shift);
               if( m_arbiter.bank_idle(bank) ) {
                   m_arbiter.allocate_bank_for_write(bank,op_t(&inst,reg,m_num_banks,m_bank_warp_shift));
               } else {
                   return false;
               }
               for(int i=0;i<(int)regs.size();i++){
                if(m_shader->get_config()->gpgpu_clock_gated_reg_file){
                    int active_count=0;
                    for(int i=0;i<m_shader->get_config()->warp_size;i=i+m_shader->get_config()->n_regfile_gating_group){
                        for(int j=0;j<m_shader->get_config()->n_regfile_gating_group;j++){
                            if(inst.get_active_mask().test(i+j)){
                                active_count+=m_shader->get_config()->n_regfile_gating_group;
                                break;
                            }
                        }
                    }
                    m_shader->incregfile_writes(active_count);
                }else{
                    m_shader->incregfile_writes(m_shader->get_config()->warp_size);//inst.active_count());
                }
         }
         return true;

        }
       void step()
       {
            dispatch_ready_cu();   
            allocate_reads();
            for( int p = 0 ; p < m_in_ports.size(); p++ ) 
                allocate_cu( p );
            process_banks();
       }
    
       void dump( FILE *fp ) const
       {
          fprintf(fp,"\n");
          fprintf(fp,"Operand Collector State:\n");
          for( int n=0; n < m_cu.size(); n++ ) {
             fprintf(fp,"   CU-%2u: ", n);
             m_cu[n]->dump(fp,m_shader);
          }
          m_arbiter.dump(fp);
       }
    
       shader_core_ctx *shader_core() { return m_shader; }
    
    private:
    
       void process_banks()
       {
          m_arbiter.reset_alloction();
       }
    
       void dispatch_ready_cu()
       {
        for( int p=0; p < m_dispatch_units.size(); ++p ) {
            dispatch_unit_t &du = m_dispatch_units[p];
            collector_unit_t *cu = du.find_ready();
            if( cu ) {
               for(int i=0;i<(cu->get_num_operands()-cu->get_num_regs());i++){
                   if(m_shader->get_config()->gpgpu_clock_gated_reg_file){
                       int active_count=0;
                       for(int i=0;i<m_shader->get_config()->warp_size;i=i+m_shader->get_config()->n_regfile_gating_group){
                           for(int j=0;j<m_shader->get_config()->n_regfile_gating_group;j++){
                               if(cu->get_active_mask().test(i+j)){
                                   active_count+=m_shader->get_config()->n_regfile_gating_group;
                                   break;
                               }
                           }
                       }
                       m_shader->incnon_rf_operands(active_count);
                   }else{
                   m_shader->incnon_rf_operands(m_shader->get_config()->warp_size);//cu->get_active_count());
                   }
              }
               cu->dispatch();
            }
         }

       }
       void allocate_cu( int port )
       {
        input_port_t& inp = m_in_ports[port_num];
        for (int i = 0; i < inp.m_in.size(); i++) {
            if( (*inp.m_in[i]).has_ready() ) {
               //find a free cu 
               for (int j = 0; j < inp.m_cu_sets.size(); j++) {
                   std::vector<collector_unit_t> & cu_set = m_cus[inp.m_cu_sets[j]];
               bool allocated = false;
                   for (int k = 0; k < cu_set.size(); k++) {
                       if(cu_set[k].is_free()) {
                          collector_unit_t *cu = &cu_set[k];
                          allocated = cu->allocate(inp.m_in[i],inp.m_out[i]);
                          m_arbiter.add_read_requests(cu);
                          break;
                       }
                   }
                   if (allocated) break; //cu has been allocated, no need to search more.
               }
               break; // can only service a single input, if it failed it will fail for others.
            }
        }

       }
       void allocate_reads(){
   // process read requests that do not have conflicts
   std::list<op_t> allocated = m_arbiter.allocate_reads();
   std::map<int,op_t> read_ops;
   for( std::list<op_t>::iterator r=allocated.begin(); r!=allocated.end(); r++ ) {
      const op_t &rr = *r;
      int reg = rr.get_reg();
      int wid = rr.get_wid();
      int bank = register_bank(reg,wid,m_num_banks,m_bank_warp_shift);
      m_arbiter.allocate_for_read(bank,rr);
      read_ops[bank] = rr;
   }
   std::map<int,op_t>::iterator r;
   for(r=read_ops.begin();r!=read_ops.end();++r ) {
      op_t &op = r->second;
      int cu = op.get_oc_id();
      int operand = op.get_operand();
      m_cu[cu]->collect_operand(operand);
      if(m_shader->get_config()->gpgpu_clock_gated_reg_file){
    	  int active_count=0;
    	  for(int i=0;i<m_shader->get_config()->warp_size;i=i+m_shader->get_config()->n_regfile_gating_group){
    		  for(int j=0;j<m_shader->get_config()->n_regfile_gating_group;j++){
    			  if(op.get_active_mask().test(i+j)){
    				  active_count+=m_shader->get_config()->n_regfile_gating_group;
    				  break;
    			  }
    		  }
    	  }
    	  m_shader->incregfile_reads(active_count);
      }else{
    	  m_shader->incregfile_reads(m_shader->get_config()->warp_size);//op.get_active_count());
      }
  }


       }
    
       // types
    
       class collector_unit_t{
        public bool ready() const 
        { 
           return (!m_free) && m_not_ready.none() && (*m_output_register).has_free(); 
        }
        
        void dump(FILE *fp, const shader_core_ctx *shader ) const
        {
           if( m_free) {
              fprintf(fp,"    <free>\n");
           } else {
              m_warp->print(fp);
              for( int i=0; i < MAX_REG_OPERANDS*2; i++ ) {
                 if( m_not_ready.test(i) ) {
                    std::string r = m_src_op[i].get_reg_string();
                    fprintf(fp,"    '%s' not ready\n", r.c_str() );
                 }
              }
           }
        }
       public void  init( int n,  int num_banks,  int log2_warp_size,const core_config *config,    opndcoll_rfu_t *rfu ) 
        { 
        m_rfu=rfu;
        m_cuid=n; 
        m_num_banks=num_banks;
        assert(m_warp==NULL); 
        m_warp = new warp_inst_t(config);
        m_bank_warp_shift=log2_warp_size;
        }

       public bool allocate( register_set* pipeline_reg_set, register_set* output_reg_set ) 
        {
           assert(m_free);
           assert(m_not_ready.none());
           m_free = false;
           m_output_register = output_reg_set;
           warp_inst_t **pipeline_reg = pipeline_reg_set->get_ready();
           if( (pipeline_reg) and !((*pipeline_reg)->empty()) ) {
              m_warp_id = (*pipeline_reg)->warp_id();
              for( int op=0; op < MAX_REG_OPERANDS; op++ ) {
                 int reg_num = (*pipeline_reg)->arch_reg.src[op]; // this math needs to match that used in function_info::ptx_decode_inst
                 if( reg_num >= 0 ) { // valid register
                    m_src_op[op] = op_t( this, op, reg_num, m_num_banks, m_bank_warp_shift );
                    m_not_ready.set(op);
                 } else 
                    m_src_op[op] = op_t();
              }
              //move_warp(m_warp,*pipeline_reg);
              pipeline_reg_set->move_out_to(m_warp);
              return true;
           }
           return false;
        }
        public void dispatch()
        {
           assert( m_not_ready.none() );
           //move_warp(*m_output_register,m_warp);
           m_output_register->move_in(m_warp);
           m_free=true;
           m_output_register = NULL;
           for( int i=0; i<MAX_REG_OPERANDS*2;i++)
              m_src_op[i].reset();
        }

       }
    
       class op_t {
       public:
    
          op_t() { m_valid = false; }
          op_t( collector_unit_t *cu, int op, int reg, int num_banks, int bank_warp_shift )
          {
             m_valid = true;
             m_warp=NULL;
             m_cu = cu;
             m_operand = op;
             m_register = reg;
             m_bank = register_bank(reg,cu->get_warp_id(),num_banks,bank_warp_shift);
          }
          op_t( const warp_inst_t *warp, int reg, int num_banks, int bank_warp_shift )
          {
             m_valid=true;
             m_warp=warp;
             m_register=reg;
             m_cu=NULL;
             m_operand = -1;
             m_bank = register_bank(reg,warp->warp_id(),num_banks,bank_warp_shift);
          }
    
          // accessors
          bool valid() const { return m_valid; }
          int get_reg() const
          {
             assert( m_valid );
             return m_register;
          }
          int get_wid() const
          {
              if( m_warp ) return m_warp->warp_id();
              else if( m_cu ) return m_cu->get_warp_id();
              else abort();
          }
          int get_active_count() const
          {
              if( m_warp ) return m_warp->active_count();
              else if( m_cu ) return m_cu->get_active_count();
              else abort();
          }
          const active_mask_t & get_active_mask()
          {
              if( m_warp ) return m_warp->get_active_mask();
              else if( m_cu ) return m_cu->get_active_mask();
              else abort();
          }
          int get_sp_op() const
          {
              if( m_warp ) return m_warp->sp_op;
              else if( m_cu ) return m_cu->get_sp_op();
              else abort();
          }
          int get_oc_id() const { return m_cu->get_id(); }
          int get_bank() const { return m_bank; }
          int get_operand() const { return m_operand; }
          void dump(FILE *fp) const 
          {
             if(m_cu) 
                fprintf(fp," <R%u, CU:%u, w:%02u> ", m_register,m_cu->get_id(),m_cu->get_warp_id());
             else if( !m_warp->empty() )
                fprintf(fp," <R%u, wid:%02u> ", m_register,m_warp->warp_id() );
          }
          std::string get_reg_string() const
          {
             char buffer[64];
             snprintf(buffer,64,"R%u", m_register);
             return std::string(buffer);
          }
    
          // modifiers
          void reset() { m_valid = false; }
       private:
          bool m_valid;
          collector_unit_t  *m_cu; 
          const warp_inst_t *m_warp;
          int  m_operand; // operand offset in instruction. e.g., add r1,r2,r3; r2 is oprd 0, r3 is 1 (r1 is dst)
          int  m_register;
          int  m_bank;
       };
    
       enum alloc_t {
          NO_ALLOC,
          READ_ALLOC,
          WRITE_ALLOC,
       };
    
    
       class dispatch_unit_t {
       public:
          dispatch_unit_t(std::vector<collector_unit_t>* cus) 
          { 
             m_last_cu=0;
             m_collector_units=cus;
             m_num_collectors = (*cus).size();
             m_next_cu=0;
          }
    
          collector_unit_t *find_ready()
          {
             for( int n=0; n < m_num_collectors; n++ ) {
                int c=(m_last_cu+n+1)%m_num_collectors;
                if( (*m_collector_units)[c].ready() ) {
                   m_last_cu=c;
                   return &((*m_collector_units)[c]);
                }
             }
             return NULL;
          }
    
       private:
          int m_num_collectors;
          std::vector<collector_unit_t>* m_collector_units;
          int m_last_cu; // dispatch ready cu's rr
          int m_next_cu;  // for initialization
       };
    
       // opndcoll_rfu_t data members
       bool m_initialized;
    
       int m_num_collector_sets;
       //int m_num_collectors;
       int m_num_banks;
       int m_bank_warp_shift;
       int m_warp_size;
       std::vector<collector_unit_t *> m_cu;
       arbiter_t m_arbiter;
    
       //int m_num_ports;
       //std::vector<warp_inst_t**> m_input;
       //std::vector<warp_inst_t**> m_output;
       //std::vector<int> m_num_collector_units;
       //warp_inst_t **m_alu_port;
    
       std::vector<input_port_t> m_in_ports;
       typedef std::map<int /* collector set */, std::vector<collector_unit_t> /*collector sets*/ > cu_sets_t;
       cu_sets_t m_cus;
       std::vector<dispatch_unit_t> m_dispatch_units;
    
       //typedef std::map<warp_inst_t**/*port*/,dispatch_unit_t> port_to_du_t;
       //port_to_du_t                     m_dispatch_units;
       //std::map<warp_inst_t**,std::list<collector_unit_t*> > m_free_cu;
       shader_core_ctx                 *m_shader;
    };
    