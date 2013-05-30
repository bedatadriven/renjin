
# Smoke test for generic build

# ensure that stats package is loaded by default
x <- rnorm(12)

# ensure that C/fortran routines are compiling
dim(x) <- c(3,4)
invisible(qr(x))

# lm() should at least work!
x <- 1:30
y <- x*2
m <- lm(y ~ x)
stopifnot( (m$coefficients["x"]-2) < 0.0001 )

