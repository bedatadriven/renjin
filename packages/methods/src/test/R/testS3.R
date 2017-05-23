# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests
## accumulate here tests of the relation between S4 and S3 classes
## $<-.data.frame did stupid things with the class attribute
## that clobbered S4 classes extending "data.frame"
## Test that the S4 method (R 2.13.0) works transparently

library(hamcrest)
library(stats)
library(methods)

set.seed(864)
xx <- data.frame(a=rnorm(10),
                 b=as.factor(sample(c("T", "F"), 10, TRUE)),
                 row.names = paste("R",1:10,sep=":"))
setClass("myData", representation(extra = "character"),
         contains = "data.frame")
mx <- new("myData", xx, extra = "testing")

## three kinds of $<-: replace, add, delete (NULL value)
mx$a <- mx$a * 2
mx$c <- 1:10
mx$b <- NULL
xx$a <- xx$a * 2
xx$c <- 1:10
xx$b <- NULL

test.relationS3toS4.01 <- function(){
    assertThat(
        mx,
        identicalTo( new("myData", xx, extra = "testing") )
    )
}

.ignore.test.relationS3toS4.02 <- function(){

    mx$newChar <- "testing_more"
    mx$newInt <- 10:1
    assertThat(
        mx,
        identicalTo( new("myData", cbind(xx, newCol="testing_more", newInt = 10:1), extra = "testing") )
    )
}
