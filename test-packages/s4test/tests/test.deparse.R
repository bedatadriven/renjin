#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
library(methods)
library("org.renjin.test:s4test")

test.s4 <- function() {
    s <- new("Z")

    assertThat(deparse(s), equalTo("<S4 object of class structure(\"Z\", package = \"s4test\")>"))
}


test.namespace.call <- function() {
    c <- quote(tree::tree(Species ~., iris))
    assertThat(deparse(c), identicalTo("tree::tree(Species ~ ., iris)"))
}
