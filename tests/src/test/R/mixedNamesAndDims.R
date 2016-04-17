
library(hamcrest)

test.mixedNamesAndDims <- function() {
    
    # I would consider this to be an error, actually, but 
    # GNU R 3.2.2 allows both names and dim/dimnames to be present
    
    x <- structure(.Data = 
             list(g0 = 41, g1 = 42, g2 = 43,
                  g0 = 44, g1 = 45, g2 = 46,
                  g0 = 47, g1 = 48, g2 = 49,
                  g0 = 50, g1 = 51, g2 = 52,
                  g0 = 53, g1 = 54, g2 = 55),
               .Dim = c(3,5),
               .Dimnames = list(
                    c("g0","g1","g2"),
                    c("student","logistic","logWeibull","extreme","Huber")))


    assertThat(dim(x), identicalTo(c(3L, 5L)))
    assertThat(dimnames(x), identicalTo(list(
        c("g0", "g1", "g2"), 
        c("student", "logistic",  "logWeibull", "extreme", "Huber"))))
    
    assertThat(names(x), identicalTo(rep(c("g0", "g1", "g2"), times = 5)))
}    
    