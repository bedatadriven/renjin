
# Common functions for generating
# test cases

deparse0 <- function(x) paste(deparse(x), collapse = "")

literal <- function(x) {
  stopifnot(is.character(x))
  class(x) <- "literal"
  x
}

test.open <- function(name) {
  filename <- sprintf("src/test/R/test.%s.R", fn)
  cat(sprintf("Opening test case %s...\n", filename))
  test <- new.env()
  test$fd <- file(filename, open="w")
  test$index <- 1
  class(test) <- "test"
  test
} 

writeln <- function(test, format, ...) {
  writeLines(test$fd, text = sprintf(format, ...))
}

writeTest <- function(test, fn, ..., tol = NULL) {
  call <- as.call(list(as.name(fn), ...))
  
  expected <- tryCatch(eval(call, envir = .GlobalEnv), error = function(e) e)

  if(inherits(expected, "error")) {
    matcher <- "throwsError()"
  } else if(is.null(tol) || !is.double(expected)) {
    matcher <- sprintf("identicalTo(%s)", deparse0(expected))
  } else {
    matcher <- sprintf("identicalTo(%s, tol = %f)", deparse0(expected), tol)
  }
  writeln(test, "test.%s.%d <- function() assertThat(%s, %s)",
                            fn, test$index, deparse0(call), matcher)
  test$index <- test$index + 1
}

close.test <- function(test) {
  close(test$fd)
}

