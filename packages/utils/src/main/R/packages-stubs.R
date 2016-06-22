#  File src/library/utils/R/packages.R
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




available.packages <-
  function(contriburl, method, fields = NULL, type = getOption("pkgType"), filters = NULL)
  {
    stop("available.packages() is not implemented. Please see http://packages.renjin.org.")
  }

available_packages_filters_default <- c("R_version", "OS_type", "subarch", "duplicates")
available_packages_filters_db <- new.env(hash = FALSE) # small
available_packages_filters_db$R_version <- function(db) FALSE
available_packages_filters_db$OS_type <- function(db) FALSE
available_packages_filters_db$subarch <- function(db) FALSE
available_packages_filters_db$duplicates <- function(db) FALSE
available_packages_filters_db$`license/FOSS` <- function(db) FALSE


update.packages <- function(lib.loc = NULL, repos = getOption("repos"),
                            contriburl = contrib.url(repos, type),
                            method, instlib = NULL, ask = TRUE,
                            available = NULL, oldPkgs = NULL, ...,
                            checkBuilt = FALSE, type = getOption("pkgType"))
{
  stop(paste("Not supported by Renjin.",
             "Renjin does not maintain a local repository of packages.",
             "Visit http://packages.renjin.org for information on the latest available packages.", 
             sep = "\n"))
}

old.packages <- function(lib.loc = NULL, repos = getOption("repos"),
                         contriburl = contrib.url(repos, type),
                         instPkgs = installed.packages(lib.loc = lib.loc),
                         method, available = NULL, checkBuilt = FALSE,
                         type = getOption("pkgType"))
{
  stop(paste("Not supported by Renjin.",
             "Visit http://packages.renjin.org for information on the latest available packages.", 
             sep = "\n"))
}

new.packages <- function(lib.loc = NULL, repos = getOption("repos"),
                         contriburl = contrib.url(repos, type),
                         instPkgs = installed.packages(lib.loc = lib.loc),
                         method, available = NULL, ask = FALSE,
                         ..., type = getOption("pkgType"))
{
  stop(paste("Not supported by Renjin.",
            "Visit http://packages.renjin.org for information on the latest available packages.", 
            sep = "\n"))
}

installed.packages <-
  function(lib.loc = NULL, priority = NULL, noCache = FALSE,
           fields = NULL, subarch = .Platform$r_arch)
  {
    warning(paste("Renjin does not maintain a local repository of packages. You can use",
                  "library(my.package) to load a package on demand from http://packages.renjin.org"))
    
    invisible(character(0))
    
  }


remove.packages <- function(pkgs, lib)
{
  stop("Not supported by Renjin.\nRenjin does not maintain a local repository of packages.")
}

download.packages <- function(pkgs, destdir, available = NULL,
                              repos = getOption("repos"),
                              contriburl = contrib.url(repos, type),
                              method, type = getOption("pkgType"), ...)
{
  stop(paste("Renjin does not maintain a local repository of packages.",
            "You can use library(my.package) to load a package on demand from ",
            "http://packages.renjin.org", sep="\n"))
  
}

contrib.url <- function(repos, type = getOption("pkgType"))
{
  stop(paste("Renjin does not maintain a local repository of packages. You can use",
             "library(my.package) to load a package on demand from http://packages.renjin.org"))
}


getCRANmirrors <- function(all=FALSE, local.only=FALSE)
{
  stop("Not supported. Renjin maintains its own repository at http://packages.renjin.org")
}


chooseCRANmirror <- function(graphics = getOption("menu.graphics"))
{
  stop("Not supported. Renjin maintains its own repository at http://packages.renjin.org")
}

chooseBioCmirror <- function(graphics = getOption("menu.graphics"))
{
  stop("Not supported. Renjin maintains its own repository at http://packages.renjin.org")  
}

setRepositories <-
  function(graphics = getOption("menu.graphics"), ind = NULL,
           addURLs = character())
{
  stop("Not supported. Renjin maintains its own repository at http://packages.renjin.org")
}


## used in some BioC packages and their support in tools.
compareVersion <- function(a, b)
{
  if(is.na(a)) return(-1L)
  if(is.na(b)) return(1L)
  a <- as.integer(strsplit(a, "[\\.-]")[[1L]])
  b <- as.integer(strsplit(b, "[\\.-]")[[1L]])
  for(k in seq_along(a))
    if(k <= length(b)) {
      if(a[k] > b[k]) return(1) else if(a[k] < b[k]) return(-1L)
    } else return(1L)
  if(length(b) > length(a)) return(-1L) else return(0L)
}


