
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
