
library(stats)
library(hamcrest)


f <- function(x) x*x
fit <- nlminb(9, f)

assertThat(fit$message, identicalTo("both X-convergence and relative convergence (5)"))
assertThat(fit$par, closeTo(0, delta = 0.01))
