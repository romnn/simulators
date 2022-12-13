CUDA_PATH ?= /usr/local/cuda

HOST_COMPILER ?= g++
NVCC          := $(CUDA_PATH)/bin/nvcc -ccbin $(HOST_COMPILER)

NVCCFLAGS   := 
CCFLAGS     :=
LDFLAGS     :=

ALL_CCFLAGS :=
ALL_CCFLAGS += $(NVCCFLAGS)
ALL_CCFLAGS += $(EXTRA_NVCCFLAGS)
ALL_CCFLAGS += $(addprefix -Xcompiler ,$(CCFLAGS))
ALL_CCFLAGS += $(addprefix -Xcompiler ,$(EXTRA_CCFLAGS))

ALL_LDFLAGS :=
ALL_LDFLAGS += $(ALL_CCFLAGS)
ALL_LDFLAGS += $(addprefix -Xlinker ,$(LDFLAGS))
ALL_LDFLAGS += $(addprefix -Xlinker ,$(EXTRA_LDFLAGS))

INCLUDES  := -I./include
LIBRARIES :=

TEJAS_LIBRARIES := "-L$(TEJAS_ROOT)/../so_files_64bit"
TEJAS_LIBRARIES += "-L$(TEJAS_ROOT)/../"
TEJAS_LIBRARIES += "-L$(CUDA_PATH)/lib64"
TEJAS_LIBRARIES += "-L/usr/lib/x86_64-linux-gnu/"

native: bandwidthTest.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) --std=c++11 $(GENCODE_FLAGS) -o bandwidthTest.o -c bandwidthTest.cu
	$(NVCC) $(ALL_LDFLAGS) $(GENCODE_FLAGS) -o bandwidthTest bandwidthTest.o $(LIBRARIES)

m2s: bandwidthTest.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) -m32 -Xcicc -O0 -Xptxas -O0 $(GENCODE_FLAGS) -arch sm_35 -o bandwidthTest.o -c bandwidthTest.cu
	$(HOST_COMPILER) $(ALL_LDFLAGS) -L$(M2S_ROOT)/lib/.libs -o bandwidthTest bandwidthTest.o -m32 $(LIBRARIES) -lm2s-cuda 

accelsim: bandwidthTest.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) --std=c++11 $(GENCODE_FLAGS) -o bandwidthTest.o -c bandwidthTest.cu
	$(NVCC) $(ALL_LDFLAGS) --cudart shared $(GENCODE_FLAGS) -o bandwidthTest bandwidthTest.o $(LIBRARIES)

tejas: bandwidthTest.cu
	python3 ../inject-main.py bandwidthTest.cu bandwidthTestTejas.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) $(GENCODE_FLAGS) -arch=sm_20 -o bandwidthTest.o -c bandwidthTestTejas.cu
	g++-4.8 -std=c++0x $(TEJAS_ROOT)/../Tracegen.cpp -c -I$(TEJAS_ROOT)/../
	g++-4.4 -o tracegen bandwidthTest.o Tracegen.o $(TEJAS_LIBRARIES) -locelot -ltinfo -Wl,-rpath-link=$(TEJAS_ROOT)/../so_files_64bit

macsim: bandwidthTest.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) $(GENCODE_FLAGS) -arch=sm_20 -o bandwidthTest.o -c bandwidthTest.cu
	g++-4.4 -o bandwidthTest bandwidthTest.o -locelot -locelotTrace
	python3 ../get-kernel-info.py $(NVCC) $(INCLUDES) $(ALL_CCFLAGS) $(GENCODE_FLAGS) -arch=sm_20 bandwidthTest.cu

clean:
	rm -f bandwidthTest bandwidthTest.o bandwidthTestTejas.cu

clobber: clean
