library(grDevices)
library(graphics)

# First plot
svg("/tmp/test.svg")
plot(sin, -pi, 2*pi)

dev.off()