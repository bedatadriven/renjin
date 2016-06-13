
# The Ogata.txt.gz file should be ignored
# because there is an Ogata.rda file present

library(hamcrest)
library(utils)
library(org.renjin.test.dataTest)

data(Ogata)

assertThat(colnames(Ogata), identicalTo(c("magnitude", "time")))

