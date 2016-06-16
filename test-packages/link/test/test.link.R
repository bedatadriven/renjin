library(hamcrest)
library(org.renjin.test.link)

assertThat(mysum2(1:10), identicalTo(56))
assertThat(mydsum2(1:10), identicalTo(58))