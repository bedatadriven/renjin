
library(hamcrest)

test.compile <- function() {
    ce <- compiler::compile(quote(x*2))
    x <- 3
    
    assertThat(eval(ce), identicalTo(6))

    x <- 4
    assertThat(eval(ce), identicalTo(8))
}
