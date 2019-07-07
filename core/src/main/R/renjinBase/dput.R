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

# source is mostly unchanged from the version in
# https://github.com/wch/r-source/commit/d767e43b1209ec7167fd7b7b865c7e56be9a14af
dput <-
    function(x, file = "",
             control = c("keepNA", "keepInteger", "showAttributes"))
{
    if(is.character(file))
        if(nzchar(file)) {
            file <- file(file, "wt")
            on.exit(close(file))
        } else file <- stdout()
    opts <- .deparseOpts(control)
    ## FIXME: this should happen in C {deparse2() in ../../../main/deparse.c}
    ##        but we are missing a C-level slotNames()
    ## Fails e.g. if an S3 list-like object has S4 components
    if(isS4(x)) {
        clx <- class(x)
        cat('new("', clx,'"\n', file = file, sep = '')
	      for(n in methods::.slotNames(clx)) {
	          cat("    ,", n, "= ", file = file)
	          dput(methods::slot(x, n), file = file, control = control)
	      }
	      cat(")\n", file = file)
	      invisible()
    } else {
        # Renjin doesn't use the dput primitive function:
        cat(deparse(x), file = file, sep = '')
    }
}

