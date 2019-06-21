
library(grDevices)
library(graphics)

pdf("/tmp/rplot.pdf")
plot(sin, -pi, 2*pi)
dev.off()

