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
### Helpers for environments and evaluation
### -------------------------------------------------------------------------

safeEval <- function(expr, envir, enclos, strict=FALSE) {
  expr <- eval(call("bquote", expr, enclos))
  if (strict) {
    enclos <- makeGlobalWarningEnv(expr, envir, enclos)
  }
  eval(expr, envir, enclos)
}

makeGlobalWarningEnv <- function(expr, envir, enclos) {
  envir <- as.env(envir, enclos)
  globals <- setdiff(all.names(expr, functions=FALSE), ls(envir))
  env <- new.env(parent=enclos)
  lapply(globals, function(g) {
    makeActiveBinding(g, function() {
      val <- get(g, enclos)
      warning("Symbol '", g, "' resolved from calling frame; ",
              "escape with .(", g, ") for safety.")
      val
    }, env)
  })
  env
}

evalArg <- function(expr, envir, ..., where=parent.frame()) {
  enclos <- eval(call("top_prenv", expr, where))
  expr <- eval(call("substitute", expr), where)
  safeEval(expr, envir, enclos, ...)
}

normSubsetIndex <- function(i) {
  i <- try(as.logical(i), silent=TRUE)
  if (inherits(i, "try-error"))
    stop("'subset' must be coercible to logical")
  i & !is.na(i)
}

missingArg <- function(arg, where=parent.frame()) {
  eval(call("missing", arg), where)
}

evalqForSubset <- function(expr, envir, ...) {
  if (missingArg(substitute(expr), parent.frame())) {
    TRUE
  } else {
    i <- evalArg(substitute(expr), envir, ..., where=parent.frame())
    normSubsetIndex(i)
  }
}

evalqForSelect <- function(expr, df, ...) {
  if (missingArg(substitute(expr), parent.frame())) {
    rep(TRUE, ncol(df))
  } else {
    nl <- as.list(seq_len(ncol(df)))
    names(nl) <- colnames(df)
    evalArg(substitute(expr), nl, ..., where=parent.frame())
  }
}

top_prenv <- function(x, where=parent.frame()) {
  sym <- substitute(x)
  if (!is.name(sym)) {
    stop("'x' did not substitute to a symbol")
  }
  if (!is.environment(where)) {
    stop("'where' must be an environment")
  }
  .Call2("top_prenv", sym, where, PACKAGE="S4Vectors")
}

top_prenv_dots <- function(...) {
  .Call("top_prenv_dots", environment(), PACKAGE="S4Vectors")
}

