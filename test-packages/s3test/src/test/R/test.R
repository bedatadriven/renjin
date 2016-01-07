

library(hamcrest)
library(org.renjin.test.s3test)

c1 <- new.circle(1)
assertThat(as.character(c1), equalTo("circle of radius 1"))
assertThat(area(c1), closeTo(pi, 0.001))

sq <- new.square(2)
assertThat(as.character(sq), equalTo("2x2 square"))
assertThat(area(sq), equalTo(4))