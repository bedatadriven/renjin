library(hamcrest)


test.orderWithNas <- function() {
    
    assertThat(order(c("C", "B", "A")), identicalTo(c(3L, 2L, 1L)))
    assertThat(order(c(NA, "B", "A")), identicalTo(c(3L, 2L, 1L)))
    assertThat(order(c(NA, "B", "A"), na.last=FALSE), identicalTo(c(1L, 3L, 2L)))
}