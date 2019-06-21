

.Call.graphics <- function (...) {
    .External2(C_do_dotcallgr, ...)
}

.External.graphics <- function (...) {
    .External2(C_do_Externalgr, ...)
}

