native:
	$(MAKE) $(MAKE_FLAGS) -C BabelStream native
	$(MAKE) $(MAKE_FLAGS) -C matrixMul native
	$(MAKE) $(MAKE_FLAGS) -C matrixMul-modified native

m2s:
	$(MAKE) $(MAKE_FLAGS) -C BabelStream m2s
	$(MAKE) $(MAKE_FLAGS) -C matrixMul-modified m2s

accelsim:
	$(MAKE) $(MAKE_FLAGS) -C BabelStream accelsim
	$(MAKE) $(MAKE_FLAGS) -C matrixMul accelsim
	$(MAKE) $(MAKE_FLAGS) -C matrixMul-modified accelsim

tejas:
	$(MAKE) $(MAKE_FLAGS) -C BabelStream tejas
	$(MAKE) $(MAKE_FLAGS) -C matrixMul-modified tejas

clean:
	$(MAKE) $(MAKE_FLAGS) -C BabelStream clean
	$(MAKE) $(MAKE_FLAGS) -C matrixMul clean
	$(MAKE) $(MAKE_FLAGS) -C matrixMul-modified clean

