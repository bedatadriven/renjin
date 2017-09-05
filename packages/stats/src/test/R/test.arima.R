library(stats)
library(hamcrest)

lh <- structure(c(2.4, 2.4, 2.4, 2.2, 2.1, 1.5, 2.3, 2.3, 2.5, 2, 1.9,  1.7, 2.2, 1.8, 3.2, 3.2, 2.7, 2.2, 2.2, 1.9,
                1.9, 1.8, 2.7, 3,  2.3, 2, 2, 2.9, 2.9, 2.7, 2.7, 2.3, 2.6, 2.4, 1.8, 1.7, 1.5,  1.4, 2.1, 3.3, 3.5,
                3.5, 3.1, 2.6, 2.1, 3.4, 3, 2.9), .Tsp = c(1,  48, 1), class = "ts")


fit <- arima(lh, order = c(3,0,0), method = "CSS")

assertThat(fit$sigma2, closeTo(0.1904692, 1e-6))



assertThat(arima(lh, order = c(1,0,0))$sigma2, closeTo(0.1975, 1e-3))
assertThat(arima(lh, order = c(3,0,0))$sigma2, closeTo(0.1787, 1e-3))
assertThat(arima(lh, order = c(1,0,1))$sigma2, closeTo(0.1923, 1e-3))
