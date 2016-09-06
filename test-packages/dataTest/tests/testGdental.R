library(hamcrest)
library(utils)
library(org.renjin.test.dataTest)

data(gdental)

assertThat(colnames(gdental), identicalTo(c("cj", "nj")))

