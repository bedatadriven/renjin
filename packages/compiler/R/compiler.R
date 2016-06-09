
# Renjin does not take the same approach as GNU R,
# which introduced the idea of "compiled" bytecode expressions

# However, some packages use these functions, generally during
# the evaluation of the namespace, so we need substitutes that
# have the same affects

compile <- function(e, env = .GlobalEnv, options = NULL) {
    e
}

cmpfun <- function (f, options = NULL) {
    stopifnot(typeof(f) == "closure")
    return(f)
}

enableJIT <- function(level) {
    warning("Renjin's JIT compiler behaves differently than that of GNU R and cannot be interactively enabled")
    level
}

compilePKGS <- function(enable) {
    warning("Renjin's JIT compiler activates at runtime, and does not need to be enabled for packages")
    FALSE
}

