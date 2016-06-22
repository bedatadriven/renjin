
library(hamcrest)

test.kronecker <- function() {
    # S4 Method Dispatch to base function
    # From ACD package examples
    
    assertThat( kronecker(diag(3),t(rep(1,3))), 
        identicalTo(structure(
              c(1, 0, 0, 
                1, 0, 0, 
                1, 0, 0, 
                0, 1, 0, 
                0, 1, 0, 
                0, 1, 0, 
                0, 0, 1, 
                0, 0, 1,
                0, 0, 1), .Dim = c(3L, 9L))))


}