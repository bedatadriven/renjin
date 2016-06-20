
library(hamcrest)
library(utils)
library(org.renjin.test.dataTest)

data(powers)
print(powers)
assertThat(powers, identicalTo(c(1, 4, 9, 16, 25, 36, 49, 64)))


