
library(hamcrest)

test.chol <- function() {

    assertThat(chol(matrix(c(8,1,1,4),2,2)), identicalTo(
        structure(c(2.82842712474619, 0, 0.353553390593274, 1.96850196850295 ),
            .Dim = c(2L, 2L)), tol = 1e-6))

}