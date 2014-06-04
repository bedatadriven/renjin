#!/usr/bin/R

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

