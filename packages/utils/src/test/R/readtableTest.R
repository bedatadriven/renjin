library(utils)

# Allow the same script to be used to generate the actuals
# For comparison as well as check the results
generate <- FALSE


test.csvWithHeader <- function() {
  test(read.csv("tables/headers.csv"), "tables/headers.csv.rds")
}

test.replacementHeaders <- function() {
  test(read.csv("tables/headers.csv", col.names = c("x", "y", "z")), 
          "tables/headers-col-names.csv.rds")
}

test.txtWithHeader <- function() {
  test(read.table("tables/headers.txt"), "tables/headers.txt.rds")
}

test.blankLines <- function() {
  test(read.csv("tables/blank-lines.csv"), "tables/blank-lines.csv.rds")
}

test.includeBlankLines <- function() {
  test(read.csv("tables/blank-lines.csv", blank.lines.skip = FALSE), "tables/blank-lines-included.csv.rds")
}

test.readFactors <- function() {
  test(read.csv("tables/factors.csv"), "tables/factors.csv.rds")
}

test <- function(actual, expectedFile) {
  expected <- readRDS(file = expectedFile)
  if(!identical(actual, expected)) {
    cat("\n")
    cat("########## ", expectedFile, "\n")
    cat("===========\n")
    cat("EXPECTED\n")
    cat("===========\n")
    print(expected)
    cat("===========\n")
    cat("ACTUAL\n")
    cat("===========\n")
    print(actual)
    stop("Mismatch")
  }
}

generateExpectedTables <- function() {
  # Overwrite the test function to actually save
  #  the expected files as rds files
  test <<- function(actual, expectedFile) {
    saveRDS(actual, expectedFile)
  }
  for(testFn in ls(.GlobalEnv, pattern = "^test\\.")) {
    do.call(testFn, list())
  }
}
