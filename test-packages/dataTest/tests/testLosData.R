
library(hamcrest)
library(utils)
library(org.renjin.test.dataTest)

data(los.data)
assertThat(colnames(los.data), identicalTo(c("adm.id", "j.01", "j.02", "j.03", "j.12", "j.13", "cens")))
assertThat(nrow(los.data), identicalTo(756L))
