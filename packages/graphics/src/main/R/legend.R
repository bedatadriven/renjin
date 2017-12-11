#  File src/library/graphics/R/legend.R
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

legend <-
function(x, y = NULL, legend, fill = NULL, col = par("col"), border="black",
         lty, lwd, pch, angle = 45, density = NULL, bty = "o", bg = par("bg"),
         box.lwd = par("lwd"), box.lty = par("lty"), box.col = par("fg"),
	 pt.bg = NA, cex = 1, pt.cex = cex, pt.lwd = lwd,
	 xjust = 0, yjust = 1, x.intersp = 1, y.intersp = 1, adj = c(0, 0.5),
	 text.width = NULL, text.col = par("col"), text.font = NULL,
	 merge = do.lines && has.pch, trace = FALSE,
	 plot = TRUE, ncol = 1, horiz = FALSE, title = NULL,
	 inset = 0, xpd, title.col = text.col, title.adj = 0.5,
         seg.len = 2)
{
    warning("graphics are not yet implemented.")
}
