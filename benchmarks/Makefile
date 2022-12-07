# SUB_DIRS = $(wildcard */.)
# SUB_DIRS_ACCELSIM = $(SUB_DIRS:%=accelsim-%)
# SUB_DIRS_CLEAN = $(SUB_DIRS:%=clean-%)

# accelsim: $(SUB_DIRS_ACCELSIM)
# clean: $(SUB_DIRS_CLEAN)

# $(SUB_DIRS_ACCELSIM):
# 	$(MAKE) $(MAKE_FLAGS) -C $(@:accelsim-%=%)

# $(SUB_DIRS_CLEAN):
#	$(MAKE) $(MAKE_FLAGS) -C $(@:clean-%=%) clean

accelsim:
	$(MAKE) $(MAKE_FLAGS) -C matrixMul accelsim

clean:
	$(MAKE) $(MAKE_FLAGS) -C matrixMul clean

