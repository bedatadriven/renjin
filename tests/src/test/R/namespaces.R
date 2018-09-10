#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, a copy is available at
# https://www.gnu.org/licenses/gpl-2.0.txt
#

library(hamcrest)
library(stats)
library('org.renjin.test:thirdparty')

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
    
    assertThat(length(search()), identicalTo(searchPathLen))
}