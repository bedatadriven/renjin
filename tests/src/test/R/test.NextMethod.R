
library(hamcrest)


test.many <- function() {

    x <-  ts(1:10, frequency = 4, start = c(1959, 2)) 
    y <- x - x
    
    assertThat(y, identicalTo(structure(c(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L), 
                    .Tsp = c(1959.25, 1961.5, 4), class = 'ts')))
     
}