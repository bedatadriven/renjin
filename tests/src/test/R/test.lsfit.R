
library(hamcrest)
library(stats)

test.issue260 <- function() {
    b <- matrix(c(2,4,3,1,5,7),nrow=3)
    d <- c(7,4,2)
    m <- lsfit(b,d)

    print(m)

    assertThat(m$coefficients, identicalTo(c(Intercept=7.375, X1=0.250, X2=-0.875), tol = 1e-6))
}