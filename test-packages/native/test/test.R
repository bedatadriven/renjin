
library(hamcrest)

assertThat(mysum(1:10), identicalTo(56))
assertThat(mydsum(1:10), identicalTo(58))

x <- try( mydpchim() )
assertThat(inherits(x, "try-error"), identicalTo(TRUE) )