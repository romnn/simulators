#!/usr/bin/env bash

# must be run from gpgpusim root

export SIM_LIB_DIR=lib/gcc-$(CC_VERSION)/cuda-$(CUDART_VERSION)/release

if [ ! -f $(SIM_LIB_DIR)/libcudart.so.2 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.2; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.3 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.3; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.4 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.4; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.5.0 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.5.0; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.5.5 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.5.5; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.6.0 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.6.0; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.6.5 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.6.5; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.7.0 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.7.0; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.7.5 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.7.5; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.8.0 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.8.0; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.9.0 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.9.0; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.9.1 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.9.1; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.9.2 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.9.2; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.10.0 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.10.0; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.10.1 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.10.1; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.10.2 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.10.2; fi
	if [ ! -f $(SIM_LIB_DIR)/libcudart.so.11.0 ]; then ln -s $(SIM_LIB_DIR)/libcudart.so $(SIM_LIB_DIR)/libcudart.so.11.0; fi
