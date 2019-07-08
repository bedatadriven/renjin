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
library("org.renjin.test:s3test")

nl <- new("Country", name = "The Netherlands", temp = "COLD")
es <- new("Country", name = "Spain", temp = "WARM")
city <- new("City", new.env(hash = TRUE, parent = emptyenv()) )

test.simple.access = function() {
    assertThat(nl@name, identicalTo("The Netherlands"))
    assertThat(es@temp, identicalTo("WARM"))
}

test.simple.update = function() {
    nl <- setCountryName(nl, "Holland")
    es <- setCountryTemp(es, "Hot!")
    assertThat(nl@name, identicalTo("Holland"))
    assertThat(es@temp, identicalTo("Hot!"))
}

test.imported.methods.extending.builtins1 = function() {
    assertThat(city[[]], identicalTo( 300 ))
}

test.imported.methods.extending.builtins2 = function() {
    city[["a"]] <- 1
    assertThat(city[["a"]], identicalTo(1))
}

setMethod("-", c("City", "ANY"), function(e1, e2) 450)

test.imported.methods.group.01 = function() {
    assertThat(city + 1, identicalTo(350))
}

test.imported.methods.group.02 = function() {
    assertThat(city - 1, identicalTo(450))
}

test.imported.methods.group.03 = function() {
    assertThat(city / "A", identicalTo(351))
}