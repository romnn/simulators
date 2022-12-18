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

INCLUDES  := 
LIBRARIES :=

TEJAS_LIBRARIES := "-L$(TEJAS_ROOT)/../so_files_64bit"
TEJAS_LIBRARIES += "-L$(TEJAS_ROOT)/../"
TEJAS_LIBRARIES += "-L$(CUDA_PATH)/lib64"
TEJAS_LIBRARIES += "-L/usr/lib/x86_64-linux-gnu/"

native: vectorAdd.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) --std=c++11 $(GENCODE_FLAGS) -o vectorAdd.o -c vectorAdd.cu
	$(NVCC) $(ALL_LDFLAGS) $(GENCODE_FLAGS) -o vectorAdd vectorAdd.o $(LIBRARIES)

m2s: vectorAdd.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) -m32 -Xcicc -O0 -Xptxas -O0 $(GENCODE_FLAGS) -arch sm_35 -o vectorAdd.o -c vectorAdd.cu
	$(HOST_COMPILER) $(ALL_LDFLAGS) -L$(M2S_ROOT)/lib/.libs -o vectorAdd vectorAdd.o -m32 $(LIBRARIES) -lm2s-cuda 

accelsim: vectorAdd.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) --std=c++11 $(GENCODE_FLAGS) -o vectorAdd.o -c vectorAdd.cu
	$(NVCC) $(ALL_LDFLAGS) --cudart shared $(GENCODE_FLAGS) -o vectorAdd vectorAdd.o $(LIBRARIES)

tejas: vectorAdd.cu
	python3 ../inject-main.py vectorAdd.cu vectorAddTejas.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) $(GENCODE_FLAGS) -arch=sm_20 -o vectorAdd.o -c vectorAddTejas.cu
	g++-4.8 -std=c++0x $(TEJAS_ROOT)/../Tracegen.cpp -c -I$(TEJAS_ROOT)/../
	g++-4.4 -o tracegen vectorAdd.o Tracegen.o $(TEJAS_LIBRARIES) -locelot -ltinfo -Wl,-rpath-link=$(TEJAS_ROOT)/../so_files_64bit

macsim: vectorAdd.cu
	$(NVCC) $(INCLUDES) $(ALL_CCFLAGS) $(GENCODE_FLAGS) -arch=sm_20 -o vectorAdd.o -c vectorAdd.cu
	g++-4.4 -o vectorAdd vectorAdd.o -locelot -locelotTrace
	python3 ../get-kernel-info.py $(NVCC) $(INCLUDES) $(ALL_CCFLAGS) $(GENCODE_FLAGS) -arch=sm_20 vectorAdd.cu

clean:
	rm -f vectorAdd vectorAdd.o vectorAddTejas.cu

clobber: clean


# nvcc -m32 -arch sm_20 -lm2s-cuda -O3 vectoradd.cu -o vectoradd.o

# nvcc -c vectoradd.cu -arch sm_35 -m32 -Xcicc -O0 -Xptxas -O0 -o vectoradd.o
# g++ vectoradd.o -o vectoradd -m32 -L/app/lib/.libs -lm2s-cuda

# nvcc -c vectorAdd.cu  -arch sm_35 -c -m32 -Xcicc -O0 -Xptxas -O0 -I../include -I../inc -o vectorAdd_disop.o
# g++   vectorAdd_disop.o -o vectorAdd_m2s -m32 -L ~/multi2sim/lib/.libs -lm2s-cuda

# m2s:
# 	nvcc -c vectoradd.cu -arch sm_35 -m32 -Xcicc -O0 -Xptxas -O0 -o vectoradd.o
# 	g++ vectoradd.o -o vectoradd -m32 -L/app/lib/.libs -lm2s-cuda

# m2s_old:
# 	nvcc -m32 -arch sm_20 -O3 vectoradd.cu -o vectoradd.o
# 	g++ -o vectoradd -m32 vectoradd.o -L/app/lib/.libs -static -lm2s-cuda

# all:
# 	nvcc -ccbin=$(CXX) \
# 		--cudart shared \
# 		-gencode=arch=compute_50,code=compute_50 \
# 		-gencode=arch=compute_50,code=sm_50 \
# 		-gencode=arch=compute_60,code=compute_60 \
# 		-gencode=arch=compute_60,code=sm_60 \
# 		-gencode=arch=compute_75,code=compute_75 \
# 		-gencode=arch=compute_75,code=sm_75 \
# 		-O3 vectoradd.cu \
# 		-o vectoradd

# clean:
# 	rm -f vectoradd
