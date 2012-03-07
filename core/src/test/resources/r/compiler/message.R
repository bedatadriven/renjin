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
