#  File src/library/tools/R/zzz.R
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

.noGenerics <- TRUE

# RENJIN EDITS: no dynamic libs

#library.dynam("tools", "tools", .Library)
.onUnload <-
function(libpath)
    library.dynam.unload("tools", libpath)

## These are created at install time: the numbers are hard-coded in signals.c
## They happen to be the BSD ones as this started in multicore
# RENJIN EDITS: no signals !

SIGHUP <- 
SIGINT <- 
SIGQUIT <-
SIGKILL <- 
SIGTERM <- 
SIGSTOP <- 
SIGTSTP <- 
SIGCONT <-
SIGCHLD <-
SIGUSR1 <- 
SIGUSR2 <- 0L

# RENJIN EDITS:
# we're not doing latex parsing for the moment

latexArgCount <- integer()              # The next line modifies this
latexTable <- list() 
#latexTable <- makeLatexTable(utf8table)  # FIXME: Should latexTable be hardcoded instead?
