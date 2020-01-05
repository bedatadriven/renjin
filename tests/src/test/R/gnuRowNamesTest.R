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

# Ensure that we can read serialized data.frames from GNU R
# With the row.names hack

test.readGnuDataFrames <- function() {
  
  df <- readRDS("gnuRowNames.rds")
  assertThat(nrow(df), identicalTo(10L))
  assertThat(attr(df, 'row.names'), identicalTo(1:10))
  assertThat(df$a, identicalTo(1:10))
  assertThat(df$b, identicalTo(factor(letters[1:10])))
}