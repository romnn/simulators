# Location of the CUDA Toolkit
CUDA_PATH ?= /usr/local/cuda

HOST_COMPILER ?= g++
NVCC          := $(CUDA_PATH)/bin/nvcc -ccbin $(HOST_COMPILER)

NVCCFLAGS   := 
# -m${TARGET_SIZE}
CCFLAGS     :=
LDFLAGS     :=

# NVCCFLAGS += -g -G

ALL_CCFLAGS :=
ALL_CCFLAGS += $(NVCCFLAGS)
ALL_CCFLAGS += $(EXTRA_NVCCFLAGS)
ALL_CCFLAGS += $(addprefix -Xcompiler ,$(CCFLAGS))
ALL_CCFLAGS += $(addprefix -Xcompiler ,$(EXTRA_CCFLAGS))

ALL_LDFLAGS :=
ALL_LDFLAGS += $(ALL_CCFLAGS)
ALL_LDFLAGS += $(addprefix -Xlinker ,$(LDFLAGS))
ALL_LDFLAGS += $(addprefix -Xlinker ,$(EXTRA_LDFLAGS))

# Common includes and paths for CUDA
INCLUDES  := -I../Common
LIBRARIES :=

################################################################################

# Gencode arguments
# SMS ?= 35 37 50 52 60 61 70 75 80 86 90

# ifeq ($(GENCODE_FLAGS),)
# Generate SASS code for each SM architecture listed in $(SMS)
# $(foreach sm,$(SMS),$(eval GENCODE_FLAGS += -gencode arch=compute_$(sm),code=sm_$(sm)))

# Generate PTX code from the highest SM architecture in $(SMS) to guarantee forward-compatibility
# HIGHEST_SM := $(lastword $(sort $(SMS)))
# ifneq ($(HIGHEST_SM),)
# GENCODE_FLAGS += -gencode arch=compute_$(HIGHEST_SM),code=compute_$(HIGHEST_SM)
# endif
# endif

# ALL_CCFLAGS += --threads 0 --std=c++11
ALL_CCFLAGS += --std=c++11

################################################################################
# $(EXEC) $(NVCC) $(INCLUDES) $(ALL_CCFLAGS) $(GENCODE_FLAGS) -o $@ -c $<
# $(EXEC) mkdir -p ../../../bin/$(TARGET_ARCH)/$(TARGET_OS)/$(BUILD_TYPE)
# $(EXEC) cp $@ ../../../bin/$(TARGET_ARCH)/$(TARGET_OS)/$(BUILD_TYPE)

# $(EXEC) $(NVCC) $(ALL_LDFLAGS) $(GENCODE_FLAGS) -o $@ $+ $(LIBRARIES)
# rm -rf ../../../bin/$(TARGET_ARCH)/$(TARGET_OS)/$(BUILD_TYPE)/matrixMul

all: build

build: matrixMul

matrixMul.o: matrixMul.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) $(GENCODE_FLAGS) -o $@ -c $<

matrixMul: matrixMul.o
	$(NVCC) $(ALL_LDFLAGS) $(GENCODE_FLAGS) -o $@ $+ $(LIBRARIES)
	
run: build
	./matrixMul

testrun: build

clean:
	rm -f matrixMul matrixMul.o

clobber: clean
