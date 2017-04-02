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


test.call <- function() {

    fn <- eval(call("function", pairlist(x=1), quote(x*3)))
    assertThat(fn(), identicalTo(3))
    assertThat(fn(4), identicalTo(12))
}

test.raw <- function() {

    fn <- `function`(NULL, 1+1)

    assertThat(fn(), identicalTo(2))
}

test.do.call <- function() {

    fn <- do.call("function", list(pairlist(x=1), quote(x*2)))

    assertThat(fn(3), identicalTo(6))
}

