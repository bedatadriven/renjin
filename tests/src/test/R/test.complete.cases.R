
library(stats)
library(hamcrest)

test.ignore.NULL <- function() {
    x <- matrix(c(2,4,3,1,5,7),nrow=3)
    y <- c(7,4,2)

    assertThat(complete.cases(x, y),             identicalTo(c(TRUE, TRUE, TRUE)))
    assertThat(complete.cases(x, y, NULL),       identicalTo(c(TRUE, TRUE, TRUE)))
    assertThat(complete.cases(x, y, numeric(0)), throwsError())
}

test.do.not.ignore.empty <- function() {
    x <- matrix(c(2,4,3,1,5,7),nrow=3)
    y <- c(7,4,NA)

    assertThat(complete.cases(x, y),             identicalTo(c(TRUE, TRUE, FALSE)))
    assertThat(complete.cases(x, y, numeric(0)), throwsError())
}