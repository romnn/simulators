all: vectoradd

vectoradd: vectoradd.o
	nvcc -O3 vectoradd.o -o vectoradd

vectoradd.o: vectoradd.cu
	nvcc -c vectoradd.cu -arch=sm_60

clean:
	rm -f vectoradd *.o *.out
