library(parallel)
library(hamcrest)

test.detectCores <- function() {
    cores <- detectCores()
    print(cores)
    assertTrue(detectCores() > 0)
}
