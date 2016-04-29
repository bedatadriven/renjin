
library(hamcrest)

assertThat(mysum(1:10), identicalTo(55))

x <- try( mydpchim() )
assertThat(inherits(x, "try-error"), identicalTo(TRUE) )