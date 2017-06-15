
## Ensure that we can read bytecode-compiled functions

library(renjin)

f <- function(x) {
    x + 33
}

f <- compiler::cmpfun(f)


stopifnot(renjin(f(41)) == 41 + 33)