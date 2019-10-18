#  File src/library/base/R/lazyload.R
#  Part of the R package, https://www.R-project.org
#
#  Copyright (C) 1995-2015 The R Core Team
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
#  https://www.R-project.org/Licenses/

## This code should be kept in step with code in ../baseloader.R
##
## This code has been factored in a somewhat peculiar way to allow the
## lazy load data base mechanism to be used for storing processed .Rd
## files. This isn't quite right as the .Rd use only uses the data
## base, not the lazy load part, but for now it will do. LT

lazyLoadDBexec <- function(filebase, fun, filter)
{
	stop("this function is for internal use in R only; it is not available in Renjin")
}

lazyLoad <- function(filebase, envir = parent.frame(), filter)
{
	stop("this function is for internal use in R only; it is not available in Renjin")
}
