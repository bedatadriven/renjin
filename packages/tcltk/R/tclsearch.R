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


addTclPath <- function(path = ".")
{
    ## Tcl uses Unix-style paths on Windows
    if(.Platform$OS.type == "windows")
        path <- gsub("\\\\", "/", path)
    a <- tclvalue(tcl("set", "auto_path"))
    paths <- strsplit(a, " ", fixed=TRUE)[[1L]]
    if (! path %in% paths)
        tcl("lappend", "auto_path", path)
    invisible(paths)
}

tclRequire <- function(package, warn = TRUE)
{
    warning("Tcl/tk is not supported by Renjin")
    return(FALSE)
}
