

detectCores <- function(all.tests = FALSE, logical = FALSE) {
    import(java.lang.Runtime)

    return(Runtime$getRuntime()$availableProcessors())
}