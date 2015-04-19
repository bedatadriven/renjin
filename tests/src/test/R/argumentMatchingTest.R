
test.noPartialMatchingWhenElipisesIsPresent <- function() {

    f <- function(... , aardvark) names(list(...))
    
    matchedArgs <- f(a = 41, b = 42, c = 43)
    
    print(matchedArgs)
    stopifnot(identical(matchedArgs, c('a', 'b', 'c')))

}