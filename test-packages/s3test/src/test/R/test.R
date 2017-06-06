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
library(methods)
library("org.renjin.test.s3test")

c1 <- new.circle(1)
assertThat(as.character(c1), equalTo("circle of radius 1"))
assertThat(area(c1), closeTo(pi, 0.001))

sq <- new.square(2)
assertThat(as.character(sq), equalTo("2x2 square"))
assertThat(area(sq), equalTo(4))


c2 <- new.circle(4)
assertThat(c1 %/% c2, equalTo(1+4))

city <- new("City", new.env(hash = TRUE, parent = emptyenv()) )
assertThat(city[[]], identicalTo( 300 ))
city[["a"]] <- 1
assertThat(city@.xData$a, identicalTo( 1 ))
assertThat(city[["a"]], identicalTo( 1 ))