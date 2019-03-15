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

test.trimws1 <- function() {
  assertThat( trimws(1), identicalTo("1") )
  assertThat( trimws(list(x = seq(10))), identicalTo("1:10") )
  assertThat( trimws(data.frame(x = seq(10))), identicalTo("1:10") )
}

test.trimws2 <- function() {
  x <- "  R in the JVM.  "
  assertThat( trimws(x), identicalTo("R in the JVM.") )
  assertThat( trimws(x, which = "left"), identicalTo("R in the JVM.  ") )
  assertThat( trimws(x, "l"), identicalTo("R in the JVM.  ") )
  assertThat( trimws(x, which = "right"), identicalTo("  R in the JVM.") )
  assertThat( trimws(x, "r"), identicalTo("  R in the JVM.") )
}

test.trimws3 <- function() {
  x <- c(" Low   ", "Moderate  ", "  High")
  assertThat( trimws(x), identicalTo(c("Low", "Moderate", "High")) )
  assertThat( trimws(x, which = "left"), identicalTo(c("Low   ", "Moderate  ", "High")) )
  assertThat( trimws(x, "l"), identicalTo(c("Low   ", "Moderate  ", "High")) )
  assertThat( trimws(x, which = "right"), identicalTo(c(" Low", "Moderate", "  High")) )
  assertThat( trimws(x, "r"), identicalTo(c(" Low", "Moderate", "  High")) )
}