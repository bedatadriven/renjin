#  File src/library/base/R/library.R
#  Part of the R package, http://www.R-project.org
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
#  http://www.r-project.org/Licenses/


library <-
function(package, help, pos = 2, lib.loc = NULL, character.only = FALSE,
         logical.return = FALSE, warn.conflicts = TRUE,
	 quietly = FALSE, verbose = getOption("verbose"))
{
    if(!missing(lib.loc)) {
        warning("Renjin ignores the library(lib.loc) argument.");
    }

    if(!missing(help)) {
        if(!character.only)
            help <- as.character(substitute(help)) # allowing "require(eda)"

        return(.library.help(help[1L]))          # only give help on one package
    }

    if(!character.only)
        package <- as.character(substitute(package)) # allowing "require(eda)"

    .Internal(library(package))
 
    if (logical.return)
	    TRUE
    else 
        invisible(.packages())
}

.library.help <- function(pkgName) {
    pkgPath <- find.package(pkgName, lib.loc, verbose = verbose)

    # Read and format the description file from package.rds
    # if it exists
    description <- local({
        f <- file.path(pkgPath, "Meta", "package.rds")
        if(file.exists(f)) {
            txt <- readRDS(f)$DESCRIPTION
            nm <- paste0(names(txt), ":")
            formatDL(nm, txt, indent = max(nchar(nm, "w")) + 3)
        }
    })

    # We don't currently build help files, but some packages (affy)
    # expect something to be here, so add some blank lines...
    index <- c("", "", "")
    vignettes <- NULL


    y <- list(name = pkgName, path = pkgPath, info = list(description, index, vignettes))
    class(y) <- "packageInfo"
    return(y)
}

format.libraryIQR <-
function(x, ...)
{
    db <- x$results
    if(!nrow(db)) return(character())
    ## Split according to LibPath, preserving order of libraries.
    libs <- db[, "LibPath"]
    libs <- factor(libs, levels = unique(libs))
    out <- lapply(split(1 : nrow(db), libs),
                  function(ind) db[ind, c("Package", "Title"),
                                   drop = FALSE])
    c(unlist(Map(function(lib, sep) {
        c(gettextf("%sPackages in library %s:\n", sep, sQuote(lib)),
          formatDL(out[[lib]][, "Package"],
                   out[[lib]][, "Title"]))
    },
                 names(out),
                 c("", rep.int("\n", length(out) - 1L)))),
      x$footer)
}

print.libraryIQR <-
function(x, ...)
{
    s <- format(x)
    if(!length(s)) {
        message("no packages found")
    } else {
        outFile <- tempfile("RlibraryIQR")
        writeLines(s, outFile)
        file.show(outFile, delete.file = TRUE,
                  title = gettext("R packages available"))
    }
    invisible(x)
}

require <-
function(package, lib.loc = NULL, quietly = FALSE, warn.conflicts = TRUE,
         character.only = FALSE, save = FALSE)
{   
    if(!missing(lib.loc)) {
         warning("Renjin ignores the library(lib.loc) argument.");
    }
    if(!character.only)
        package <- as.character(substitute(package)) # allowing "require(eda)"
        
    loaded <- paste("package", package, sep = ":") %in% search()

    if (!loaded) {
        invisible(.Internal(require(package)))
       
    } else {
        invisible(TRUE)
    }
}


.packages <- function(all.available = FALSE, lib.loc = NULL) {
	if(all.available) {
		warning(".packages(all.available=TRUE) not supported by Renjin.")
	}
	s <- search()
    return(invisible(substring(s[substr(s, 1L, 8L) == "package:"], 9)))
}


path.package <-
function(package = NULL, quiet = FALSE)
{
    if(is.null(package)) package <- .packages()
    if(length(package) == 0L) return(character())
    s <- search()
    searchpaths <-
        lapply(seq_along(s), function(i) attr(as.environment(i), "path"))
    searchpaths[[length(s)]] <- system.file()
    pkgs <- paste("package", package, sep = ":")
    pos <- match(pkgs, s)
    if(any(m <- is.na(pos))) {
        if(!quiet) {
            if(all(m))
                stop("none of the packages are loaded")
            else
                warning(sprintf(ngettext(as.integer(sum(m)),
                                         "package %s is not loaded",
                                         "packages %s are not loaded"),
                                paste(package[m], collapse=", ")),
                        domain = NA)
        }
        pos <- pos[!m]
    }
    unlist(searchpaths[pos], use.names = FALSE)
}

## As from 2.9.0 ignore versioned installs
find.package <-
function(package = NULL, lib.loc = NULL, quiet = FALSE,
         verbose = getOption("verbose"))
{   
    stop("TODO: Not yet implemented.");
}

format.packageInfo <-
function(x, ...)
{
    if(!inherits(x, "packageInfo")) stop("wrong class")
    vignetteMsg <-
        gettextf("Further information is available in the following vignettes in directory %s:",
                 sQuote(file.path(x$path, "doc")))
    headers <- sprintf("\n%s\n",
                       c(gettext("Description:"),
                         gettext("Index:"),
                         paste(strwrap(vignetteMsg), collapse = "\n")))
    formatDocEntry <- function(entry) {
        if(is.list(entry) || is.matrix(entry))
            formatDL(entry, style = "list")
        else
            entry
    }
    c(gettextf("\n\t\tInformation on package %s", sQuote(x$name)),
      unlist(lapply(which(!vapply(x$info, is.null, NA)),
                    function(i)
                        c(headers[i], formatDocEntry(x$info[[i]])))))

}

print.packageInfo <-
function(x, ...)
{
    outFile <- tempfile("RpackageInfo")
    writeLines(format(x), outFile)
    file.show(outFile, delete.file = TRUE,
              title =
              gettextf("Documentation for package %s", sQuote(x$name)))
    invisible(x)
}

.getRequiredPackages <-
function(file="DESCRIPTION", lib.loc = NULL, quietly = FALSE, useImports = FALSE)
{
    ## OK to call tools as only used during installation.
    pkgInfo <- tools:::.split_description(tools:::.read_description(file))
    .getRequiredPackages2(pkgInfo, quietly, lib.loc, useImports)
    invisible()
}

.getRequiredPackages2 <-
function(pkgInfo, quietly = FALSE, lib.loc = NULL, useImports = FALSE)
{
    stop("TODO: Not yet implemented")
}

.expand_R_libs_env_var <-
function(x)
{
    v <- paste(R.version[c("major", "minor")], collapse = ".")

    expand <- function(x, spec, expansion)
        gsub(paste0("(^|[^%])(%%)*%", spec),
             sprintf("\\1\\2%s", expansion), x)

    ## %V => version x.y.z
    x <- expand(x, "V", v)
    ## %v => version x.y
    x <- expand(x, "v", sub("\\.[^.]*$", "", v))
    ## %p => platform
    x <- expand(x, "p", R.version$platform)
    ## %a => arch
    x <- expand(x, "a", R.version$arch)
    ## %o => os
    x <- expand(x, "o", R.version$os)

    gsub("%%", "%", x)
}