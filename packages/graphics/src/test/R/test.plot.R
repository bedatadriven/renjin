library(grDevices)
library(graphics)

test.plot.default <- function() {
    plot(sin, -pi, 2*pi) # see ?plot.function
}

test.plot.pdf <- function() {
    png("/tmp/test.png")
    plot(sin, -pi, 2*pi)
    dev.off()
}