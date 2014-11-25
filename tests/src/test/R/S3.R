test.factorSubset <- function() {

    f <- factor(c("Yes", "No"))
    f <- f[, drop=TRUE]
}

test.defaultMethodsInPackages <- function() {
    df <- data.frame(x=1:10)
    print(utils::str(df))
}
