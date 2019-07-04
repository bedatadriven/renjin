
library(hamcrest)

assertThat(diag(3), identicalTo(structure(c(1, 0, 0, 0, 1, 0, 0, 0, 1), .Dim = c(3L, 3L))))

assertThat(diag(10, 3, 4), identicalTo(structure(c(10, 0, 0, 0, 10, 0, 0, 0, 10, 0, 0, 0), .Dim = 3:4)))

assertThat(diag(1:3), identicalTo(structure(c(1L, 0L, 0L, 0L, 2L, 0L, 0L, 0L, 3L), .Dim = c(3L, 3L))))

