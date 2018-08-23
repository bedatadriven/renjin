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

df <- data.frame(
        a = 1:3,
        b = c('x', 'y', 'z'),
        c = factor(c('yes', 'yes', 'no')),
        d = (1:3)*1.5,
        e = (1:3)+c(1i, 2i, 3i))

test.simple <- function() {
    assertThat(data.matrix(df), identicalTo(
            matrix(
                c(1, 2, 3, 
                  1, 2, 3, 
                  2, 2, 1, 
                  1.5, 3, 4.5, 
                  1, 2, 3), 
              nrow = 3, 
              dimnames = list(NULL, c("a", "b", "c", "d", "e")))))
}

test.rownamesForce <- function() {
    assertThat(data.matrix(df, rownames.force = TRUE), identicalTo(
            matrix(
                c(1, 2, 3, 
                  1, 2, 3, 
                  2, 2, 1, 
                  1.5, 3, 4.5, 
                  1, 2, 3), 
              nrow = 3, 
              dimnames = list(c("1", "2", "3"), c("a", "b", "c", "d", "e")))))
}

test.integer <- function() {
    assertThat(data.matrix(df[, 1:2]), identicalTo(
            matrix(
                c(1L, 2L, 3L, 
                  1L, 2L, 3L), 
              nrow = 3, 
              dimnames = list(NULL, c("a", "b")))))
}
