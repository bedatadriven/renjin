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



# Source: http://gallery.rcpp.org/articles/parallel-distance-matrix/

n  = 1000
m = matrix(runif(n*10), ncol = 10)
m = m/rowSums(m)

js_distance <- function(mat) {
    kld = function(p,q) sum(ifelse(p == 0 | q == 0, 0, log(p/q)*p))
    res = matrix(0, nrow(mat), nrow(mat))
    for (i in 1:(nrow(mat) - 1)) {
        for (j in (i+1):nrow(mat)) {
            m = (mat[i,] + mat[j,])/2
            d1 = kld(mat[i,], m)
            d2 = kld(mat[j,], m)
            res[j,i] = sqrt(.5*(d1 + d2))
        }
    }
    res
}

r_res <- js_distance(m)
