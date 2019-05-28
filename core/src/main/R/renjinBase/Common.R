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


# Renjin added from src/library/profile/Common.R
# NOTE(ab) these seem pretty essential to the language so it's not clear to me why
# they should be in (nominally) configurable system profile
.GlobalEnv <- globalenv()
.AutoloadEnv <- as.environment(2)
assign(".Autoloaded", NULL, envir = .AutoloadEnv)
T <- TRUE
F <- FALSE
R.version <- structure(R.Version(), class = "simple.list")
version <- R.version            # for S compatibility

.onLoad <- function() {

	options(keep.source = interactive())
	options(warn = 0)
	options(timeout = 60)
	options(encoding = "native.enc")
	options(show.error.messages = TRUE)
	## keep in sync with PrintDefaults() in  ../../main/print.c :
	options(scipen = 0)
	options(max.print = 99999)# max. #{entries} in internal printMatrix()
	options(add.smooth = TRUE)# currently only used in 'plot.lm'
	options(stringsAsFactors = TRUE)
	if(!interactive() && is.null(getOption("showErrorCalls")))
		options(showErrorCalls = TRUE)


    makeActiveBinding(".Options", function() {
     as.pairlist(options())
    }, baseenv())
}
