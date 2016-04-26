
mysum <- function(x) {
    .Call(Cmysum, as.numeric(x))
}