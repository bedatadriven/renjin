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

pos.to.env <- function(x) {
    if(x == -1) {
        return(parent.frame(n = 2))
    }
    if(x < 1) {
        stop("invalid 'pos' argument")
    }
    env <- .GlobalEnv
    while(x > 1) {
        if(identical(env, emptyenv())) {
            stop("invalid 'pos' environment")
        }
        env <- parent.env(env)
        x <- x - 1
    }
    env
}