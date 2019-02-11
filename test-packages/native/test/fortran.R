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
library("org.renjin.test:native")

# Using dynamic lookup, we should be able to call
# fortran methods using any case, and *without* the final underscore
output <- .Fortran("DPCHIM2", 1)
assertThat(output[[1]], identicalTo(99))

output <- .Fortran("DPchim2", 1)
assertThat(output[[1]], identicalTo(99))

# However, if we use lookup from the registration table 
# (see init.c), then the name is always lowercased, but
# *no* underscore will be added.

output <- .Fortran("fmyname", 1)
assertThat(output[[1]], identicalTo(99))

output <- .Fortran("FMYname", 1)
assertThat(output[[1]], identicalTo(99))



