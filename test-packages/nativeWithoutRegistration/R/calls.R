


fortranCall <- function() {
    data <- c(0, 1, 2)
    output <- .Fortran("fortsub", as.double(data), length(data))
    return(output[[1]])
}


dotCall <- function(x) {
    .Call("_dotCall", x)
}

dotC <- function(x) {
    data <- c(1L, 2L, 3L)
    output <- .C("_dotC", data)
    return(output[[1]])
}