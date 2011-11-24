
Rnorm <- rnorm


matrix1 <- newBenchmark("Creation, transp., deformation of a 2500x2500 matrix", {
	a <- matrix(Rnorm(2500*2500)/10, ncol=2500, nrow=2500);
	b <- t(a);
	a <- t(b);
})

matrix2 <- newBenchmark("2400x2400 normal distributed random matrix ^1000",
 init = {
	a <- abs(matrix(Rnorm(2500*2500)/2, ncol=2500, nrow=2500));
 }, 
 run = {
    a^1000 
 }
)

matrix3 <- newBenchmark("Sorting of 7,000,000 random values", 
 init = {
	a <- Rnorm(2800*2800)
    dim(a) <- c(2800, 2800)
 },
 run = { 
 	sort(a, method="quick")	# Sort is modified in v. 1.5.x	
 }
)

matrix4 <- newBenchmark("2800x2800 cross-product matrix (b = a' * a)", 
 init = {
	a <- Rnorm(2800*2800); dim(a) <- c(2800, 2800)
 },
 run = {
	crossprod(a)
})
	

registerBenchmarkSuite(
   name="Matrix Calculations",
   source="R-benchmark-25.R",
   description="Matrix calculations",
   benchmarks = list(matrix1, matrix2, matrix3, matrix4))
   		
   		
 