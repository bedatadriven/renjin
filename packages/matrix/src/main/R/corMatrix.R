#### "corMatrix" (was "correlation" in 2005) ---
#### ----------- correlation matrices, inheriting from  "dpoMatrix"

## dpo* -> cor* is in ./dpoMatrix.R
.M.2cor <- function(from) as(as(from, "dpoMatrix"), "corMatrix")

setAs("Matrix", "corMatrix", .M.2cor)
setAs("matrix", "corMatrix", .M.2cor)

## This is necessary :
setAs("dsyMatrix", "corMatrix", .M.2cor)
## BUT only because __ MM thinks __
## the *automatical* (by inheritance) coercion
### dsyMatrix -> corMatrix coercion is wrong:
## selectMethod(coerce, c("dsyMatrix","corMatrix")) # gives
## function (from, to)
## {
##     obj <- new("corMatrix")
##     as(obj, "dsyMatrix") <- from
##     obj
## }

rm(.M.2cor)
