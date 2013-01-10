
# most of the namespace functionality has been moved out of "user land"
# but some functions defined in namespace.R are neeeded elsewhere

topenv <- function(envir = parent.frame(),
		matchThisEnv = getOption("topLevelEnvironment")) {
	while (! identical(envir, emptyenv())) {
		nm <- attributes(envir)[["names", exact = TRUE]]
		if ((is.character(nm) && length(grep("^package:" , nm))) ||
				## matchThisEnv is used in sys.source
				identical(envir, matchThisEnv) ||
				identical(envir, .GlobalEnv) ||
				identical(envir, baseenv()) ||
				isNamespace(envir) ||
				## packages except base and those with a separate namespace have .packageName
				exists(".packageName", envir = envir, inherits = FALSE))
			return(envir)
		else envir <- parent.env(envir)
	}
	return(.GlobalEnv)
}

loadNamespace <- function(package) {
	getNamespace(package)
}

asNamespace <- function(ns, base.OK = TRUE) {
	if (is.character(ns) || is.name(ns))
		ns <- getNamespace(ns)
	if (! isNamespace(ns))
		stop("not a name space")
	else if (! base.OK && isBaseNamespace(ns))
		stop("operation not allowed on base name space")
	else ns
}

isBaseNamespace <- function(ns) identical(ns, .BaseNamespaceEnv)
