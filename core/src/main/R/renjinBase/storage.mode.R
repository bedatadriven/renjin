


`storage.mode<-` <- function(x, value) {
    y <- switch(value,
            logical   = as.logical(x),
            numeric   = as.numeric(x),
            double    = as.double(x),
            integer   = as.integer(x),
            character = as.character(x),
            raw       = as.raw(x),
                 stop(sprintf("invalid or unimplemented storage mode %s", value)))

    attributes(y) <- attributes(x)
    y
}
