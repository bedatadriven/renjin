

fft <- newBenchmark("FFT over 2,400,000 random values", 
		init = {
			a <- rnorm(2400000)
		},
		run = {
			b <- fft(a)
		})

eign <- newBenchmark("Eigenvalues of a 640x640 random matrix", 
		init = {
			a <- rnorm(2500*2500)
			dim(a) <- c(2500, 2500)
		},
		run = {
			b <- det(a)
		})

# needs S4 objects
cholesky <- newBenchmark("Cholesky decomposition of a 3000x3000 matrix",
		init = {
			a <- new("dgeMatrix", x = Rnorm(1600*1600), Dim = as.integer(c(1600, 1600)))
		}, run = {
			
		})

inverse <- newBenchmark("Inverse of a 1600x1600 random matrix",
		init = {
			
		}, run = {
			
			
		}		
		)

		
#registerBenchmarkSuite(
#	name="Matrix Functions",
#	source="R-benchmark-25.R",
#	description="Matrix calculations",
#	benchmarks = list(eigen))
	
	

