#include <ocelot/api/interface/ocelot.h>
#include <ocelot/trace/interface/TraceGenerator.h>
#include <ocelot/trace/interface/TraceEvent.h>
#include <ocelot/ir/interface/Instruction.h>
#include <ocelot/executive/interface/EmulatedKernel.h>

#include <iostream>
#include <vector>
#include <cstdint>
#include <cstdio>
#include <bitset>
#include <fstream>
#include <cstdlib>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/shm.h>
#include <cstring>
#include <cstdlib>

#include <sched.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <time.h>
#include <sys/timeb.h>

#define TYPE_BRANCH 0
#define TYPE_MEM 1
#define TYPE_IP 2

using namespace std;

int32_t last_block_id=-1;
int no_of_blocks = 0;

char file_name[50];

int MAX_THREADS =0;

fstream file;
ofstream temp_file;
int  gridDimX;

int next_file = 0;

class TraceGenerator : public trace::TraceGenerator
{
        public:

                // Called when a traced kernel is launched to retrieve some parameters from the kernel
                void initialize(const executive::ExecutableKernel& kernel);

                // Ocelot calls this function every time a new instruction is encountered
                void event(const trace::TraceEvent & event);

                //a new thread in a warp has started
                void BlockStart();

                //on finishing of a thread IN A WARP
                void BlockFini();

                // This function is called when the app ends
                void finish();
};

//this is kernel start function we must send a packet to the feeder indicating that a new kenel has started alongwith timing info
void TraceGenerator::initialize(const executive::ExecutableKernel& kernel)
{
	last_block_id = -1;
	next_file = 0;
	no_of_blocks = kernel.gridDim().x * kernel.gridDim().y * kernel.gridDim().z;
	gridDimX = kernel.gridDim().x;

	for(int i =0; i< MAX_THREADS; i++)
	{
		sprintf(file_name, "%d.txt",i);
		file.open(file_name, ios::in|ios::out|ios::ate|ios::app);
		file << "KERNEL START "<<no_of_blocks<< endl;
		file.close();
	}
}
void TraceGenerator::event(const trace::TraceEvent & event)
{

        int cur_block = event.blockId.y * gridDimX + event.blockId.x;
        std::string instruction=event.instruction->toString();
        std::replace( instruction.begin(), instruction.end(), ' ', '\t');

        if (last_block_id != cur_block)
        {
                if(last_block_id != -1)
                {
                        BlockFini();
                }
		
                BlockStart();
		        last_block_id = cur_block;
        }

        //IF IT IS A BRANCH INSTRUCTION
        if(event.instruction->isBranch())
        {
		        temp_file << TYPE_BRANCH <<"&"<< event.PC <<"&"<< instruction << endl;
        }

        else if(event.instruction->isLoad())
        {
                temp_file << TYPE_MEM <<"&"<< event.PC <<"&"<< instruction;
		        for(int i=0; i<event.active.size(); i++)
		        {
			        temp_file << "&" << event.memory_addresses[i];
		        }
		        temp_file << endl;
        }
	    else if(event.instruction->isStore())
	    {
		    temp_file << TYPE_MEM <<"&"<< event.PC <<"&"<< instruction;
		    for(int i=0; i<event.active.size(); i++)
		    {
			    temp_file << "&" << event.memory_addresses[i];
		    }
		    temp_file << endl;
	    }
        else 
        {
               temp_file << TYPE_IP <<"&"<< event.PC <<"&"<< instruction<<endl;
        }
}

void TraceGenerator::BlockStart(){
        temp_file.open("temp.txt");
}



void TraceGenerator::BlockFini() {

	temp_file << "BLOCK END"<<endl;
	temp_file.clear();
	long block_size = temp_file.tellp();
	temp_file.close();

	sprintf(file_name,"%d.txt",next_file);
	file.open(file_name, ios::in|ios::out|ios::ate|ios::app);

	int last_written = file.tellp();

	file << (block_size) << endl;

	ifstream input("temp.txt");
	string str;
	while(getline(input, str))
	{
		file<<str<<endl;
	}

	input.close();
	file.close();
	next_file = (next_file+1) % MAX_THREADS;
}



// This function is called when the kernel ends
void TraceGenerator::finish() {
	temp_file << "BLOCK END"<<endl;
	temp_file.clear();
	long block_size = temp_file.tellp();
	temp_file.close();

	sprintf(file_name,"%d.txt",next_file);
	file.open(file_name, ios::in|ios::out|ios::ate|ios::app);

	file << (block_size) << endl;

	ifstream input("temp.txt");
	string str;
	while(getline(input, str))
	{
		file<<str<<endl;
	}

	input.close();
	file.close();
	next_file = (next_file+1) % MAX_THREADS;
}

/* ===================================================================== */
/* Main                                                                  */
/* ===================================================================== */
extern int GPUTejas_main(int argc, char** argv);
int main(int argc, char** argv)
{
	MAX_THREADS = atoi(argv[argc -1]);
	// cout<<MAX_THREADS;
        TraceGenerator generator;
        ocelot::addTraceGenerator( generator );
        GPUTejas_main(argc-1,argv);
        exit(0);
}
