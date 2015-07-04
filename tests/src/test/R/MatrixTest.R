library(hamcrest)

test.MatrixProduct <- function() {
	assertThat(dim(matrix(10)), equalTo(c(1L,1L)))
	
	x <- 1:3 %*% c(3,2,1)
	assertThat(typeof(x), equalTo("double"))
	assertThat(x, equalTo(10))
	assertThat(dim(x), equalTo(c(1L,1L)))
	assertThat(x, identicalTo(matrix(10)))
}

test.Svd <- function() {
	hilbert <- function(n) { 
		i <- 1:n
		1 / outer(i - 1, i, '+') 
	}
	X <- hilbert(9)[,1:6]
	s <- svd(X)
	
	assertThat(s$d, closeTo(c(
							1.668433e+00, 
							2.773727e-01, 
							2.223722e-02, 
							1.084693e-03, 
							3.243788e-05, 
							5.234864e-07), 0.000001))
	
	assertThat( s$u, closeTo(
					rbind(
							c(-0.7244999,  0.6265620,  0.27350003, -0.08526902,  0.02074121, -0.004024550),
							c(-0.4281556, -0.1298781, -0.64293597,  0.55047428, -0.27253421,  0.092815916),
							c(-0.3121985, -0.2803679, -0.33633240, -0.31418014,  0.61632113, -0.440903754),
							c(-0.2478932, -0.3141885, -0.06931246, -0.44667149,  0.02945426,  0.530119859),
							c(-0.2063780, -0.3140734,  0.10786005, -0.30241655, -0.35566839,  0.237038375),
							c(-0.1771408, -0.3026808,  0.22105904, -0.09041508, -0.38878613, -0.260449267),
							c(-0.1553452, -0.2877310,  0.29280775,  0.11551327, -0.19285565, -0.420944825),
							c(-0.1384280, -0.2721599,  0.33783778,  0.29312535,  0.11633231, -0.160790254),
							c(-0.1248940, -0.2571250,  0.36542543,  0.43884649,  0.46496714,  0.434599540)), 0.0000001));    
	
	assertThat( s$v, closeTo(
					rbind(
							c(-0.7364928,  0.6225002,  0.2550021, -0.06976287,  0.01328234, -0.001588146),
							c(-0.4432826, -0.1818705, -0.6866860,  0.50860089, -0.19626669,  0.041116974),
							c(-0.3274789, -0.3508553, -0.2611139, -0.50473697,  0.61605641, -0.259215626),
							c(-0.2626469, -0.3921783,  0.1043599, -0.43747940, -0.40833605,  0.638901622),
							c(-0.2204199, -0.3945644,  0.3509658,  0.01612426, -0.46427916, -0.675826789),
							c(-0.1904420, -0.3831871,  0.5110654,  0.53856351,  0.44663632,  0.257248908)), 0.0000001));
}

test.DimNamesOnMatrixMultiplication <- function() {
		
	y <- diag(1:4)
	dimnames(y) <- list(
			c("yr1", "yr2", "yr3", "yr4"), 
			c("yc1", "yc2", "yc3", "yc4"))
	
	z <- matrix(1:12, ncol = 3, nrow = 4)
	dimnames(z) <- list(
			c("zr1", "zr2", "zr3", "zr4"), 
			c("zc1", "zc2", "zc3"))
	
	x <- y %*% z
	
	assertThat(dimnames(x), identicalTo(
					list(c("yr1", "yr2", "yr3", "yr4"),
						 c("zc1", "zc2", "zc3"))))
}

test.lazyTranspose <- function() {

	m <- matrix(1:10000, ncol=2)
	colnames(m) <- c("a", "b")
	rownames(m) <- paste("r", 1:nrow(m), sep="")
	
	tm <- t(m)
	
	assertThat(colnames(tm), identicalTo(rownames(m)))
	assertThat(rownames(tm), identicalTo(colnames(m)))
	
	# Dropping dims shouldn't affect the transformation
	dim(tm) <- NULL
	assertThat(tm[1:6], identicalTo(c(1, 5001, 2, 5002, 3, 5003)))
}

