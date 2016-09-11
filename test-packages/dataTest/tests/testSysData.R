
library(hamcrest)
library(org.renjin.test.dataTest)

assertThat(computeMeaningOfLife(), identicalTo(42))
