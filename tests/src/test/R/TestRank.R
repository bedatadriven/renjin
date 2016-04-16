library(hamcrest)

test.RankNames <- function(){
    assertThat(rank(c(a=3, b=3, c=1)), identicalTo(c(a=2.5, b=2.5, c=1.0)))
    assertThat(rank(c(a=3, b=3, c=1), ties.method='min'), identicalTo(c(a=2L, b=2L, c=1L)))
    assertThat(rank(c(a=3, b=3, c=1), ties.method='max'), identicalTo(c(a=3L, b=3L, c=1L)))
    assertThat(rank(c(a=3, b=3, c=1), ties.method='average'), identicalTo(c(a=2.5, b=2.5, c=1.0)))
}
