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

### =========================================================================
### Split a vector-like object as a list-like object
### -------------------------------------------------------------------------


setMethod("split", c("list", "Vector"),
    function(x, f, drop=FALSE, ...) split(x, as.vector(f), drop=drop, ...)
)

### The remaining methods delegate to IRanges::splitAsList().

setMethod("split", c("Vector", "ANY"),
    function(x, f, drop=FALSE)
    {
        if (!requireNamespace("IRanges", quietly=TRUE))
            stop("Couldn't load the IRanges package. You need to install ",
                 "the IRanges\n  package in order to split a Vector object.")
        IRanges::splitAsList(x, f, drop=drop)
    }
)

setMethod("split", c("ANY", "Vector"),
    function(x, f, drop=FALSE)
    {
        if (!requireNamespace("IRanges", quietly=TRUE))
            stop("Couldn't load the IRanges package. You need to install ",
                 "the IRanges\n  package in order to split by a Vector object.")
        IRanges::splitAsList(x, f, drop=drop)
    }
)

setMethod("split", c("Vector", "Vector"),
    function(x, f, drop=FALSE)
    {
        if (!requireNamespace("IRanges", quietly=TRUE))
            stop("Couldn't load the IRanges package. You need to install ",
                 "the IRanges\n  package in order to split a Vector object.")
        IRanges::splitAsList(x, f, drop=drop)
    }
)

