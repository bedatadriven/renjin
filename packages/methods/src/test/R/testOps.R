
library(methods)
library(hamcrest)


# create S4 class "molecule"
molecule <- setClass("Molecule",
    slots = c(
        name = "character",
        content = "character",
        size = "numeric"
        ),
    prototype = list(
        name = "Molecule_Name",
        content = "Molecule_Formula",
        size = 0.0
        )
)

a = new("Molecule", name = "Water", content = "H2O", size = 100)
b = molecule(name = "Salt", content = "NaCl", size = 10.1)

setMethod("+", signature = c("Molecule", "numeric"), function(e1, e2) { 41 } )
setMethod("+", signature = c("numeric", "Molecule"), function(e1, e2) { 42 } )

assertThat(a+1, identicalTo(41))
assertThat(1+a, identicalTo(42))

setMethod("%*%", signature = c("Molecule", "numeric"), function(x, y) { 43 } )
setMethod("%*%", signature = c("numeric", "Molecule"), function(x, y) { 44 } )

assertThat(a %*% 99, identicalTo(43))
assertThat(99 %*% a, identicalTo(44))
