
mysum2 <- function(x) {
    .Call(Cmysum2, as.numeric(x))
}