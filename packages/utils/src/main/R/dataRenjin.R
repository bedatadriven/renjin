#  File src/library/utils/R/data.R
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

data <-
		function(..., list = character(), package = NULL, lib.loc = NULL,
				verbose = getOption("verbose"), envir = .GlobalEnv)
{
	

	names <- c(as.character(substitute(list(...))[-1L]), list)
	
	s <- search()
	packages <- substring(s[substr(s, 1L, 8L) == "package:"], 9)
	
	for(name in names) {
		found <- FALSE
		for(pkg in packages) {
			dataset <- .Internal(getDataset(pkg, name))
			if(!is.null(dataset)) {
				found <- TRUE
				envir[[name]] <- dataset
				break;
			}
		}
		if(!found) {
			stop(paste("Could not find dataset '", name, "'", sep=""))
		}
	}
}
