
fib <- newBenchmark("3,500,000 Fibonacci numbers calculation (vector calc)", 
  init = {
	 a <- 0
	 b <- 0
	 phi <- 1.6180339887498949
	 a <- floor(runif(3500000)*1000)
  },
  run = {
     (phi^a - (-phi)^(-a))/sqrt(5)
  }
)

hilbert <- newBenchmark("Creation of a 3000x3000 Hilbert matrix (matrix calc)", {
	a <- 3000
	b <- rep(1:a, a)
  	dim(b) <- c(a, a);
    b <- 1 / (t(b) + 0:(a-1))
})	

## I haven't included this yet because Renjin's handling vector/matrix updating 
## is catastrophically bad at the moment.
toeplitz <- newBenchmark("Creation of a 500x500 Toeplitz matrix (loops)", 
  init = {
    b <- rep(0, 500*500)
    dim(b) <- c(500, 500)
  },
  run = {
	  # Rem: there are faster ways to do this
	  	# but here we want to time loops (220*220 'for' loops)! 
	    for (j in 1:500) {
	      for (k in 1:500) {
	        b[k,j] <- abs(j - k) + 1
	      }
	    }
	  }
 )


# need to implement cor() for this benchmark
escoufier <- newBenchmark("Escoufier's method on a 45x45 matrix (mixed)",
 init = {
    p <- 0; vt <- 0; vr <- 0; vrt <- 0; rvt <- 0; RV <- 0; j <- 0; k <- 0;
	x2 <- 0; R <- 0; Rxx <- 0; Ryy <- 0; Rxy <- 0; Ryx <- 0; Rvmax <- 0
	Trace <- function(y) {sum(c(y)[1 + 0:(min(dim(y)) - 1) * (dim(y)[1] + 1)], na.rm=FALSE)}
	x <- abs(rnorm(45*45)); dim(x) <- c(45, 45)
	
 },
 run = {
 	# Calculation of Escoufier's equivalent vectors
    p <- ncol(x)
    vt <- 1:p                                  # Variables to test
    vr <- NULL                                 # Result: ordered variables
    RV <- 1:p                                  # Result: correlations
    vrt <- NULL
    for (j in 1:p) {                           # loop on the variable number
      Rvmax <- 0
      for (k in 1:(p-j+1)) {                   # loop on the variables
        x2 <- cbind(x, x[,vr], x[,vt[k]])
        R <- cor(x2)                           # Correlations table
        Ryy <- R[1:p, 1:p]
        Rxx <- R[(p+1):(p+j), (p+1):(p+j)]
        Rxy <- R[(p+1):(p+j), 1:p]
        Ryx <- t(Rxy)
        rvt <- Trace(Ryx %*% Rxy) / sqrt(Trace(Ryy %*% Ryy) * Trace(Rxx %*% Rxx)) # RV calculation
        if (rvt > Rvmax) {
          Rvmax <- rvt                         # test of RV
          vrt <- vt[k]                         # temporary held variable
        }
      }
      vr[j] <- vrt                             # Result: variable
      RV[j] <- Rvmax                           # Result: correlation
      vt <- vt[vt!=vr[j]]                      # reidentify variables to test
    }
 }
)

registerBenchmarkSuite(
   name="Programmation",
   source="R-benchmark-25.R",
   description="'Programmation' benchmarks from the R-benchmark-25.R script",
   benchmarks = list(fib, hilbert))
   		
   		