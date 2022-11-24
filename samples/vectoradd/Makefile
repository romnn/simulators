all:
	nvcc -ccbin=$(CXX) \
		--cudart shared \
		-gencode=arch=compute_50,code=compute_50 \
		-gencode=arch=compute_50,code=sm_50 \
		-gencode=arch=compute_60,code=compute_60 \
		-gencode=arch=compute_60,code=sm_60 \
		-gencode=arch=compute_75,code=compute_75 \
		-gencode=arch=compute_75,code=sm_75 \
		-O3 vectoradd.cu \
		-o vectoradd

clean:
	rm -f vectoradd
