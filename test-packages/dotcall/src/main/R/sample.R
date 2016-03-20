

mySample <- function(count) {
    .Call(Cmysample, as.integer(count))
}