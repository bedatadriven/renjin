#!/usr/bin/R

# This script takes the micro test list as input, and generates
# a set of expected values using GNU R.

# Evaluate each test expression and store the result as an
# expectation for future runs

# Run in an anonymous closure because we clear the global
# environment before each test
go <- function() {

    test.out <- file("src/test/java/org/renjin/MicroTest.java", "wt")
    cat(file=test.out, "// Auto-generated from micro-tests.in\n")
    cat(file=test.out, "package org.renjin;\n");
    cat(file=test.out, "import org.junit.*;\n");

    cat(file=test.out, "public class MicroTest extends AbstractMicroTest {\n");

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
                      cat(file=test.out,         "  @Test\n")
                      cat(file=test.out, sprintf("  public void micro%d() {\n", i));

                      sdeparse <- function(x) paste(deparse(x), collapse="")

                      cat(file=test.out, sprintf('    assertIdentical(%s, %s);\n',
                            sdeparse(test.source), sdeparse(sdeparse(result))))
                      cat(file=test.out, "  }\n");
                    }
                }
            }
        }
    }

    close(test.out)

}
go()

