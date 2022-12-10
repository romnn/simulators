native:
	$(MAKE) $(MAKE_FLAGS) -C CUDA10-transpose native
	$(MAKE) $(MAKE_FLAGS) -C BabelStream native
	$(MAKE) $(MAKE_FLAGS) -C CUDA10-matrixMul native
	$(MAKE) $(MAKE_FLAGS) -C CUDA4-matrixMul native

accelsim:
	$(MAKE) $(MAKE_FLAGS) -C BabelStream accelsim
	$(MAKE) $(MAKE_FLAGS) -C CUDA10-transpose accelsim
	$(MAKE) $(MAKE_FLAGS) -C CUDA10-matrixMul accelsim
	$(MAKE) $(MAKE_FLAGS) -C CUDA4-matrixMul accelsim

m2s:
	$(MAKE) $(MAKE_FLAGS) -C BabelStream m2s
	$(MAKE) $(MAKE_FLAGS) -C CUDA4-matrixMul m2s

tejas:
	$(MAKE) $(MAKE_FLAGS) -C BabelStream tejas
	$(MAKE) $(MAKE_FLAGS) -C CUDA4-matrixMul tejas

clean:
	$(MAKE) $(MAKE_FLAGS) -C BabelStream clean
	$(MAKE) $(MAKE_FLAGS) -C CUDA10-transpose clean
	$(MAKE) $(MAKE_FLAGS) -C CUDA10-matrixMul clean
	$(MAKE) $(MAKE_FLAGS) -C CUDA4-matrixMul clean

