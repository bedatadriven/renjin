

.Call.graphics <- .Call

.External.graphics <- function (...) {
    .External2(C_do_Externalgr, ...)
}

