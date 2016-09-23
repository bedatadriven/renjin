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

message <-
		function(..., domain = NULL, appendLF = TRUE)
{
	## args <- list(...)
	## cond <- if (length(args) == 1L && inherits(args[[1L]], "condition")) {
	##             if(nargs() > 1L)
	##                 warning("additional arguments ignored in message()")
	##             args[[1L]]
	##         } else {
				msg <- .makeMessage(..., domain=domain, appendLF = appendLF)
				## call <- sys.call()
				## simpleMessage(msg, call)
			## }
	## defaultHandler <- function(c) {
	##     ## Maybe use special connection here?
	##     cat(conditionMessage(c), file=stderr(), sep="")
	## }
	## withRestarts({
	##     signalCondition(cond)
	##     ## We don't get to the default handler if the signal
	##     ## is handled with a non-local exit, e.g. by
	##     ## invoking the muffleMessage restart.
	##     defaultHandler(cond)
	## }, muffleMessage = function() NULL)
	# invisible()
}
