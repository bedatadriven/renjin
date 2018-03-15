

compute <- function() {
    # compute abc intervals for the mean
    x <- rnorm(10)
    theta <- function(p,x) {sum(p*x)/sum(p)}
    results <- abcnon(x, theta)

    # return the confidence limits
    results$limits
}
