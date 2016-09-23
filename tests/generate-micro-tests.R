#!/usr/bin/R
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


# This script takes the micro test list as input, and generates
# a set of expected values using GNU R.

# Evaluate each test expression and store the result as an
# expectation for future runs

# Run in an anonymous closure because we clear the global
# environment before each test
(function() {

    test.out <- file("src/test/R/micro-tests.R", "wt")
    cat("# Auto-generated from micro-tests.in\n")


    test.sources <- readLines("src/test/resources/micro-tests.in")
    for(i in seq_along(test.sources)) {

        # Clear the global environment
        rm(list=ls(envir=.GlobalEnv), envir=.GlobalEnv)

        test.source <- test.sources[[i]]
        if( !grepl(x=test.source,pattern="^#")) {

            test.sexp <- tryCatch(parse(text=test.source),
                error = function(e) { NULL })

            if(is.null(test.sexp)) {
                warning(sprintf("could not parse: %s\n", test.source));
                NULL
            } else {
                resultsInError <- FALSE
                warningIssued <- FALSE

                result <- tryCatch(eval(test.sexp, envir=.GlobalEnv),
                    error = function(e) { resultsInError <<- TRUE })

                source.string <- paste(deparse(test.source), collapse="")
                if(resultsInError) {
                   # TODO: sprintf("list(%s, FALSE)", source.string)
                } else {
                    if(is.vector(result)) {    # ignore environment results, etc
                      expected <- paste(deparse(result), collapse="")
                      cat(file=test.out, sprintf("test.micro%d <- function() { warning('TEST %d'); stopifnot(identical(%s, %s)) }\n",
                        i, i, test.source, expected))
                    }
                }
            }
        }
    }

    close(test.out)

})()

