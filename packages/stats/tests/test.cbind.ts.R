library(stats)
library(hamcrest)

x <- ts(1:10, frequency = 4, start = c(1959, 2))
y <- ts(1:10, frequency = 4, start = c(1960, 2))
z <- cbind(x, y)

assertTrue(inherits(z, "ts"))