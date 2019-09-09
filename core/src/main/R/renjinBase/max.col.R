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

# Pure R replacement for the original max.col function which calls to the C implementation R_max_col:
max.col <- function(m, ties.method = c("random", "first", "last")) {

  ties.method <- match.arg(ties.method)

  unname(apply(as.matrix(m), 1, function(row) {
    maxVal <- max(row)

    # max.col doesn't have the na.rm argument:
    if (is.na(maxVal)) return(NA_integer_)

    i <- which(row == maxVal)
    # no ties:
    if (length(i) == 1L) return(i)
    # ties:
    k <- switch(ties.method,
           "random" = sample(i, 1),
           "first" = i[1],
           "last" = i[length(i)])
  }))
}