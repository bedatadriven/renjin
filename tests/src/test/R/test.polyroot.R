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

assertThat(round(polyroot(c(1, 2, 1))), identicalTo( c(-1-0i, -1+0i)))

assertThat(round(polyroot(choose(8, 0:8))), identicalTo(as.complex(rep(-1, times = 8))))

assertThat(polyroot(c(5,-4,3,-2,-12,12)), identicalTo(
        c(-0.010179915420319+0.722959095782939i,
          -8.54328191187856e-01+8e-16i,
          -0.010179915420323-0.72295909578293i,
          0.937344011014235-0.233064870689384i,
          0.937344011014263+0.233064870689374i), tol = 0.01))

