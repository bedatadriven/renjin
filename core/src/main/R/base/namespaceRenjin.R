
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

isNamespaceLoaded <- function(name) {
	name %in% loadedNamespaces()
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

getNamespaceInfo <- function(ns, which) {
	
	switch(which,
		"imports" = getNamespaceImports(ns),
		"exports" = getNamespaceExports(ns),
		"path" = find.package(ns),
				 	stop(sprintf("getNamespaceInfo(which='%s') not implemented", which)))
}

isBaseNamespace <- function(ns) identical(ns, .BaseNamespaceEnv)

.getNamespace <- function(what) .Internal(getRegisteredNamespace(what))


find.package <- function(package = NULL, lib.loc = NULL, quiet = FALSE, verbose = getOption("verbose")) {
	if(is.null(package)) {
		stop("package = NULL not supported by Renjin.")
	}
	.Internal(find.package(package))
}

loadNamespaceMethods <- function(package, ns, expClasses, expClassPatterns) {
  ## cache generics, classes in this namespace (but not methods itself,
	## which pre-cached at install time
#	methods:::cacheMetaData(ns, TRUE, ns)
	## process class definition objects
#	expClasses <- nsInfo$exportClasses
	##we take any pattern, but check to see if the matches are classes
	pClasses <- character()
	aClasses <- methods:::getClasses(ns)
#	classPatterns <- nsInfo$exportClassPatterns
#	## defaults to exportPatterns
#	if(!length(classPatterns))
#		classPatterns <- nsInfo$exportPatterns
#	for (p in classPatterns) {
#		pClasses <- c(aClasses[grep(p, aClasses)], pClasses)
#	}
	pClasses <- unique(pClasses)
	if( length(pClasses) ) {
		good <- vapply(pClasses, methods:::isClass, NA, where = ns)
		if( !any(good) && length(nsInfo$exportClassPatterns))
			warning(gettextf("exportClassPattern specified in NAMESPACE but no matching classes in package %s", sQuote(package)),
					call. = FALSE, domain = NA)
		expClasses <- c(expClasses, pClasses[good])
	}
	if(length(expClasses)) {
		missingClasses <-
			!vapply(expClasses, methods:::isClass, NA, where = ns)
		if(any(missingClasses))
			stop(gettextf("in package %s classes %s were specified for export but not defined",
						  sQuote(package),
						  paste(expClasses[missingClasses],
								collapse = ", ")),
				 domain = NA)
		expClasses <- paste(methods:::classMetaName(""), expClasses,
							sep = "")
	}
	## process methods metadata explicitly exported or
	## implied by exporting the generic function.
	allGenerics <- unique(c(methods:::.getGenerics(ns),
						   methods:::.getGenerics(parent.env(ns))))
	expMethods <- nsInfo$exportMethods
	expTables <- character()
	if(length(allGenerics)) {
		expMethods <-
			unique(c(expMethods,
					 exports[!is.na(match(exports, allGenerics))]))
		missingMethods <- !(expMethods %in% allGenerics)
		if(any(missingMethods))
			stop(gettextf("in %s methods for export not found: %s",
						  sQuote(package),
						  paste(expMethods[missingMethods],
								collapse = ", ")),
				 domain = NA)
		tPrefix <- methods:::.TableMetaPrefix()
		allMethodTables <-
			unique(c(methods:::.getGenerics(ns, tPrefix),
					 methods:::.getGenerics(parent.env(ns), tPrefix)))
		needMethods <-
			(exports %in% allGenerics) & !(exports %in% expMethods)
		if(any(needMethods))
			expMethods <- c(expMethods, exports[needMethods])
		## Primitives must have their methods exported as long
		## as a global table is used in the C code to dispatch them:
		## The following keeps the exported files consistent with
		## the internal table.
		pm <- allGenerics[!(allGenerics %in% expMethods)]
		if(length(pm)) {
			prim <- logical(length(pm))
			for(i in seq_along(prim)) {
				f <- methods:::getFunction(pm[[i]], FALSE, FALSE, ns)
				prim[[i]] <- is.primitive(f)
			}
			expMethods <- c(expMethods, pm[prim])
		}
		for(i in seq_along(expMethods)) {
			mi <- expMethods[[i]]
			if(!(mi %in% exports) &&
			   exists(mi, envir = ns, mode = "function",
					  inherits = FALSE))
				exports <- c(exports, mi)
			pattern <- paste(tPrefix, mi, ":", sep="")
			ii <- grep(pattern, allMethodTables, fixed = TRUE)
			if(length(ii)) {
	if(length(ii) > 1L) {
		warning(gettextf("multiple methods tables found for %s",
			sQuote(mi)), call. = FALSE, domain = NA)
		ii <- ii[1L]
	}
				expTables[[i]] <- allMethodTables[ii]
			 }
			else { ## but not possible?
			  warning(gettextf("failed to find metadata object for %s",
							   sQuote(mi)), call. = FALSE, domain = NA)
			}
		}
	}
	else if(length(expMethods))
		stop(gettextf("in package %s methods %s were specified for export but not defined",
					  sQuote(package),
					  paste(expMethods, collapse = ", ")),
			 domain = NA)
	exports <- c(exports, expClasses,  expTables)
}

requireNamespace <- function (package, ..., quietly = FALSE) {
    package <- as.character(package)[[1L]]
    ns <- .Internal(getRegisteredNamespace(package))
    res <- TRUE
    if (is.null(ns)) {
        if (!quietly) 
            packageStartupMessage(gettextf("Loading required namespace: %s", 
                package), domain = NA)
        value <- tryCatch(loadNamespace(package, ...), error = function(e) e)
        if (inherits(value, "error")) {
            if (!quietly) {
                msg <- conditionMessage(value)
                cat("Failed with error:  ", sQuote(msg), "\n", 
                  file = stderr(), sep = "")
                .Internal(printDeferredWarnings())
            }
            res <- FALSE
        }
    }
    invisible(res)
}

namespaceExport <- function(ns, vars) {
    namespaceIsSealed <- function(ns)
       environmentIsLocked(ns)
    if (namespaceIsSealed(ns))
        stop("cannot add to exports of a sealed namespace")
    ns <- asNamespace(ns, base.OK = FALSE)
    if (length(vars)) {
        addExports <- function(ns, new) {
            exports <- .getNamespaceInfo(ns, "exports")
            expnames <- names(new)
            objs <- names(exports)
            ex <- expnames %in% objs
            if(any(ex))
                warning(sprintf(ngettext(sum(ex),
                                         "previous export '%s' is being replaced",
                                         "previous exports '%s' are being replaced"),
                                paste(sQuote(expnames[ex]), collapse = ", ")),
                        call. = FALSE, domain = NA)
            list2env(as.list(new), exports)
        }
        makeImportExportNames <- function(spec) {
            old <- as.character(spec)
            new <- names(spec)
            if (is.null(new)) new <- old
            else {
                change <- !nzchar(new)
                new[change] <- old[change]
            }
            names(old) <- new
            old
        }
        new <- makeImportExportNames(unique(vars))
        ## calling exists each time is too slow, so do two phases
        undef <- new[! new %in% names(ns)]
        undef <- undef[! vapply(undef, exists, NA, envir = ns)]
        if (length(undef)) {
            undef <- do.call("paste", as.list(c(undef, sep = ", ")))
            stop(gettextf("undefined exports: %s", undef), domain = NA)
        }
        if(.isMethodsDispatchOn()) .mergeExportMethods(new, ns)
        addExports(ns, new)
    }
}

.getNamespaceInfo <- function(ns, which) {
    ns[[".__NAMESPACE__."]][[which]]
}

.mergeExportMethods <- function(new, ns)
{
    ## avoid bootstrapping issues when using methods:::methodsPackageMetaName("M","")
    ## instead of  ".__M__" :
    newMethods <- new[startsWith(new, ".__M__")]
    nsimports <- parent.env(ns)
    for(what in newMethods) {
	if(!is.null(m1 <- nsimports[[what]])) {
            m2 <- get(what, envir = ns)
            ns[[what]] <- methods::mergeMethods(m1, m2)
        }
    }
}
