
library(hamcrest)

test.partialMatchingBeforeElipses <- function() {

    f <- function(aardvark, ... ) names(list(...))
    
    matchedArgs <- f(a = 41, b = 42, c = 43)
    
    assertThat(matchedArgs, identicalTo(c('b', 'c')))

}

test.noPartialMatchingFollowingElipses <- function() {

    f <- function(... , aardvark) names(list(...))
    
    matchedArgs <- f(a = 41, b = 42, c = 43)
    
    assertThat(matchedArgs, identicalTo(c('a', 'b', 'c')))

}