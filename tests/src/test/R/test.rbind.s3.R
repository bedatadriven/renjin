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

rbind.foo <- function(...) list("foo", ...)

rbind.baz <- function(...) list("baz", ...)


foo <- structure(41, class = "foo")
baz <- structure(42, class = "baz")
qux <- structure(42, class = "qux")


assertThat(rbind(1,3,4,5,foo), identicalTo(list("foo", 1, 3, 4, 5, foo)))
assertThat(rbind(1,3,4,5,foo,qux), identicalTo(list("foo", 1, 3, 4, 5, foo, qux)))

# Conflicting method selections should result it default

assertThat(rbind(34,foo,baz), identicalTo(structure(c(34, 41, 42), .Dim = c(3L, 1L), .Dimnames = list(c("",  "foo", "baz"), NULL))))





