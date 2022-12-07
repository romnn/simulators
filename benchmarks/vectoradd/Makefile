# nvcc -m32 -arch sm_20 -lm2s-cuda -O3 vectoradd.cu -o vectoradd.o

# nvcc -c vectoradd.cu -arch sm_35 -m32 -Xcicc -O0 -Xptxas -O0 -o vectoradd.o
# g++ vectoradd.o -o vectoradd -m32 -L/app/lib/.libs -lm2s-cuda

# nvcc -c vectorAdd.cu  -arch sm_35 -c -m32 -Xcicc -O0 -Xptxas -O0 -I../include -I../inc -o vectorAdd_disop.o
# g++   vectorAdd_disop.o -o vectorAdd_m2s -m32 -L ~/multi2sim/lib/.libs -lm2s-cuda

m2s:
	nvcc -c vectoradd.cu -arch sm_35 -m32 -Xcicc -O0 -Xptxas -O0 -o vectoradd.o
	g++ vectoradd.o -o vectoradd -m32 -L/app/lib/.libs -lm2s-cuda

m2s_old:
	nvcc -m32 -arch sm_20 -O3 vectoradd.cu -o vectoradd.o
	g++ -o vectoradd -m32 vectoradd.o -L/app/lib/.libs -static -lm2s-cuda

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
