
library <- 
		function(name, ...)
{
	getNamespace(name)
}


require <-
		function(package, lib.loc = NULL, quietly = FALSE, warn.conflicts = TRUE,
				keep.source = getOption("keep.source.pkgs"),
				character.only = FALSE)
{
	if (!missing(keep.source))
		warning("'keep.source' is deprecated and will be ignored")
	if(!character.only)
		package <- as.character(substitute(package)) # allowing "require(eda)"
	loaded <- paste("package", package, sep = ":") %in% search()
	
	if (!loaded) {
		if (!quietly)
			packageStartupMessage(gettextf("Loading required package: %s",
							package), domain = NA)
		## value <- tryCatch(library(package, lib.loc = lib.loc,
		##                 character.only = TRUE,
		##                 logical.return = TRUE,
		##                 warn.conflicts = warn.conflicts,
		##                 quietly = quietly),
		##         error = function(e) e)
		value <- tryCatch(library(package),
				error = function(e) e)
		if (inherits(value, "error")) {
			if (!quietly) {
				msg <- conditionMessage(value)
				cat("Failed with error:  ",
						sQuote(msg), "\n", file = stderr(), sep = "")
				.Internal(printDeferredWarnings())
			}
			return(invisible(FALSE))
		}
		if (!value) return(invisible(FALSE))
	} else value <- TRUE
	invisible(value)
}