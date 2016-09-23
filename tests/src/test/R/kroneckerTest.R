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

test.kronecker <- function() {
    # S4 Method Dispatch to base function
    # From ACD package examples
    
    assertThat( kronecker(diag(3),t(rep(1,3))), 
        identicalTo(structure(
              c(1, 0, 0, 
                1, 0, 0, 
                1, 0, 0, 
                0, 1, 0, 
                0, 1, 0, 
                0, 1, 0, 
                0, 0, 1, 
                0, 0, 1,
                0, 0, 1), .Dim = c(3L, 9L))))


}