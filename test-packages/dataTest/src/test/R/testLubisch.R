library(hamcrest)
library(utils)
library(org.renjin.test.dataTest)

data(lubisch)
print(lubisch)
assertThat(nrow(lubisch), identicalTo(74L))
assertThat(names(lubisch), identicalTo(c("Espece", "X1", "X2", "X3", "X4", "X5", "X6", "E")))

