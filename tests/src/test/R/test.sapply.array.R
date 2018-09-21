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

assertThat(sapply(1:3, function(i) matrix(c(1, i), nrow = 2)),
        identicalTo(structure(c(1, 1, 1, 2, 1, 3), .Dim = 2:3)))

assertThat(sapply(1:3, function(i) c(1, i)),
        identicalTo(structure(c(1, 1, 1, 2, 1, 3), .Dim = 2:3)))

assertThat(sapply(1:3, function(i) c(a=1, b=i)),
        identicalTo(structure(c(1, 1, 1, 2, 1, 3), .Dim = 2:3, .Dimnames = list(c("a",  "b"), NULL))))

assertThat(sapply(1:3, simplify = "array", function(i) matrix(c(1, i), nrow = 2)),
        identicalTo(structure(c(1, 1, 1, 2, 1, 3), .Dim = c(2L, 1L, 3L))))

