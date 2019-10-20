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

rowsum.default <- function(x, group, reorder = TRUE, na.rm = FALSE, ...)
{
    if (!is.numeric(x)) stop("'x' must be numeric")
    if (length(group) != NROW(x)) stop("incorrect length for 'group'")
    if (anyNA(group)) warning("missing values for 'group'")
    ugroup <- unique(group)
    if (reorder) ugroup <- sort(ugroup, na.last = TRUE, method = "quick")

    res <- do.call(rbind, lapply(split(as.data.frame(x), factor(group, levels = ugroup)), colSums, na.rm = na.rm))
    colnames(res) <- NULL
    res
}

rowsum.data.frame <- function(x, group, reorder = TRUE, na.rm = FALSE, ...)
{
    if (!is.data.frame(x)) stop("not a data frame") ## make MM happy
    if (length(group) != NROW(x)) stop("incorrect length for 'group'")
    if (anyNA(group)) warning("missing values for 'group'")
    ugroup <- unique(group)
    if (reorder) ugroup <- sort(ugroup, na.last = TRUE, method = "quick")

    if (!all(sapply(x, is.numeric))) stop("non-numeric data frame in rowsum")

    res <- do.call(rbind, lapply(split(x, factor(group, levels = ugroup)), colSums, na.rm = na.rm))
    colnames(res) <- NULL
    res
}

