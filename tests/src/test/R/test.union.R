
library(hamcrest)

test.union <- function() {

    assertThat( union("a", "b"), identicalTo(c("a", "b")) )
    assertThat( union(c(3,2,2,1), 4), identicalTo(c(3,2,1,4)) )
    assertThat( union(4, c(3,2,2,1)), identicalTo(c(4,3,2,1)) )
    assertThat( union(1,2,3), throwsError() )
    assertThat( union(1L, 2), identicalTo(c(1,2)) )
    assertThat( union(1L, 2L), identicalTo(c(1L,2L)) )
    assertThat( union(c(1, NA, 2), c(NA, 1, 1, 2, 3)), identicalTo(c(1, NA, 2, 3)) )
    assertThat( union(list(a=1), list(b=1)), identicalTo(list(1)) )
    assertThat( union(list(a=1), list(b=2)), identicalTo(list(1, 2)) )

}
