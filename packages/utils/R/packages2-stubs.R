#  File src/library/utils/R/packages2.R
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

getDependencies <- function(...)
{
  stop("Not implemented.")
}

install.packages <- function(...)
{
  stop(paste("Not supported.",
  "Renjin does not maintain a local package repository, you can run library(your.package) to automatically",
  "download and load your.package", sep="\n"))  
}


# The following code is copied from https://github.com/wch/r-source/blob/88e0bb260a8bae389f62e3fc6ce2f235e1d4fc98/src/library/utils/R/packages2.R
# and is meant to add dummies for globalVariables() and suppressForeignCheck(), both of which are
# not relevant for Renjin, but are required by many R packages.

## treat variables as global in a package, for codetools & check
globalVariables <- function(names, package, add = TRUE)
    registerNames(names, package, ".__global__", add)

## suppress foreign function checks, for check
suppressForeignCheck <- function(names, package, add = TRUE)
    registerNames(names, package, ".__suppressForeign__", add)

registerNames <- function(names, package, .listFile, add = TRUE) {
    # the actual function in GNU R returns a character vector with the names of the
    # global variables so here we simply return an empty character vector:
    character()
}

# copied from GNU R 3.5.3:
packageName <- function(env = parent.frame()) {
    if (!is.environment(env)) stop("'env' must be an environment")
    env <- topenv(env)
    if (!is.null(pn <- get0(".packageName", envir = env, inherits = FALSE)))
	pn
    else if (identical(env, .BaseNamespaceEnv))
	"base"
    ## else NULL
}


