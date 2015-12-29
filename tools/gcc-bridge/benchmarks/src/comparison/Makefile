
GCC=gcc


all: kmeans_benchmark

kmns.o: ../main/fortran/kmns.f
	$(GCC) -c -O3 ../main/fortran/kmns.f

kmeans_benchmark.o: kmeans_benchmark.c
	$(GCC) -c -O3 kmeans_benchmark.c
    
kmeans_benchmark: kmns.o kmeans_benchmark.o
	$(GCC) -o kmeans_benchmark kmeans_benchmark.o kmns.o
	
