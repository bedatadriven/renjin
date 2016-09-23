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

test.mixedNamesAndDims <- function() {
    
    # I would consider this to be an error, actually, but 
    # GNU R 3.2.2 allows both names and dim/dimnames to be present
    
    x <- structure(.Data = 
             list(g0 = 41, g1 = 42, g2 = 43,
                  g0 = 44, g1 = 45, g2 = 46,
                  g0 = 47, g1 = 48, g2 = 49,
                  g0 = 50, g1 = 51, g2 = 52,
                  g0 = 53, g1 = 54, g2 = 55),
               .Dim = c(3,5),
               .Dimnames = list(
                    c("g0","g1","g2"),
                    c("student","logistic","logWeibull","extreme","Huber")))


    assertThat(dim(x), identicalTo(c(3L, 5L)))
    assertThat(dimnames(x), identicalTo(list(
        c("g0", "g1", "g2"), 
        c("student", "logistic",  "logWeibull", "extreme", "Huber"))))
    
    assertThat(names(x), identicalTo(rep(c("g0", "g1", "g2"), times = 5)))
}    
    