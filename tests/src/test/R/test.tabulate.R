
library(hamcrest)

assertThat(tabulate(c(2,3,5)), identicalTo(c(0L, 1L, 1L, 0L, 1L)))

assertThat(tabulate(c(2,3,3,5), nbins = 10), identicalTo(c(0L, 1L, 2L, 0L, 1L, 0L, 0L, 0L, 0L, 0L)))

assertThat(tabulate(c(-2,0,2,3,3,5)),  # -2 and 0 are ignored
    identicalTo(c(0L, 1L, 2L, 0L, 1L)))

assertThat(tabulate(c(-2,0,2,3,3,5), nbins = 3), identicalTo(c(0L, 1L, 2L)))

assertThat(tabulate(factor(letters[1:10])), identicalTo(c(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L)))

