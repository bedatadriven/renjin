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
library(utils)

test.typeConvert.1 <- function() {
    assertThat(type.convert(c('1','2','3'), 'NA', FALSE), identicalTo(c(1L,2L,3L)))
}

test.typeConvert.2 <- function() {
    assertThat(type.convert(c('T','NA','F'), 'NA', FALSE), identicalTo(c(TRUE, NA, FALSE)))
}

test.typeConvert.3 <- function() {
    assertThat(type.convert(c('bing', 'bop'), 'FOO', TRUE), equalTo(c("bing","bop")))
}

test.typeConvert.4 <- function() {
    assertThat(type.convert(c('bing', 'bop'), 'FOO', FALSE),  identicalTo(structure(1:2, class = "factor", .Label = c("bing", "bop"))))
}

test.typeConvert.5 <- function() {
    assertThat(type.convert(c('T','NA',''), 'NA', FALSE), identicalTo(c(TRUE, NA, NA)))
}

test.typeConvert.6 <- function() {
    assertThat(type.convert(c('T','FALSE','BOB'), 'BOB', FALSE), identicalTo(c(TRUE, FALSE, NA)))
}

test.typeConvert.7 <- function() {
    assertThat(type.convert(c('3.5','3.6','FOO'), 'FOO', FALSE), identicalTo(c(3.5,3.6,NA)))
}

test.typeConvert.8 <- function() {
    assertThat(type.convert(c('C','A','B'), 'FOO', FALSE), identicalTo(   structure(c(3L, 1L, 2L), .Label = c("A", "B", "C"), class = "factor")   ))
}