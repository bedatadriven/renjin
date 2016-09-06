library(hamcrest)
library(utils)
library(org.renjin.test.dataTest)

data(colors)
print(colors)
assertThat(colors, identicalTo(c("red", "blue", "green", "purple")))


