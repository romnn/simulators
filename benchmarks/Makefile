BENCHES = CUDA6-transpose
BENCHES += CUDA6-bandwidthTest
BENCHES += CUDA4-matrixMul
BENCHES += CUDA10-transpose
BENCHES += CUDA10-matrixMul
BENCHES += vectorAdd
BENCHES += BabelStream

# https://stackoverflow.com/a/23710719

TEJAS_BENCHES = $(BENCHES:%=tejas-%)
M2S_BENCHES = $(BENCHES:%=m2s-%)
MACSIM_BENCHES = $(BENCHES:%=macsim-%)
ACCELSIM_BENCHES = $(BENCHES:%=accelsim-%)
NATIVE_BENCHES = $(BENCHES:%=native-%)
CLEAN_BENCHES = $(BENCHES:%=clean-%)

native: $(NATIVE_BENCHES)
macsim: $(MACSIM_BENCHES)
m2s: $(M2S_BENCHES)
accelsim: $(ACCELSIM_BENCHES)
tejas: $(TEJAS_BENCHES)
clean: $(CLEAN_BENCHES)

$(NATIVE_BENCHES):
	+$(MAKE) $(MAKE_FLAGS) -C $(@:native-%=%) native

$(MACSIM_BENCHES):
	+$(MAKE) $(MAKE_FLAGS) -C $(@:macsim-%=%) macsim

$(M2S_BENCHES):
	+$(MAKE) $(MAKE_FLAGS) -C $(@:m2s-%=%) m2s

$(ACCELSIM_BENCHES):
	+$(MAKE) $(MAKE_FLAGS) -C $(@:accelsim-%=%) accelsim

$(TEJAS_BENCHES):
	+$(MAKE) $(MAKE_FLAGS) -C $(@:tejas-%=%) tejas

$(CLEAN_BENCHES):
	+$(MAKE) $(MAKE_FLAGS) -C $(@:clean-%=%) clean

nvbit-tracer-tool:
	rm -rf accel-sim-framework
	git clone https://github.com/accel-sim/accel-sim-framework.git
	curl -L -o ./accel-sim-framework/util/tracer_nvbit/nvbit.tar.bz2 https://github.com/NVlabs/NVBit/releases/download/1.5.5/nvbit-Linux-x86_64-1.5.5.tar.bz2
	tar -xf ./accel-sim-framework/util/tracer_nvbit/nvbit.tar.bz2 -C ./accel-sim-framework/util/tracer_nvbit/
	make -C ./accel-sim-framework/util/tracer_nvbit/

.PHONY: $(TEJAS_BENCHES)
.PHONY: $(ACCELSIM_BENCHES)
.PHONY: $(NATIVE_BENCHES)
.PHONY: $(M2S_BENCHES)
.PHONY: $(MACSIM_BENCHES)
.PHONY: $(CLEAN_BENCHES)
.PHONY: clean
