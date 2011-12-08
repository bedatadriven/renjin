mean.online <- function(x) {
	xbar <- x[1]
	
	for(n in seq(from = 2, to = length(x))) {
		xbar <- ((n - 1) * xbar + x[n]) / n
	}
	
	xbar
}

var.online <- function(x) {
	mean <- 0
	M2 <- 0
	
	for(n in seq(length(x))) {
		delta <- x[n] - mean
		mean <- mean + delta / n
		M2 <- M2 + delta * (x[n] - mean)
	}
	
	M2 / length(x)
}

t.test.online <- function(x, y) { # 2-sample t-test
	n <- length(x)
	m <- length(y)
	s2 <- ((n - 1) * var.online(x) + (m - 1) * var.online(y)) / (n + m - 2)
	
	t <- sqrt(n) * sqrt(m) / sqrt(n + m) * (mean.online(x) - mean.online(y)) / sqrt(s2)
	t
}
cat("starting...\n")
start <- Sys.time()

### 
#mean.online(log(seq(10e6)))


### 
#var.online(log(seq(10e6)))


### 
x <- log(seq(10e5))
y <- log(seq(10e5 + 5 * 10e5) - 0.5)

cat(paste("t=",t.test.online(x, y),"\n"))

cat("done 2\n");
end <- Sys.time()

print(end - start) 
