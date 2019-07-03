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


.bincode <- function(x, breaks, right = TRUE, include.lowest = FALSE) {
    code <- rep.int(NA_integer_, times=length(x))
    n <- length(breaks)
    if(n == 1) {
        if(include.lowest) {
            code[ x == breaks ] <- 1L
        }
    } else if(n >= 2) {
        if(right) {

            # Open on the left, closed on the right

            if(include.lowest) {
                code[ x >= breaks[1] & x <= breaks[2] ] <- 1L
            } else {
                code[ x >  breaks[1] & x <= breaks[2] ] <- 1L
            }

            for(i in seq(from=2, n-1)) {
                code[ x > breaks[i] & x <= breaks[i+1] ] <- i
            }

        } else {

            # Closed on the left, open on the right

            for(i in seq(from=1, n-2)) {
                code[ x >= breaks[i] & x < breaks[i+1] ] <- i
            }

            if(include.lowest) {
                code[ x >= breaks[n-1] & x <= breaks[n] ] <- n-1L
            } else {
                code[ x >= breaks[n-1] & x < breaks[n] ] <- n-1L
            }
        }
    }
    code
}
