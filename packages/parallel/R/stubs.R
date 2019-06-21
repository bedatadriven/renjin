#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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


.reg <-  new.env()
assign("default", NULL, envir = .reg)



checkForRemoteErrors <- function() {
    # NO OP
}

defaultCluster <- function(cl = NULL)
{
    if(is.null(cl)) cl <- get("default", envir = .reg)
    if(is.null(cl)) stop("no cluster 'cl' supplied and none is registered")
    checkCluster(cl)
    cl
}

setDefaultCluster <- function(cl = NULL)
{
    if(!is.null(cl)) checkCluster(cl)
    assign("default", cl, envir = .reg)
}


#
# Checking and subsetting
#

checkCluster <- function(cl)
    if (!inherits(cl, "cluster")) stop("not a valid cluster");

`[.cluster` <- function(cl, ...) {
    v <- NextMethod()
    class(v) <- class(cl)
    v
}


sendCall <- function (con, fun, args, return = TRUE, tag = NULL)
{
    timing <-  .snowTimingData$running()
    if (timing)
        start <- proc.time()[3L]
    postNode(con, "EXEC",
             list(fun = fun, args = args, return = return, tag = tag))
    if (timing)
        .snowTimingData$enterSend(con$rank, start, proc.time()[3L])
    NULL
}

makeCluster <- function (spec, type, ...) 
{
    NULL
}
