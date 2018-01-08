#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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


# Renjin does not take the same approach as GNU R,
# which introduced the idea of "compiled" bytecode expressions

# However, some packages use these functions, generally during
# the evaluation of the namespace, so we need substitutes that
# have the same affects

compile <- function(e, env = .GlobalEnv, options = NULL) {
    e
}

cmpfun <- function (f, options = NULL) {
    stopifnot(typeof(f) == "closure")
    return(f)
}

enableJIT <- function(level) {
    warning("Renjin's JIT compiler behaves differently than that of GNU R and cannot be interactively enabled")
    level
}

compilePKGS <- function(enable) {
    warning("Renjin's JIT compiler activates at runtime, and does not need to be enabled for packages")
    FALSE
}

