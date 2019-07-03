library(grDevices)
library(graphics)

print(.Devices)

# First plot
svg("/tmp/test2.svg")
plot(sin, -pi, 2*pi)

dev.off()