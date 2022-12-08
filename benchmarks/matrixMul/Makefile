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
	
accelsim: matrixMul.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) --std=c++11 $(GENCODE_FLAGS) -o matrixMul.o -c matrixMul.cu
	$(NVCC) $(ALL_LDFLAGS) --cudart shared $(GENCODE_FLAGS) -o matrixMul matrixMul.o $(LIBRARIES)
	
# NOTE: TEJAS (CUDA 4.2) does not support non blocking streams etc.

clean:
	rm -f matrixMul matrixMul.o

clobber: clean
