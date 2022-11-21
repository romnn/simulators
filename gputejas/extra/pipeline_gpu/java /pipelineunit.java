
class simd_function_unit {
    public:
        simd_function_unit( const shader_core_config *config );
        ~simd_function_unit() { delete m_dispatch_reg; }
    
        // modifiers
        virtual void issue( register_set& source_reg ) { source_reg.move_out_to(m_dispatch_reg); occupied.set(m_dispatch_reg->latency);}
        virtual void cycle() = 0;
        virtual void active_lanes_in_pipeline() = 0;
    
        // accessors
        virtual unsigned clock_multiplier() const { return 1; }
        virtual bool can_issue( const warp_inst_t &inst ) const { return m_dispatch_reg->empty() && !occupied.test(inst.latency); }
        virtual bool stallable() const = 0;
        virtual void print( FILE *fp ) const
        {
            fprintf(fp,"%s dispatch= ", m_name.c_str() );
            m_dispatch_reg->print(fp);
        }
    protected:
        std::string m_name;
        const shader_core_config *m_config;
        warp_inst_t *m_dispatch_reg;
        static const unsigned MAX_ALU_LATENCY = 512;
        std::bitset<MAX_ALU_LATENCY> occupied;
    };
    
    class pipelined_simd_unit : public simd_function_unit {
    public:
        pipelined_simd_unit( register_set* result_port, const shader_core_config *config, unsigned max_latency, shader_core_ctx *core );
    
        //modifiers
        virtual void cycle();
        virtual void issue( register_set& source_reg );
        virtual unsigned get_active_lanes_in_pipeline()
        {
            active_mask_t active_lanes;
            active_lanes.reset();
            for( unsigned stage=0; (stage+1)<m_pipeline_depth; stage++ ){
                if( !m_pipeline_reg[stage]->empty() )
                    active_lanes|=m_pipeline_reg[stage]->get_active_mask();
            }
            return active_lanes.count();
        }
        virtual void active_lanes_in_pipeline() = 0;
        // accessors
        virtual bool stallable() const { return false; }
        virtual bool can_issue( const warp_inst_t &inst ) const
        {
            return simd_function_unit::can_issue(inst);
        }
        virtual void print(FILE *fp) const
        {
            simd_function_unit::print(fp);
            for( int s=m_pipeline_depth-1; s>=0; s-- ) {
                if( !m_pipeline_reg[s]->empty() ) { 
                    fprintf(fp,"      %s[%2d] ", m_name.c_str(), s );
                    m_pipeline_reg[s]->print(fp);
                }
            }
        }
    protected:
        unsigned m_pipeline_depth;
        warp_inst_t **m_pipeline_reg;
        register_set *m_result_port;
        class shader_core_ctx *m_core;
    };
    
    class sfu : public pipelined_simd_unit
    {
    public:
        sfu( register_set* result_port, const shader_core_config *config, shader_core_ctx *core );
        virtual bool can_issue( const warp_inst_t &inst ) const
        {
            switch(inst.op) {
            case SFU_OP: break;
            case ALU_SFU_OP: break;
            default: return false;
            }
            return pipelined_simd_unit::can_issue(inst);
        }
        virtual void active_lanes_in_pipeline();
        virtual void issue(  register_set& source_reg );
    };
    
    class sp_unit : public pipelined_simd_unit
    {
    public:
        sp_unit( register_set* result_port, const shader_core_config *config, shader_core_ctx *core );
        virtual bool can_issue( const warp_inst_t &inst ) const
        {
            switch(inst.op) {
            case SFU_OP: return false; 
            case LOAD_OP: return false;
            case STORE_OP: return false;
            case MEMORY_BARRIER_OP: return false;
            default: break;
            }
            return pipelined_simd_unit::can_issue(inst);
        }
        virtual void active_lanes_in_pipeline();
        virtual void issue( register_set& source_reg );
    };
    

    void sp_unit::active_lanes_in_pipeline(){
        unsigned active_count=pipelined_simd_unit::get_active_lanes_in_pipeline();
        assert(active_count<=m_core->get_config()->warp_size);
        m_core->incspactivelanes_stat(active_count);
        m_core->incfuactivelanes_stat(active_count);
        m_core->incfumemactivelanes_stat(active_count);
    }
    
    void sfu::active_lanes_in_pipeline(){
        unsigned active_count=pipelined_simd_unit::get_active_lanes_in_pipeline();
        assert(active_count<=m_core->get_config()->warp_size);
        m_core->incsfuactivelanes_stat(active_count);
        m_core->incfuactivelanes_stat(active_count);
        m_core->incfumemactivelanes_stat(active_count);
    }
    
    sp_unit::sp_unit( register_set* result_port, const shader_core_config *config,shader_core_ctx *core)
        : pipelined_simd_unit(result_port,config,config->max_sp_latency,core)
    { 
        m_name = "SP "; 
    }
    
    void sp_unit :: issue(register_set& source_reg)
    {
        warp_inst_t** ready_reg = source_reg.get_ready();
        //m_core->incexecstat((*ready_reg));
        (*ready_reg)->op_pipe=SP__OP;
        m_core->incsp_stat(m_core->get_config()->warp_size,(*ready_reg)->latency);
        pipelined_simd_unit::issue(source_reg);
    }
    
    
    pipelined_simd_unit::pipelined_simd_unit( register_set* result_port, const shader_core_config *config, unsigned max_latency,shader_core_ctx *core )
        : simd_function_unit(config) 
    {
        m_result_port = result_port;
        m_pipeline_depth = max_latency;
        m_pipeline_reg = new warp_inst_t*[m_pipeline_depth];
        for( unsigned i=0; i < m_pipeline_depth; i++ ) 
        m_pipeline_reg[i] = new warp_inst_t( config );
        m_core=core;
    }
    
    void pipelined_simd_unit::cycle()
    {
        if( !m_pipeline_reg[0]->empty() ){
            m_result_port->move_in(m_pipeline_reg[0]);
        }
        for( unsigned stage=0; (stage+1)<m_pipeline_depth; stage++ )
            move_warp(m_pipeline_reg[stage], m_pipeline_reg[stage+1]);
        if( !m_dispatch_reg->empty() ) {
            if( !m_dispatch_reg->dispatch_delay()){
                int start_stage = m_dispatch_reg->latency - m_dispatch_reg->initiation_interval;
                move_warp(m_pipeline_reg[start_stage],m_dispatch_reg);
            }
        }
        occupied >>=1;
    }
    
    
    void pipelined_simd_unit::issue( register_set& source_reg )
    {
        //move_warp(m_dispatch_reg,source_reg);
        warp_inst_t** ready_reg = source_reg.get_ready();
        m_core->incexecstat((*ready_reg));
        //source_reg.move_out_to(m_dispatch_reg);
        simd_function_unit::issue(source_reg);
    }
    
    
    
    simd_function_unit::simd_function_unit( const shader_core_config *config )
    { 
        m_config=config;
        m_dispatch_reg = new warp_inst_t(config); 
    }
    
    
    sfu:: sfu(  register_set* result_port, const shader_core_config *config,shader_core_ctx *core  )
        : pipelined_simd_unit(result_port,config,config->max_sfu_latency,core)
    { 
        m_name = "SFU"; 
    }
    
    void sfu::issue( register_set& source_reg )
    {
        warp_inst_t** ready_reg = source_reg.get_ready();
        //m_core->incexecstat((*ready_reg));
    
        (*ready_reg)->op_pipe=SFU__OP;
        m_core->incsfu_stat(m_core->get_config()->warp_size,(*ready_reg)->latency);
        pipelined_simd_unit::issue(source_reg);
    }