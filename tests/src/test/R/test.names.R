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

test.attributes.dropped <- function() {
    n <- c(a="x", b="y", c="z")
    x <- 1:3

    # The names<- function drops attributes
    names(x) <- n
    assertThat(names(x), identicalTo(c("x", "y", "z")))

    # Setting the attribute directly does not!
    attr(x, 'names') <- n
    assertThat(names(x), identicalTo(n))
}

test.dimnames.attributes.dropped <- function() {

    x <- 1:4
    dim(x) <- c(2,2)

    rn <- c(a='x', b='y')
    dim(rn) <- c(1,2)

    cn <- c(d='z', e='q')

    dimnames(x) <- list(rn, cn)



}