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

test.paste <- function() {
    s1 <- paste("foo", "bar")
    s2 <- paste("x", 0, sep = "")
    s3 <- paste(c("A", "B"), collapse = ", ")

    assertThat(s1, equalTo("foo bar"))
    assertThat(s2, equalTo("x0"))
    assertThat(s3, equalTo("A, B"))
}


test.paste.factors <- function() {

    x <- as.factor(c("Geothermal", "Electricity"))

    assertThat(paste(x[1], x[2]), identicalTo("Geothermal Electricity"))
}

test.paste.null <- function() {

    assertThat(paste(NULL), identicalTo(character(0)))
    assertThat(paste(NULL, collapse=""), identicalTo(""))
}

test.paste.eval <- function() {
    assertThat(paste(quote(a)), identicalTo("a"))
}