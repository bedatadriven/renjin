
library(hamcrest)

test.turlach <- function() {

    assertThat(runmed(c(1, 5, 10, 3, 2), 3, algorithm = "Turlach"), 
        identicalTo(structure(c(5, 5, 5, 3, 2), k = 3L)))
    
}

