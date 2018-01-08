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

assertThat(sprintf("%i", 1L), identicalTo("1"))
assertThat(sprintf("%f", 1L), identicalTo("1.000000"))
assertThat(sprintf("%E", 1L), identicalTo("1.000000E+00"))
assertThat(sprintf("%e", 1L), identicalTo("1.000000e+00"))
assertThat(sprintf("%g", 1L), identicalTo("1"))
assertThat(sprintf("%G", 1L), identicalTo("1"))

assertThat(sprintf("%g", 102400000L), identicalTo("1.024e+08"))
assertThat(sprintf("%G", 102400000L), identicalTo("1.024E+08"))

assertThat(sprintf('%.f', 3 ), identicalTo("3"))
assertThat(sprintf('%0.f', 3 ), identicalTo("3"))
assertThat(sprintf('%0.f', 3.1 ), identicalTo("3"))
assertThat(sprintf('%0.f', 3.5 ), identicalTo("4"))
assertThat(sprintf('%0.f', 3.75 ), identicalTo("4"))

assertThat(sprintf("0x%x", 64), identicalTo("0x40"))



