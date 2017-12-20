#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

f <- function(x) x*2
g <- function(x) { y <- x +4 ; z <- y * x +3; }

assertThat(deparse(f), identicalTo(c("function (x) x * 2")))
assertThat(deparse(g), identicalTo(c("function (x) {\ny <- x + 4\nz <- y * x + 3\n}")))

assertThat(deparse("\"label\""), identicalTo( "\"\\\"label\\\"\""))
assertThat(deparse("\n"), identicalTo("\"\\n\""))

h <- function(x) {
    z <- function(y) y * 3
    z(x)
}

hh <- eval(parse(text=deparse(h)))
assertThat(hh(3), identicalTo(9))


cat(deparse(stats:::plotNode))

plotNode <- eval(parse(text=deparse(stats:::plotNode)))

