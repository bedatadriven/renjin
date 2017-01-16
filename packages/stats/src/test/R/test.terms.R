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
z0 <- terms(dt$z1 ~ dt$z1, "strata")
y0 <- attr(z0, "term.labels")
z1 <- terms(dt:z1 ~ dt:z1, "strata")
y1 <- attr(z1, "term.labels")
z2 <- terms(a55 ~ a56 + a57 + a58, "strata")
y2 <- attr(z2, "term.labels")
z3 <- terms(a55 ~ a56 + a57 + a58 : a59 :a60, "strata")
y3 <- attr(z3, "term.labels")
z4 <- terms(a55 ~ a56 + a57 + a58 : a59 : a60 ~ a61, "strata")
y4 <- attr(z4, "term.labels")
test.t.0 <- function() assertThat(y0, equalTo("dt$z1"))
test.t.1 <- function() assertThat(y1, equalTo("dt:z1"))
test.t.2 <- function() assertThat(y2, equalTo( c("a56", "a57", "a58") ))
test.t.3 <- function() assertThat(y3, equalTo( c("a56", "a57", "a58:a59:a60") ))
test.t.4 <- function() assertThat(y4, equalTo( c("a61") ))
