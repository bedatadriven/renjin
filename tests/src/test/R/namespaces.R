library(hamcrest)
library(stats)
library(org.renjin.test.thirdparty)

test.getNamespaceName <- function() {
    
    assertThat(getNamespaceName(environment(rnorm)), identicalTo(c(name = "stats")))
    assertThat(getNamespaceName(environment(qr)), identicalTo("base"))
}

test.getNamespaceNameOfThirdParty <- function() {
    assertThat(getNamespaceName(environment(compute)), identicalTo(c(name = "org.renjin.test:thirdparty")))
}

test.getNamespaceExports <- function() {
    
    assertTrue("model.matrix.lm" %in% getNamespaceExports(environment(rnorm)))
    assertTrue("model.matrix.lm" %in% getNamespaceExports("stats"))
    assertTrue("model.matrix.lm" %in% getNamespaceInfo(environment(rnorm), which = "exports"))
    assertTrue("for" %in% getNamespaceExports(environment(qr)))
    assertTrue("for" %in% getNamespaceExports("base"))
}

test.isNamespaceLoaded <- function() {
    assertTrue(isNamespaceLoaded("stats"))
}

# Not yet implemented
ignore.test.getNamespaceImports <- function() {

    imports <- getNamespaceImports("stats")
    print(imports)
    assertThat(typeof(imports), identicalTo("list"))
    assertThat(imports$base, identicalTo(TRUE))
    assertThat(imports$utils, identicalTo(str = str))
}

test.requireNamespace <- function() {
    assertTrue(requireNamespace("stats"))
}

test.requireNamespaceNonExistant <- function() {
    assertFalse(requireNamespace("fooooobarrr"))
}

test.library.multiple <- function() {
    searchPathLen <- length(search())
    
    library(stats)
    library(stats)
    library(methods)
    
    assertThat(length(search()), identicalTo(searchPathLen))
}