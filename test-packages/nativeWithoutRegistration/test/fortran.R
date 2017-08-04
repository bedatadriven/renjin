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

data <- c(0, 1, 2)

# Fortran calling convention is case-insensitive
output <- .Fortran("fortsub", data, length(data))
assertThat(output[[1]], identicalTo(c(999, 1, 2)))
assertThat(data, identicalTo(c(0, 1, 2)))

# So case should not matter
data <- c(100, 200, 300)
output <- .Fortran("FORTSUB", data, length(data))
assertThat(output[[1]], identicalTo(c(999, 200, 300)))
assertThat(data, identicalTo(c(100, 200, 300)))


