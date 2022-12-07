all: vectoradd

vectoradd: vectoradd.o
	nvcc -O3 vectoradd.o -o vectoradd

vectoradd.o: vectoradd.cu
	# -arch=sm_30
	# supposed to work for CUDA 4 such that ocelot can handle the PTX
	nvcc -c vectoradd.cu \
		-gencode=arch=compute_10,code=compute_10 \
		-gencode=arch=compute_13,code=compute_13 \
		-gencode=arch=compute_20,code=compute_20 \
		-gencode=arch=compute_30,code=compute_30

clean:
	rm -f vectoradd *.o *.out
