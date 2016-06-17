
mydsum <- function(x) {
    .Call(Cmydsum, as.numeric(x))
}