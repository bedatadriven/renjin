
## formula argument :

dx2x <- deriv(~ x^2, "x")

print(dx2x)

#expression({
#      .value <- x^2
#      .grad <- array(0, c(length(.value), 1), list(NULL, c("x")))
#      .grad[, "x"] <- 2 * x
#      attr(.value, "gradient") <- .grad
#      .value
#})

mode(dx2x)
x <- -1:2
eval(dx2x)

## Something 'tougher':
trig.exp <- expression(sin(cos(x + y^2)))
( D.sc <- D(trig.exp, "x") )
all.equal(D(trig.exp[[1]], "x"), D.sc)

( dxy <- deriv(trig.exp, c("x", "y")) )
y <- 1
eval(dxy)
eval(D.sc)

## function returned:
deriv((y ~ sin(cos(x) * y)), c("x","y"), func = TRUE)

## function with defaulted arguments:
(fx <- deriv(y ~ b0 + b1 * 2^(-x/th), c("b0", "b1", "th"),
          function(b0, b1, th, x = 1:7){} ) )
fx(2, 3, 4)

## New (R 3.4.0, 2017):
D(quote(log1p(x^2)), "x") ## log1p(x) = log(1 + x)
stopifnot(identical(
    D(quote(log1p(x^2)), "x"),
    D(quote(log(1+x^2)), "x")))
D(quote(expm1(x^2)), "x") ## expm1(x) = exp(x) - 1
stopifnot(identical(
    D(quote(expm1(x^2)), "x") -> Dex1,
    D(quote(exp(x^2)-1), "x")),
    identical(Dex1, quote(exp(x^2) * (2 * x))))

D(quote(sinpi(x^2)), "x") ## sinpi(x) = sin(pi*x)
D(quote(cospi(x^2)), "x") ## cospi(x) = cos(pi*x)
D(quote(tanpi(x^2)), "x") ## tanpi(x) = tan(pi*x)

stopifnot(identical(D(quote(log2 (x^2)), "x"),
                 quote(2 * x/(x^2 * log(2)))),
       identical(D(quote(log10(x^2)), "x"),
                 quote(2 * x/(x^2 * log(10)))))

