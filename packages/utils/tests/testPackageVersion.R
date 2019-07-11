
library(hamcrest)


# Find the package version of a package that is NOT loaded
assertFalse(isNamespaceLoaded("grid"))
assertThat(as.character(packageVersion("grid")), identicalTo("3.5.3"))

