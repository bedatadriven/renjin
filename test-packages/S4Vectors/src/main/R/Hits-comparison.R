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
### Comparing and ordering hits
### -------------------------------------------------------------------------
###


.compatible_Hits <- function(x, y)
{
    nLnode(x) == nLnode(y) && nRnode(x) == nRnode(y)
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### pcompare()
###
### Hits are ordered by 'from' first and then by 'to'.
### This way, the space of hits is totally ordered.
###

setMethod("pcompare", c("Hits", "Hits"),
    function(x, y)
    {
        if (!.compatible_Hits(x, y))
            stop("'x' and 'y' are incompatible Hits objects ",
                 "by number of left and/or right nodes")
        pcompareIntegerPairs(from(x), to(x), from(y), to(y))
    }
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### match()
###

setMethod("match", c("Hits", "Hits"),
    function(x, table, nomatch=NA_integer_, incomparables=NULL,
                       method=c("auto", "quick", "hash"))
    {
        if (!.compatible_Hits(x, table))
            stop("'x' and 'y' are incompatible Hits objects ",
                 "by number of left and/or right nodes")
        if (!is.null(incomparables))
            stop("\"match\" method for Hits objects ",
                 "only accepts 'incomparables=NULL'")
        matchIntegerPairs(from(x), to(x),
                          from(table), to(table),
                          nomatch=nomatch, method=method)
    }
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### selfmatch()
###
### Is this useful? When do we have to deal with duplicated hits in a Hits
### object? Which function return that? Would be good to know the use case.
### If there aren't any (and we don't expect any in the future), maybe we
### should enforce unicity in the validity method for Hits objects. Then
### selfmatch(), duplicated(), and unique() become pointless on Hits objects
### because their output is predictable (and thus they can be implemented
### in a trivial way that is very fast).
###

#setMethod("selfmatch", "Hits",
#    function (x, method=c("auto", "quick", "hash"))
#        selfmatchIntegerPairs(from(x), to(x), method=method)
#)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Ordering hits
###
### order(), sort(), rank() on Hits objects are consistent with the order
### on hits implied by pcompare().
###

### TODO: Maybe add a method for SortedByQueryHits that takes advantage of
### the fact that Hits objects are already sorted by 'from'.
### 'na.last' is pointless (Hits objects don't contain NAs) so is ignored.
### 'method' is also ignored at the moment.
setMethod("order", "Hits",
    function(..., na.last=TRUE, decreasing=FALSE, method=c("shell", "radix"))
    {
        if (!isTRUEorFALSE(decreasing))
            stop("'decreasing' must be TRUE or FALSE")
        ## All arguments in '...' are guaranteed to be Hits objects.
        args <- list(...)
        if (length(args) == 1L) {
            x <- args[[1L]]
            return(orderIntegerPairs(from(x), to(x), decreasing=decreasing))
        }
        print(111)
        order_args <- vector("list", 2L * length(args));print(order_args)
        idx <- 2L * seq_along(args); print(idx)
        order_args[idx - 1L] <- lapply(args, from); print(order_args)
        order_args[idx] <- lapply(args, to); print(order_args)
        print(116);
        do.call(order, c(order_args, list(na.last=TRUE, decreasing=FALSE, method="shell")))
    }
)

