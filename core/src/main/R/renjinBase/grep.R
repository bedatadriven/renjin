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

# convert split=NULL to split="" to make prettyNum() happy:
strsplit <-
function(x, split, fixed = FALSE, perl = FALSE, useBytes = FALSE) {
    if (is.null(split)) split <- ""
    .Internal(strsplit(x, as.character(split), fixed, perl, useBytes))
}


agrepl <-
function(pattern, x, max.distance = 0.1, costs = NULL,
         ignore.case = FALSE, fixed = TRUE, useBytes = FALSE)
{
    pattern <- as.character(pattern)
    if(!is.character(x)) x <- as.character(x)

    ## TRE needs integer costs: coerce here for simplicity.
    costs <- as.integer(.amatch_costs(costs))
    bounds <- .amatch_bounds(max.distance)

    ind <- .Internal(agrep(pattern, x, ignore.case, FALSE, costs, bounds,
                           useBytes, fixed))

    result <- logical(length(x))

    if (length(ind > 0)) result[ind] <- TRUE

    result
}

