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

test.negativeComplex <- function() {
	z <- 1+1i
	assertThat( -z, equalTo( complex(real = -1, imaginary = -1)))
}

test.operations <- function() {
    assertThat(Re(eigen(matrix(c(3, 4, -2, -1),2))$vectors[1]), closeTo(0.4082483, 1e-6))
    assertThat(Im(as.complex(1)), identicalTo(0))
    assertThat(Re(as.complex(1)), identicalTo(1))
    # assertThat(Im(sqrt(as.complex(-1))), identicalTo(1))
    assertThat(Mod(1+1i), closeTo(1.4142136, 1e-6))
    assertThat(Im(1+1i + 1+3i), identicalTo(4.0))
    assertThat(Im((1+1i) - (1+3i)), identicalTo(-2.0))
    assertThat(Im(1+1i * 1+3i), identicalTo(4.0))
    assertThat(Re((1+1i) * (1+3i)), identicalTo(-2.0))
}
