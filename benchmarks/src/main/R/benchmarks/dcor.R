

dcor <- function (x, y, index = 1)
{
  import(org.renjin.DistanceMatrix)

  x <- DistanceMatrix$new(x)
  y <- DistanceMatrix$new(y)

  cat("calculated distances...\n")

	n <- nrow(x)
	m <- nrow(y)
	if (n != m)
		stop("Sample sizes must agree")
#	if (!(all(is.finite(c(x, y)))))
#		stop("Data contains missing or infinite values")
	if (index < 0 || index > 2) {
		warning("index must be in [0,2), using default index=1")
		index = 1
	}

	cat("to matrices...\n")

	stat <- 0
	dims <- c(n, ncol(x), ncol(y))
	Akl <- function(x) {
		d <- as.matrix(x)^index
		m <- rowMeans(d)
		M <- mean(d)
		a <- sweep(d, 1, m)
		b <- sweep(a, 2, m)
		return(b + M)
	}
	A <- Akl(x)
	B <- Akl(y)
	dCov <- sqrt(mean(A * B))
	dVarX <- sqrt(mean(A * A))
	dVarY <- sqrt(mean(B * B))
	V <- sqrt(dVarX * dVarY)
	print(V)
	print(dCov)
	if (V > 0)
		dCor <- dCov/V
	else dCor <- 0
	dCor
}

dcor5k <- newBenchmark("distance correlation n = 5000",
 init = {
	a <- rnorm(20000);
	b <- rnorm(20000);
 },
 run = {
  dcor(a,b)
})

registerBenchmarkSuite(
   name="Distance Correlation",
   source="energy package",
   description="Distance correlation",
   benchmarks = list(dcor5k))


