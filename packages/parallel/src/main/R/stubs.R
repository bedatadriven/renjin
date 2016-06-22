
.reg <-  new.env()
assign("default", NULL, envir = .reg)



checkForRemoteErrors <- function() {
    # NO OP
}

defaultCluster <- function(cl = NULL)
{
    if(is.null(cl)) cl <- get("default", envir = .reg)
    if(is.null(cl)) stop("no cluster 'cl' supplied and none is registered")
    checkCluster(cl)
    cl
}

setDefaultCluster <- function(cl = NULL)
{
    if(!is.null(cl)) checkCluster(cl)
    assign("default", cl, envir = .reg)
}


#
# Checking and subsetting
#

checkCluster <- function(cl)
    if (!inherits(cl, "cluster")) stop("not a valid cluster");

`[.cluster` <- function(cl, ...) {
    v <- NextMethod()
    class(v) <- class(cl)
    v
}


sendCall <- function (con, fun, args, return = TRUE, tag = NULL)
{
    timing <-  .snowTimingData$running()
    if (timing)
        start <- proc.time()[3L]
    postNode(con, "EXEC",
             list(fun = fun, args = args, return = return, tag = tag))
    if (timing)
        .snowTimingData$enterSend(con$rank, start, proc.time()[3L])
    NULL
}

makeCluster <- function (spec, type, ...) 
{
    NULL
}
