library(grDevices)
library(graphics)

plots <- c(tempfile(), tempfile())

# First plot
png(plots[1])
dev.control(displaylist = "enable")
plot(1:12)

# Record the graphics list
rp <- recordPlot()

# Close this plot
dev.off()

# Create a second plot
png(plots[2])
replayPlot(rp)
dev.off()

# Check that both are the same
plot1 <- readBin(plots[1], what="raw")
plot2 <- readBin(plots[2], what="raw")

stopifnot(all(plot1==plot2))

