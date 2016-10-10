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

### =========================================================================
### Miscellaneous low-level utils
### -------------------------------------------------------------------------
###
### Unless stated otherwise, nothing in this file is exported.
###


### Wrap the message in lines that don't exceed the terminal width (obtained
### with 'getOption("width")'). Usage:
###   stop(wmsg(...))
###   warning(wmsg(...))
###   message(wmsg(...))
wmsg <- function(...)
    paste0(strwrap(paste0(c(...), collapse="")), collapse="\n  ")

errorIfWarning <- function(expr)
{
    old_options <- options(warn=2)        
    on.exit(options(old_options))
    eval(expr)
}

.AEbufs_use_malloc <- function(x)
    .Call("AEbufs_use_malloc", x, PACKAGE="S4Vectors")

.AEbufs_free <- function()
    .Call("AEbufs_free", PACKAGE="S4Vectors")

### Exported!
.Call2 <- function(.NAME, ..., PACKAGE)
{
    ## Uncomment the 2 lines below to switch from R_alloc- to malloc-based
    ## Auto-Extending buffers.
    #.AEbufs_use_malloc(TRUE)
    #on.exit({.AEbufs_free(); .AEbufs_use_malloc(FALSE)})    
    .Call(.NAME, ..., PACKAGE=PACKAGE)
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Functional fun
###

Has <- function(FUN) {
  function(x) {
    !is.null(FUN(x))
  }
}

