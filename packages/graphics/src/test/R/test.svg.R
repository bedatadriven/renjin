library(grDevices)
library(graphics)

# First plot
svg("/tmp/test2.svg")
plot(sin, -pi, 2*pi)

dev.off()