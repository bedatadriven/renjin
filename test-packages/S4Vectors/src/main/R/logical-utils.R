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

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Basic manipulation of a "compact bit vector" i.e. a bit vector stored in
### a standard raw vector.
###

logicalAsCompactBitvector <- function(x)
{
    if (!is.logical(x))
        stop("'x' must be a logical vector")
    .Call2("logical_as_compact_bitvector", x, PACKAGE="S4Vectors")
}

compactBitvectorAsLogical <- function(x, length.out)
{
    if (!is.raw(x))
        stop("'x' must be a raw vector")
    if (!isSingleNumber(length.out))
        stop("'length.out' must be a single number")
    if (!is.integer(length.out))
        length.out <- as.integer(length.out)
    .Call2("compact_bitvector_as_logical", x, length.out, PACKAGE="S4Vectors")
}

subsetCompactBitvector <- function(x, i)
{
    if (!is.raw(x))
        stop("'x' must be a raw vector")
    if (!is.integer(i))
        stop("'i' must be an integer vector")
    .Call2("subset_compact_bitvector", x, i, PACKAGE="S4Vectors")
}

