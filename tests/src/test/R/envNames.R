library(hamcrest)

test.searchPath <- function() {
    assertTrue(".GlobalEnv" %in% search())
    assertTrue("package:base" %in% search())
}