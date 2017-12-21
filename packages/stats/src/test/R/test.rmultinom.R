
library(hamcrest)

test.prob.normalized <- function() {
    set.seed(100)
    x <- rmultinom(n=1, size=1000, prob=c(40, 60))

    assertThat(x, identicalTo(structure(c(406L, 594L), .Dim = c(2L, 1L))))
}

test.prob.empty <- function() {

    assertThat(rmultinom(n=1, size=0, prob=c(40, 60)), identicalTo(structure(c(0L, 0L), .Dim = c(2L, 1L))))
    assertThat(rmultinom(n=0, size=500, prob=c(40, 60)), identicalTo(structure(integer(0), .Dim = c(2L, 0L))))

}