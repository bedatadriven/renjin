
mysum  <- function(x) {
    .Call(C_mysum , as.numeric(x))
}