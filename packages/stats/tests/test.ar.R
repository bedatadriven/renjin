
library(stats)
library(hamcrest)

lh <- structure(c(2.4, 2.4, 2.4, 2.2, 2.1, 1.5, 2.3, 2.3, 2.5, 2, 1.9,
    1.7, 2.2, 1.8, 3.2, 3.2, 2.7, 2.2, 2.2, 1.9, 1.9, 1.8, 2.7, 3,
    2.3, 2, 2, 2.9, 2.9, 2.7, 2.7, 2.3, 2.6, 2.4, 1.8, 1.7, 1.5,
    1.4, 2.1, 3.3, 3.5, 3.5, 3.1, 2.6, 2.1, 3.4, 3, 2.9), .Tsp = c(1,
    48, 1), class = "ts")


test.ar <- function() {

    fit <- ar(lh)

    assertThat(fit$ar, closeTo(c(0.6534, -0.0636, -0.2269), delta = 0.0001))
}


test.ar.burg <- function() {

    fit <- ar(lh, method = "burg")

    assertThat(fit$ar, closeTo(c(0.65879114, -0.06080726, -0.22337332), delta = 0.0001))
}

test.ar.ols <- function() {

    fit <- ar(lh, method = "ols")

    assertThat(fit$ar, closeTo(c(0.585987), delta = 0.0001))
}


