
library(hamcrest)

test.namespaceLoads <- function() {
	library(utils)
}

test.globalVariables <- function() {
    assertThat(utils::globalVariables(c(".obj1", ".obj2")), instanceOf("character"))
}

test.suppressForeignCheck <- function() {
    assertThat(utils::suppressForeignCheck(c(".obj1", ".obj2")), instanceOf("character"))
}

