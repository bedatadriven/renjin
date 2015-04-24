
library(org.renjin.test.beta)
library(hamcrest)


test.jacksonVersion <- function() {
    assertThat(betaVersion(), identicalTo("2.0.0"))
}

test.invokeClassesInPackage <- function() {
    assertThat(betaName(), identicalTo("Beta"))
}

