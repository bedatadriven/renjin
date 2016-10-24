#  File src/library/graphics/R/polygon.R
#  Part of the R package, http://www.R-project.org
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  A copy of the GNU General Public License is available at
#  http://www.r-project.org/Licenses/

### polyhatch -  a pure R implementation of polygon hatching
### Copyright (C) 2001 by Kevin Buhr <buhr@stat.wisc.edu>
### Provided to the R project for release under GPL.
### Original nice clean structure destroyed by Ross Ihaka

polygon <-
  function(x, y = NULL, density = NULL, angle = 45,
           border = NULL, col = NA, lty = par("lty"), ..., fillOddEven=FALSE)
{
  warning("graphics are not yet implemented.\n")
}

xspline <-
  function(x, y = NULL, shape = 0, open = TRUE, repEnds = TRUE,
           draw = TRUE, border = par("fg"), col = NA, ...)
{
    xy <- xy.coords(x, y)
    s <- rep.int(shape, length(xy$x))
    if(open) s[1L] <- s[length(x)] <- 0
    warning("graphics are not yet implemented.\n")
}

polypath <-
  function(x, y = NULL, 
           border = NULL, col = NA, lty = par("lty"),
           rule = "winding", ...)
{
    xy <- xy.coords(x, y)
    if (is.logical(border)) {
        if (!is.na(border) && border) border <- par("fg")
        else border <- NA
    }
    rule <- match(rule, c("winding", "evenodd"))
    if (is.na(rule))
        stop("Invalid fill rule for graphics path")
    # Determine path components
    breaks <- which(is.na(xy$x) | is.na(xy$y))
    if (length(breaks) == 0) { # Only one path
        warning("graphics are not yet implemented.")
    } else {
        nb <- length(breaks)
        lengths <- c(breaks[1] - 1,
                     diff(breaks) - 1,
                     length(xy$x) - breaks[nb])
        warning("graphics are not yet implemented.")
    }
}

