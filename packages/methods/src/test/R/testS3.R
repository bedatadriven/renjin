# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests
## accumulate here tests of the relation between S4 and S3 classes
## $<-.data.frame did stupid things with the class attribute
## that clobbered S4 classes extending "data.frame"
## Test that the S4 method (R 2.13.0) works transparently

library(hamcrest)
library(stats)
library(methods)

set.seed(864)
xx <- data.frame(a=rnorm(3),
                 b=as.factor(sample(c("T", "F"), 3, TRUE)),
                 row.names = paste("R",1:3,sep=":"))

setClass("myData", representation(extra = "character"), contains = "data.frame")
mx <- new("myData", xx, extra = "testing")

test.class.s3part.s4object = function() {
    assertThat(S3Class(S3Part(mx)), identicalTo("data.frame"))
    assertThat(S3Class(S3Part(mx, TRUE)), identicalTo("data.frame"))
}


# three kinds of $<-: replace, add, delete (NULL value)
mx$a <- mx$a * 2; mx$c <- 1:3; mx$b <- NULL
xx$a <- xx$a * 2; xx$c <- 1:3; xx$b <- NULL

ox <- new("myData", xx, extra = "testing")

test.relationS3toS4.01 <- function(){
    assertThat(mx, identicalTo( ox ))
}

tx <- mx



# test addition of vector as a data.frame column
ChrVectr <- c("A", "B", "C"); IntVectr <- 10:8; DblVectr <- c(0.1, 0.2, 0.3); LgcVectr <- c(TRUE, TRUE, FALSE)
tx$ChrVectr <- ChrVectr; tx$IntVectr <- IntVectr; tx$DblVectr <- DblVectr; tx$LgcVectr <- LgcVectr;
# test addition and extension of single value as a data.frame column
Chr <- "ABC"; Int <- 5L; Dbl <- 1.5; Lgc <- TRUE
tx$Chr <- Chr; tx$Int <- Int; tx$Dbl <- Dbl; tx$Lgc <- Lgc;

xy <- cbind(xx, ChrVectr = ChrVectr, IntVectr = IntVectr, DblVectr = DblVectr, LgcVectr = LgcVectr,
                     Chr = Chr, Int = Int, Dbl = Dbl, Lgc = Lgc, stringsAsFactors = FALSE)

nx <- new("myData", xy, extra = "testing")

test.relationS3toS4.02 <- function(){
    assertThat(tx, identicalTo( nx ))
}
