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

test.sum.na.rm <- function() {
    assertThat(sum(c(1:3, NA), na.rm="T"), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm="TR"), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm="TRUE"), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm="FOOBAR"), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=NA), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=1), identicalTo(6L))

    assertThat(sum(c(1:3, NA), na.rm="F"), identicalTo(NA_integer_))
    assertThat(sum(c(1:3, NA), na.rm="false"), identicalTo(NA_integer_))
    assertThat(sum(c(1:3, NA), na.rm="FALSE"), identicalTo(NA_integer_))
    assertThat(sum(c(1:3, NA), na.rm="FFFFO"), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=0), identicalTo(NA_integer_))

    assertThat(sum(c(1:3, NA), na.rm=quote(x)), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=.GlobalEnv), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=integer(0)), identicalTo(6L))
    assertThat(sum(c(1:3, NA), na.rm=NULL), identicalTo(6L))

}