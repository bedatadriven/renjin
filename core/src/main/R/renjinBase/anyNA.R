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


# anyNA
# Introduced in R-3.1.0, see the release notes at http://cran.r-project.org/src/base/NEWS
# The 'recursive' argument was added in R-3.2.0
anyNA <- function(x, recursive = FALSE) {
    UseMethod("anyNA")
}

anyNA.default <- function(x, recursive = FALSE) {
    if (isTRUE(recursive)) x <- unlist(x)
    any(is.na(x))
}

anyNA.numeric_version <- function(x, recursive = FALSE) {
    anyNA(.encode_numeric_version(x))
}

anyNA.POSIXlt <-  function(x, recursive = FALSE) {
    anyNA(as.POSIXct(x))
}

