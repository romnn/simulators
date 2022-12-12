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

INCLUDES  := -I../Common
LIBRARIES :=

# lib/gcc-$CC_VERSION/cuda-$CUDA_VERSION_NUMBER/release/

native: matrixMul.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) --std=c++11 $(GENCODE_FLAGS) -o matrixMul.o -c matrixMul.cu
	$(NVCC) $(ALL_LDFLAGS) $(GENCODE_FLAGS) -o matrixMul matrixMul.o $(LIBRARIES)
	
m2s: matrixMul.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) -m32 -Xcicc -O0 -Xptxas -O0 $(GENCODE_FLAGS) -arch sm_35 -o matrixMul.o -c matrixMul.cu
	$(HOST_COMPILER) $(ALL_LDFLAGS) -L$(M2S_ROOT)/lib/.libs -o matrixMul matrixMul.o -m32 $(LIBRARIES) -lm2s-cuda 
	
accelsim: matrixMul.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) --std=c++11 $(GENCODE_FLAGS) -o matrixMul.o -c matrixMul.cu
	$(NVCC) $(ALL_LDFLAGS) --cudart shared $(GENCODE_FLAGS) -o matrixMul matrixMul.o $(LIBRARIES)

# NOTE: tejas and macsim (CUDA 4.2) does not support non blocking streams etc.
tejas: matrixMul.cu

macsim: matrixMul.cu
	# $(NVCC) $(INCLUDES) -I$(CUDA_PATH)/include  -L$(CUDA_PATH)/lib64 $(ALL_CCFLAGS) $(GENCODE_FLAGS) -arch=sm_20 -o matrixMul.o -c matrixMul.cu
	# g++-4.4 -o matrixMul matrixMul.o -locelot -locelotTrace
	# python3 ../get-kernel-info.py $(NVCC) $(INCLUDES) $(ALL_CCFLAGS) $(GENCODE_FLAGS) -arch=sm_20 matrixMul.cu

clean:
	rm -f matrixMul matrixMul.o

clobber: clean
