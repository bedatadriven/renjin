
library(hamcrest)

test.noPartialMatchingWhenElipisesIsPresent <- function() {

    f <- function(... , aardvark) names(list(...))
    
    matchedArgs <- f(a = 41, b = 42, c = 43)
    
    assertThat(matchedArgs, identicalTo(c('a', 'b', 'c')))

}