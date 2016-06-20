
library(hamcrest)
library(utils)
library(org.renjin.test.dataTest)

data(etm)
assertThat(colnames(etm), identicalTo(c("id", "entry", "exit", "group", "cause")))


