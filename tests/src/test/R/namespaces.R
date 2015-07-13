library(hamcrest)
library(stats)

test.getNamespaceName <- function() {
    
    assertThat(getNamespaceName(environment(rnorm)), identicalTo(c(name = "stats")))
    assertThat(getNamespaceName(environment(qr)), identicalTo(c(name = "base")))
}

test.getNamespaceExports <- function() {
    
    assertTrue("model.matrix.lm" %in% getNamespaceExports(environment(rnorm)))
    assertTrue("for" %in% getNamespaceExports(environment(qr)))
}

