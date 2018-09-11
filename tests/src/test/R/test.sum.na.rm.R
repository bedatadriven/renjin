
library(hamcrest)

test.sum.na.rm <- function() {
    assertThat(sum(c(1:3, NA), na.rm="T"), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm="TR"), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm="TRUE"), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm="FOOBAR"), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=NA), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=1), identicalTo(6L))

    assertThat(sum(c(1:3, NA), na.rm="F"), identicalTo(NA_integer_))
    assertThat(sum(c(1:3, NA), na.rm="false"), identicalTo(NA_integer_))
    assertThat(sum(c(1:3, NA), na.rm="FALSE"), identicalTo(NA_integer_))
    assertThat(sum(c(1:3, NA), na.rm="FFFFO"), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=0), identicalTo(NA_integer_))

    assertThat(sum(c(1:3, NA), na.rm=quote(x)), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=.GlobalEnv), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=integer(0)), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=NULL), identicalTo(6L))

}