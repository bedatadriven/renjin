library(hamcrest)

test.searchPath <- function() {
    assertTrue(".GlobalEnv" %in% search())
    assertTrue("package:base" %in% search())
}

test.environmentNames <- function() {
    env <- new.env()
    env$x <- 1
    env$y <- 2
    
    assertThat(names(env), identicalTo(c("x", "y")))

}