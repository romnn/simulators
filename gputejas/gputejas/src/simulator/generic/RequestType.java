/*
/*****************************************************************************
				GPUTejas Simulator
------------------------------------------------------------------------------------------------------------

   Copyright [2014] [Indian Institute of Technology, Delhi]
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
------------------------------------------------------------------------------------------------------------

	Contributors:  Seep Goel, Geetika Malhotra, Harinder Pal
*****************************************************************************/ 
package generic;

public enum RequestType {
	PERFORM_DECODE,
	DECODE_COMPLETE,
	ALLOC_DEST_REG,
	RENAME_COMPLETE,
	FUNC_UNIT_AVAILABLE,
	LOAD_ADDRESS_COMPUTED,
	ICACHE_EXEC_COMPLETE,
	EXEC_COMPLETE,
	WRITEBACK_ATTEMPT,
	WRITEBACK_COMPLETE,
	PERFORM_COMMITS,
	MISPRED_PENALTY_COMPLETE,
	BOOT_PIPELINE,
	BROADCAST,
	
	Column_Read_Complete,
	Tell_LSQ_Addr_Ready,
	Validate_LSQ_Addr,
	Attempt_L1_Issue,
	Cache_Read,
	Cache_Read_from_iCache,
	Cache_Write,
	Main_Mem_Read,
	Main_Mem_Write,
	Mem_Response,
	LSQ_Commit,
	Main_Mem_Access,
	//banked memory element's request types
	CacheBank_Read,
	CacheBank_Write,
	CacheBank_Read_from_iCache,
	MemBank_Response,
	Main_MemBank_Read,
	Main_MemBank_Write,
	Main_MemBank_Response,
	
	MESI_Invalidate,
	MESI_RWITM,
	Request_for_copy,
	Request_for_modified_copy,
	Reply_with_shared_copy,
	Write_Modified_to_sharedmem, 
	Main_Mem_Response,
	Mem_Cntrlr_State_Update,
	Rank_Response,
	
	TOKEN,
	LOCAL_TOKEN,
	PIPELINE_RESUME,
	PIPELINE_SLEEP,
	Migrate_Block,
	TREE_BARRIER,
	TREE_BARRIER_RELEASE,
	ReadMissDirectoryUpdate,
	WriteMissDirectoryUpdate,
	WriteHitDirectoryUpdate,
	EvictionDirectoryUpdate,
	Cache_Read_Writeback,
	Cache_Read_Writeback_Invalidate,
	MemResponseDirectoryUpdate,
	Send_Mem_Response,
	Cache_Write_Writeback_Invalidate,
	
	PerformPulls, 
	Send_Mem_Response_Invalidate,
	Invalid_Event, Cache_Hit, Cache_Miss,

	MSHR_Full,
	Tlb_Miss_Response, CacheLine_Invalidate, Send_Migrate_Block
	
	
	
	
}
