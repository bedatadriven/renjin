library(hamcrest)

test.merge <- function() {

    x <- data.frame(t=c(1,2,4,3,5), a=(1:5)^2)
    y <- data.frame(t=c(3,6,5,4,7), b=(3:7)^2)

    assertThat(merge(x,y), identicalTo(data.frame(t=c(3,4,5), a=c(16,9,25), b=c(9,36,25))))
    assertThat(merge(y,x), identicalTo(data.frame(t=c(3,4,5), b=c(9,36,25), a=c(16,9,25))))
    assertThat(merge(x,y,all=TRUE), identicalTo(data.frame(t=c(1,2,3,4,5,6,7),
                                                           a=c(1,4,16,9,25,NA,NA),
                                                           b=c(NA,NA,9,36,25,16,49))))
    assertThat(merge(x,y,all.y=TRUE), identicalTo(data.frame(t=c(3,4,5,6,7),
                                                             a=c(16,9,25,NA,NA),
                                                             b=c(9,36,25,16,49))))
}