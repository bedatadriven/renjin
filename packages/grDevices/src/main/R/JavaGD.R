


.geometry <- function(width, height, units, res)
{
    units <- match.arg(units, c("in", "px", "cm", "mm"))
    if(units != "px" && is.na(res))
        stop("'res' must be specified unless 'units = \"px\"'")
    width <- switch(units,
                    "in" = res,
                    "cm" = res/2.54,
                    "mm" = res/25.4,
                    "px" = 1) * width
    height <- switch(units,
                     "in" = res,
                     "cm" = res/2.54,
                     "mm" = res/25.4,
                     "px" = 1) * height
    list(width = width, height = height)
}


png <- function(filename = "Rplot%03d.png",
                width = 480, height = 480, units = "px",
                pointsize = 12, bg = "white", res = NA, ...,
                type = c("cairo", "cairo-png", "Xlib", "quartz"), antialias) {


   if(!checkIntFormat(filename)) {
     stop("invalid 'filename'")
   }
   g <- .geometry(width, height, units, res)
   new <- list(...)
   if(missing(type)) {
        type <- getOption("bitmapType")
    }
    type <- match.arg(type)
    if(!missing(antialias)) {
        new$antialias <- match.arg(antialias, aa.cairo)
    }
    invisible(.Call(C_newJavaGD, filename, width, height, pointsize,
        "org.renjin.grDevices.FileDevice",
        list(filename = filename,
             format = "png")
    ))
}

JavaGD <- function(name="JavaGD", deviceClass = "org.renjin.grDevices.JavaGD", width=400, height=300, ps=12) {
  invisible(.Call(C_newJavaGD, name, width, height, ps, deviceClass, list()))
}

