package pipeline;
public class ldst_unit extends GPUpipeline{
public
    ldst_unit( mem_fetch_interface *icnt,
               shader_core_mem_fetch_allocator *mf_allocator,
               shader_core_ctx *core, 
               opndcoll_rfu_t *operand_collector,
               Scoreboard *scoreboard,
               const shader_core_config *config, 
               const memory_config *mem_config,  
               class shader_core_stats *stats, 
               unsigned sid, unsigned tpc )
    {
        init( icnt,
        mf_allocator,
        core, 
        operand_collector,
        scoreboard,
        config, 
        mem_config,  
        stats, 
        sid,
        tpc );
  if( !m_config->m_L1D_config.disabled() ) {
      char L1D_name[STRSIZE];
      snprintf(L1D_name, STRSIZE, "L1D_%03d", m_sid);
      m_L1D = new l1_cache( L1D_name,
                            m_config->m_L1D_config,
                            m_sid,
                            get_shader_normal_cache_id(),
                            m_icnt,
                            m_mf_allocator,
                            IN_L1D_MISS_QUEUE );
  }

  }

    // modifiers
    virtual void issue( register_set &inst )
    {
        warp_inst_t* inst = *(reg_set.get_ready());

        // record how many pending register writes/memory accesses there are for this instruction
        assert(inst->empty() == false);
        if (inst->is_load() and inst->space.get_type() != shared_space) {
           unsigned warp_id = inst->warp_id();
           unsigned n_accesses = inst->accessq_count();
           for (unsigned r = 0; r < 4; r++) {
              unsigned reg_id = inst->out[r];
              if (reg_id > 0) {
                 m_pending_writes[warp_id][reg_id] += n_accesses;
              }
           }


	inst->op_pipe=MEM__OP;
	// stat collection
	m_core->mem_instruction_stats(*inst);
	m_core->incmem_stat(m_core->get_config()->warp_size,1);
	pipelined_simd_unit::issue(reg_set);

    }
    virtual void cycle()
    {
        writeback();
        m_operand_collector->step();
        for( unsigned stage=0; (stage+1)<m_pipeline_depth; stage++ ) 
            if( m_pipeline_reg[stage]->empty() && !m_pipeline_reg[stage+1]->empty() )
                 move_warp(m_pipeline_reg[stage], m_pipeline_reg[stage+1]);
     
        if( !m_response_fifo.empty() ) {
            mem_fetch *mf = m_response_fifo.front();
            if (mf->istexture()) {
                if (m_L1T->fill_port_free()) {
                    m_L1T->fill(mf,gpu_sim_cycle+gpu_tot_sim_cycle);
                    m_response_fifo.pop_front(); 
                }
            } else if (mf->isconst())  {
                if (m_L1C->fill_port_free()) {
                    mf->set_status(IN_SHADER_FETCHED,gpu_sim_cycle+gpu_tot_sim_cycle);
                    m_L1C->fill(mf,gpu_sim_cycle+gpu_tot_sim_cycle);
                    m_response_fifo.pop_front(); 
                }
            } else {
                if( mf->get_type() == WRITE_ACK || ( m_config->gpgpu_perfect_mem && mf->get_is_write() )) {
                    m_core->store_ack(mf);
                    m_response_fifo.pop_front();
                    delete mf;
                } else {
                    assert( !mf->get_is_write() ); // L1 cache is write evict, allocate line on load miss only
     
                    bool bypassL1D = false; 
                    if ( CACHE_GLOBAL == mf->get_inst().cache_op || (m_L1D == NULL) ) {
                        bypassL1D = true; 
                    } else if (mf->get_access_type() == GLOBAL_ACC_R || mf->get_access_type() == GLOBAL_ACC_W) { // global memory access 
                        if (m_core->get_config()->gmem_skip_L1D)
                            bypassL1D = true; 
                    }
                    if( bypassL1D ) {
                        if ( m_next_global == NULL ) {
                            mf->set_status(IN_SHADER_FETCHED,gpu_sim_cycle+gpu_tot_sim_cycle);
                            m_response_fifo.pop_front();
                            m_next_global = mf;
                        }
                    } else {
                        if (m_L1D->fill_port_free()) {
                            m_L1D->fill(mf,gpu_sim_cycle+gpu_tot_sim_cycle);
                            m_response_fifo.pop_front();
                        }
                    }
                }
            }
        }
     
        m_L1T->cycle();
        m_L1C->cycle();
        if( m_L1D ) m_L1D->cycle();
     
        warp_inst_t &pipe_reg = *m_dispatch_reg;
        enum mem_stage_stall_type rc_fail = NO_RC_FAIL;
        mem_stage_access_type type;
        bool done = true;
        done &= shared_cycle(pipe_reg, rc_fail, type);
        done &= constant_cycle(pipe_reg, rc_fail, type);
        done &= texture_cycle(pipe_reg, rc_fail, type);
        done &= memory_cycle(pipe_reg, rc_fail, type);
        m_mem_rc = rc_fail;
     
        if (!done) { // log stall types and return
           assert(rc_fail != NO_RC_FAIL);
           m_stats->gpgpu_n_stall_shd_mem++;
           m_stats->gpu_stall_shd_mem_breakdown[type][rc_fail]++;
           return;
        }
     
        if( !pipe_reg.empty() ) {
            unsigned warp_id = pipe_reg.warp_id();
            if( pipe_reg.is_load() ) {
                if( pipe_reg.space.get_type() == shared_space ) {
                    if( m_pipeline_reg[2]->empty() ) {
                        // new shared memory request
                        move_warp(m_pipeline_reg[2],m_dispatch_reg);
                        m_dispatch_reg->clear();
                    }
                } else {
                    //if( pipe_reg.active_count() > 0 ) {
                    //    if( !m_operand_collector->writeback(pipe_reg) ) 
                    //        return;
                    //} 
     
                    bool pending_requests=false;
                    for( unsigned r=0; r<4; r++ ) {
                        unsigned reg_id = pipe_reg.out[r];
                        if( reg_id > 0 ) {
                            if( m_pending_writes[warp_id].find(reg_id) != m_pending_writes[warp_id].end() ) {
                                if ( m_pending_writes[warp_id][reg_id] > 0 ) {
                                    pending_requests=true;
                                    break;
                                } else {
                                    // this instruction is done already
                                    m_pending_writes[warp_id].erase(reg_id); 
                                }
                            }
                        }
                    }
                    if( !pending_requests ) {
                        m_core->warp_inst_complete(*m_dispatch_reg);
                        m_scoreboard->releaseRegisters(m_dispatch_reg);
                    }
                    m_core->dec_inst_in_pipeline(warp_id);
                    m_dispatch_reg->clear();
                }
            } else {
                // stores exit pipeline here
                m_core->dec_inst_in_pipeline(warp_id);
                m_core->warp_inst_complete(*m_dispatch_reg);
                m_dispatch_reg->clear();
            }
    }
     
    void fill( mem_fetch *mf )
    {
        mf->set_status(IN_SHADER_LDST_RESPONSE_FIFO,gpu_sim_cycle+gpu_tot_sim_cycle);
        m_response_fifo.push_back(mf);

    }
    void flush()
    {
        m_L1D->flush();
    }
    void writeback();
    {
    // process next instruction that is going to writeback
    if( !m_next_wb.empty() ) {
        if( m_operand_collector->writeback(m_next_wb) ) {
            bool insn_completed = false; 
            for( unsigned r=0; r < 4; r++ ) {
                if( m_next_wb.out[r] > 0 ) {
                    if( m_next_wb.space.get_type() != shared_space ) {
                        assert( m_pending_writes[m_next_wb.warp_id()][m_next_wb.out[r]] > 0 );
                        unsigned still_pending = --m_pending_writes[m_next_wb.warp_id()][m_next_wb.out[r]];
                        if( !still_pending ) {
                            m_pending_writes[m_next_wb.warp_id()].erase(m_next_wb.out[r]);
                            m_scoreboard->releaseRegister( m_next_wb.warp_id(), m_next_wb.out[r] );
                            insn_completed = true; 
                        }
                    } else { // shared 
                        m_scoreboard->releaseRegister( m_next_wb.warp_id(), m_next_wb.out[r] );
                        insn_completed = true; 
                    }
                }
            }
            if( insn_completed ) {
                m_core->warp_inst_complete(m_next_wb);
            }
            m_next_wb.clear();
            m_last_inst_gpu_sim_cycle = gpu_sim_cycle;
            m_last_inst_gpu_tot_sim_cycle = gpu_tot_sim_cycle;
        }
    }

    unsigned serviced_client = -1; 
    for( unsigned c = 0; m_next_wb.empty() && (c < m_num_writeback_clients); c++ ) {
        unsigned next_client = (c+m_writeback_arb)%m_num_writeback_clients;
        switch( next_client ) {
        case 0: // shared memory 
            if( !m_pipeline_reg[0]->empty() ) {
                m_next_wb = *m_pipeline_reg[0];
                if(m_next_wb.isatomic()) {
                    m_next_wb.do_atomic();
                    m_core->decrement_atomic_count(m_next_wb.warp_id(), m_next_wb.active_count());
                }
                m_core->dec_inst_in_pipeline(m_pipeline_reg[0]->warp_id());
                m_pipeline_reg[0]->clear();
                serviced_client = next_client; 
            }
            break;
        case 1: // texture response
            if( m_L1T->access_ready() ) {
                mem_fetch *mf = m_L1T->next_access();
                m_next_wb = mf->get_inst();
                delete mf;
                serviced_client = next_client; 
            }
            break;
        case 2: // const cache response
            if( m_L1C->access_ready() ) {
                mem_fetch *mf = m_L1C->next_access();
                m_next_wb = mf->get_inst();
                delete mf;
                serviced_client = next_client; 
            }
            break;
        case 3: // global/local
            if( m_next_global ) {
                m_next_wb = m_next_global->get_inst();
                if( m_next_global->isatomic() ) 
                    m_core->decrement_atomic_count(m_next_global->get_wid(),m_next_global->get_access_warp_mask().count());
                delete m_next_global;
                m_next_global = NULL;
                serviced_client = next_client; 
            }
            break;
        case 4: 
            if( m_L1D && m_L1D->access_ready() ) {
                mem_fetch *mf = m_L1D->next_access();
                m_next_wb = mf->get_inst();
                delete mf;
                serviced_client = next_client; 
            }
            break;
        default: abort();
        }
    }
    // update arbitration priority only if: 
    // 1. the writeback buffer was available 
    // 2. a client was serviced 
    if (serviced_client != (unsigned)-1) {
        m_writeback_arb = (serviced_client + 1) % m_num_writeback_clients; 
    }


    }

    // accessors
    virtual unsigned clock_multiplier() const;

    virtual bool can_issue( const warp_inst_t &inst ) const
    {
        switch(inst.op) {
        case LOAD_OP: break;
        case STORE_OP: break;
        case MEMORY_BARRIER_OP: break;
        default: return false;
        }
        return m_dispatch_reg->empty();
    }

    virtual void active_lanes_in_pipeline();
    virtual bool stallable() const { return true; }
    bool response_buffer_full() const;
    // void print(FILE *fout) const;
    // void print_cache_stats( FILE *fp, unsigned& dl1_accesses, unsigned& dl1_misses );
    // void get_cache_stats(unsigned &read_accesses, unsigned &write_accesses, unsigned &read_misses, unsigned &write_misses, unsigned cache_type);
    // void get_cache_stats(cache_stats &cs);

    // void get_L1D_sub_stats(struct cache_sub_stats &css) const;
    // void get_L1C_sub_stats(struct cache_sub_stats &css) const;
    // void get_L1T_sub_stats(struct cache_sub_stats &css) const;

protected:
    void ldst_unit( mem_fetch_interface *icnt,
               shader_core_mem_fetch_allocator *mf_allocator,
               shader_core_ctx *core, 
               opndcoll_rfu_t *operand_collector,
               Scoreboard *scoreboard,
               const shader_core_config *config,
               const memory_config *mem_config,  
               shader_core_stats *stats,
               unsigned sid,
               unsigned tpc,
               l1_cache* new_l1d_cache )
    {

        init( icnt,
        mf_allocator,
        core, 
        operand_collector,
        scoreboard,
        config, 
        mem_config,  
        stats, 
        sid,
        tpc );
  if( !m_config->m_L1D_config.disabled() ) {
      char L1D_name[STRSIZE];
      snprintf(L1D_name, STRSIZE, "L1D_%03d", m_sid);
      m_L1D = new l1_cache( L1D_name,
                            m_config->m_L1D_config,
                            m_sid,
                            get_shader_normal_cache_id(),
                            m_icnt,
                            m_mf_allocator,
                            IN_L1D_MISS_QUEUE );
  }
   }
    void init( mem_fetch_interface *icnt,
               shader_core_mem_fetch_allocator *mf_allocator,
               shader_core_ctx *core, 
               opndcoll_rfu_t *operand_collector,
               Scoreboard *scoreboard,
               const shader_core_config *config,
               const memory_config *mem_config,  
               shader_core_stats *stats,
               unsigned sid,
               unsigned tpc )
               {
               m_memory_config = mem_config;
                m_icnt = icnt;
                m_mf_allocator=mf_allocator;
                m_core = core;
                m_operand_collector = operand_collector;
                m_scoreboard = scoreboard;
                m_stats = stats;
                m_sid = sid;
                m_tpc = tpc;
                #define STRSIZE 1024
                char L1T_name[STRSIZE];
                char L1C_name[STRSIZE];
                snprintf(L1T_name, STRSIZE, "L1T_%03d", m_sid);
                snprintf(L1C_name, STRSIZE, "L1C_%03d", m_sid);
                m_L1T = new tex_cache(L1T_name,m_config->m_L1T_config,m_sid,get_shader_texture_cache_id(),icnt,IN_L1T_MISS_QUEUE,IN_SHADER_L1T_ROB);
                m_L1C = new read_only_cache(L1C_name,m_config->m_L1C_config,m_sid,get_shader_constant_cache_id(),icnt,IN_L1C_MISS_QUEUE);
                m_L1D = NULL;
                m_mem_rc = NO_RC_FAIL;
                m_num_writeback_clients=5; // = shared memory, global/local (uncached), L1D, L1T, L1C
                m_writeback_arb = 0;
                m_next_global=NULL;
                m_last_inst_gpu_sim_cycle=0;
                m_last_inst_gpu_tot_sim_cycle=0;
         }
protected:
   bool shared_cycle( warp_inst_t &inst, mem_stage_stall_type &rc_fail, mem_stage_access_type &fail_type)
   {






   }
   bool constant_cycle( warp_inst_t &inst, mem_stage_stall_type &rc_fail, mem_stage_access_type &fail_type)
   {
            if( inst.empty() || ((inst.space.get_type() != const_space) && (inst.space.get_type() != param_space_kernel)) )
            return true;
        if( inst.active_count() == 0 ) 
            return true;
        mem_stage_stall_type fail = process_memory_access_queue(m_L1C,inst);
        if (fail != NO_RC_FAIL){ 
        rc_fail = fail; //keep other fails if this didn't fail.
        fail_type = C_MEM;
        if (rc_fail == BK_CONF or rc_fail == COAL_STALL) {
            m_stats->gpgpu_n_cmem_portconflict++; //coal stalls aren't really a bank conflict, but this maintains previous behavior.
        }
        }
        return inst.accessq_empty(); //done if empty.
   }
   bool texture_cycle( warp_inst_t &inst, mem_stage_stall_type &rc_fail, mem_stage_access_type &fail_type)
   {
    if( inst.empty() || inst.space.get_type() != tex_space )
    return true;
if( inst.active_count() == 0 ) 
    return true;
mem_stage_stall_type fail = process_memory_access_queue(m_L1T,inst);
if (fail != NO_RC_FAIL){ 
   rc_fail = fail; //keep other fails if this didn't fail.
   fail_type = T_MEM;
}
return inst.accessq_empty(); //done if empty.
   }
   bool memory_cycle( warp_inst_t &inst, mem_stage_stall_type &rc_fail, mem_stage_access_type &fail_type)
   {
   if( inst.empty() || 
       ((inst.space.get_type() != global_space) &&
        (inst.space.get_type() != local_space) &&
        (inst.space.get_type() != param_space_local)) ) 
       return true;
   if( inst.active_count() == 0 ) 
       return true;
   assert( !inst.accessq_empty() );
   mem_stage_stall_type stall_cond = NO_RC_FAIL;
   const mem_access_t &access = inst.accessq_back();

   bool bypassL1D = false; 
   if ( CACHE_GLOBAL == inst.cache_op || (m_L1D == NULL) ) {
       bypassL1D = true; 
   } else if (inst.space.is_global()) { // global memory access 
       // skip L1 cache if the option is enabled
       if (m_core->get_config()->gmem_skip_L1D) 
           bypassL1D = true; 
   }

   if( bypassL1D ) {
       // bypass L1 cache
       unsigned control_size = inst.is_store() ? WRITE_PACKET_SIZE : READ_PACKET_SIZE;
       unsigned size = access.get_size() + control_size;
       if( m_icnt->full(size, inst.is_store() || inst.isatomic()) ) {
           stall_cond = ICNT_RC_FAIL;
       } else {
           mem_fetch *mf = m_mf_allocator->alloc(inst,access);
           m_icnt->push(mf);
           inst.accessq_pop_back();
           //inst.clear_active( access.get_warp_mask() );
           if( inst.is_load() ) { 
              for( unsigned r=0; r < 4; r++) 
                  if(inst.out[r] > 0) 
                      assert( m_pending_writes[inst.warp_id()][inst.out[r]] > 0 );
           } else if( inst.is_store() ) 
              m_core->inc_store_req( inst.warp_id() );
       }
   } else {
       assert( CACHE_UNDEFINED != inst.cache_op );
       stall_cond = process_memory_access_queue(m_L1D,inst);
   }
   if( !inst.accessq_empty() ) 
       stall_cond = COAL_STALL;
   if (stall_cond != NO_RC_FAIL) {
      stall_reason = stall_cond;
      bool iswrite = inst.is_store();
      if (inst.space.is_local()) 
         access_type = (iswrite)?L_MEM_ST:L_MEM_LD;
      else 
         access_type = (iswrite)?G_MEM_ST:G_MEM_LD;
   }
   return inst.accessq_empty(); 
 }

   virtual mem_stage_stall_type process_cache_access( cache_t* cache,   new_addr_type address,    warp_inst_t &inst,
                                                      std::list<cache_event>& events,  mem_fetch *mf,   enum cache_request_status status)
{
    mem_stage_stall_type result = NO_RC_FAIL;
    bool write_sent = was_write_sent(events);
    bool read_sent = was_read_sent(events);
    if( write_sent ) 
        m_core->inc_store_req( inst.warp_id() );
    if ( status == HIT ) {
        assert( !read_sent );
        inst.accessq_pop_back();
        if ( inst.is_load() ) {
            for ( unsigned r=0; r < 4; r++)
                if (inst.out[r] > 0)
                    m_pending_writes[inst.warp_id()][inst.out[r]]--; 
        }
        if( !write_sent ) 
            delete mf;
    } else if ( status == RESERVATION_FAIL ) {
        result = COAL_STALL;
        assert( !read_sent );
        assert( !write_sent );
        delete mf;
    } else {
        assert( status == MISS || status == HIT_RESERVED );
        //inst.clear_active( access.get_warp_mask() ); // threads in mf writeback when mf returns
        inst.accessq_pop_back();
    }
    if( !inst.accessq_empty() )
        result = BK_CONF;
    return result;
}
   mem_stage_stall_type process_memory_access_queue( cache_t *cache, warp_inst_t &inst )
   {
    mem_stage_stall_type result = NO_RC_FAIL;
    if( inst.accessq_empty() )
        return result;

    if( !cache->data_port_free() ) 
        return DATA_PORT_STALL; 

    //const mem_access_t &access = inst.accessq_back();
    mem_fetch *mf = m_mf_allocator->alloc(inst,inst.accessq_back());
    std::list<cache_event> events;
    enum cache_request_status status = cache->access(mf->get_addr(),mf,gpu_sim_cycle+gpu_tot_sim_cycle,events);
    return process_cache_access( cache, mf->get_addr(), inst, events, mf, status );
       }

   const memory_config *m_memory_config;
   class mem_fetch_interface *m_icnt;
   shader_core_mem_fetch_allocator *m_mf_allocator;
   class shader_core_ctx *m_core;
   unsigned m_sid;
   unsigned m_tpc;

   tex_cache *m_L1T; // texture cache
   read_only_cache *m_L1C; // constant cache
   l1_cache *m_L1D; // data cache
   std::map<unsigned/*warp_id*/, std::map<unsigned/*regnum*/,unsigned/*count*/> > m_pending_writes;
   std::list<mem_fetch*> m_response_fifo;
   opndcoll_rfu_t *m_operand_collector;
   Scoreboard *m_scoreboard;

   mem_fetch *m_next_global;
   warp_inst_t m_next_wb;
   unsigned m_writeback_arb; // round-robin arbiter for writeback contention between L1T, L1C, shared
   unsigned m_num_writeback_clients;

   enum mem_stage_stall_type m_mem_rc;

   shader_core_stats *m_stats; 

   // for debugging
   unsigned long long m_last_inst_gpu_sim_cycle;
   unsigned long long m_last_inst_gpu_tot_sim_cycle;
};

enum pipeline_stage_name_t {
    ID_OC_SP=0,
    ID_OC_SFU,  
    ID_OC_MEM,  
    OC_EX_SP,
    OC_EX_SFU,
    OC_EX_MEM,
    EX_WB,
    N_PIPELINE_STAGES 
};

const char* const pipeline_stage_name_decode[] = {
    "ID_OC_SP",
    "ID_OC_SFU",  
    "ID_OC_MEM",  
    "OC_EX_SP",
    "OC_EX_SFU",
    "OC_EX_MEM",
    "EX_WB",
    "N_PIPELINE_STAGES" 
};