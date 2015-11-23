## Now in ./Tsparse.R
## setAs("dgTMatrix", "dgCMatrix",
##       function(from) .Call(Tsparse_to_Csparse, from, FALSE)
##       )

setAs("dgTMatrix", "dgeMatrix",
      function(from) .Call(dgTMatrix_to_dgeMatrix, from))

setAs("dgTMatrix", "matrix",
      function(from) .Call(dgTMatrix_to_matrix, from))


setAs("dgeMatrix", "dgTMatrix",
      function(from) as(as(from, "dgCMatrix"), "dgTMatrix"))

if(FALSE) ## special case, relatively ugly, needed ??
setAs("dgTMatrix", "dsCMatrix",
      function(from) {
          if (!isSymmetric(from))
	      stop("cannot coerce non-symmetric \"dgTMatrix\" to \"dsCMatrix\" class")
          upper <- from@i <= from@j
          uC <- as(new("dgTMatrix", Dim = from@Dim,  Dimnames = from@Dimnames,
                       i = from@i[upper],
                       j = from@j[upper], x = from@x[upper]), "dgCMatrix")
          new("dsCMatrix", Dim = uC@Dim, p = uC@p, i = uC@i, x = uC@x, uplo = "U")
      })

## This is faster:
setAs("dgTMatrix", "dtCMatrix",
      function(from) {
	  if(!(iTri <- isTriangular(from)))
	      stop("the matrix is not triangular")
	  ## else
	  stopifnot(is.character(uplo <- attr(iTri, "kind")))
	  .Call(Tsparse_to_tCsparse, from, uplo, "N")
      })

setAs("dgTMatrix", "dtTMatrix",
      function(from) check.gT2tT(from, toClass = "dtTMatrix", do.n=FALSE))
setAs("dgTMatrix", "triangularMatrix",
      function(from) check.gT2tT(from, toClass = "dtTMatrix", do.n=FALSE))

setAs("dgTMatrix", "dsTMatrix",
      function(from) check.gT2sT(from, toClass = "dsTMatrix", do.n=FALSE))
setAs("dgTMatrix", "symmetricMatrix",
      function(from) check.gT2sT(from, toClass = "dsTMatrix", do.n=FALSE))

mat2dgT <- function(from) {
    x <- as.double(from)
    nz <- isN0(x)
    new("dgTMatrix", Dim = dim(from),
        i = row(from)[nz] - 1L,
        j = col(from)[nz] - 1L,
        x = x[nz])
}

setAs("matrix", "dgTMatrix", mat2dgT)


## "[" methods are now in ./Tsparse.R

## "[<-" methods { setReplaceMethod()s }  too ...


setMethod("image", "dgTMatrix", ## *The* real one
          function(x,
		   xlim = c(1, di[2]),
		   ylim = c(di[1], 1), aspect = "iso",
                   sub = sprintf("Dimensions: %d x %d", di[1], di[2]),
		   xlab = "Column", ylab = "Row", cuts = 15,
		   useRaster = FALSE,
                   useAbs = NULL, colorkey = !useAbs, col.regions = NULL,
                   lwd = NULL, ...)
      {
          ## 'at' can remain missing and be passed to levelplot
          di <- x@Dim
          xx <- x@x
          if(missing(useAbs)) ## use abs() when all values are non-neg
              useAbs <- min(xx, na.rm=TRUE) >= 0
          else if(useAbs)
              xx <- abs(xx)
          rx <- range(xx, finite=TRUE)
	  ## FIXME: make use of 'cuts' now
	  ##	    and call levelplot() with 'at = ', making sure  0 is included and matching
	  ##	    *exactly* - rather than approximately
          if(is.null(col.regions))
              col.regions <-
                  if(useAbs) {
                      grey(seq(from = 0.7, to = 0, length = 100))
                  } else { ## no abs(.), rx[1] < 0
                      nn <- 100
                      n0 <- min(nn, max(0, round((0 - rx[1])/(rx[2]-rx[1]) * nn)))
                      col.regions <-
                          c(colorRampPalette(c("blue3", "gray80"))(n0),
                            colorRampPalette(c("gray75","red3"))(nn - n0))
                  }
          if(!is.null(lwd) && !(is.numeric(lwd) && all(lwd >= 0))) # allow lwd=0
              stop("'lwd' must be NULL or non-negative numeric")
          stopifnot(length(xlim) == 2, length(ylim) == 2)
	  ## ylim: the rows count from top to bottom:
	  ylim <- sort(ylim, decreasing=TRUE)
	  if(all(xlim == round(xlim))) xlim <- xlim+ c(-.5, +.5)
	  if(all(ylim == round(ylim))) ylim <- ylim+ c(+.5, -.5) # decreasing!
          levelplot(x@x ~ (x@j + 1L) * (x@i + 1L),
                    sub = sub, xlab = xlab, ylab = ylab,
                    xlim = xlim, ylim = ylim, aspect = aspect,
		    colorkey = colorkey, col.regions = col.regions, cuts = cuts,
		    par.settings = list(background = list(col = "transparent")),
		    panel = if(useRaster) panel.levelplot.raster else
		    function(x, y, z, subscripts, at, ..., col.regions)
		{   ## a trimmed down version of  lattice::panel.levelplot
                    x <- as.numeric(x[subscripts])
                    y <- as.numeric(y[subscripts])

                    ## FIXME: use  level.colors() here and 'at' from above --
                    ## -----  look at 'zcol' in  panel.levelplot()
                    numcol <- length(at) - 1
                    num.r <- length(col.regions)
		    col.regions <-
			if (num.r <= numcol)
			    rep_len(col.regions, numcol)
			else col.regions[1+ ((1:numcol-1)*(num.r-1)) %/% (numcol-1)]
                    zcol <- rep.int(NA_integer_, length(z))
		    for (i in seq_along(col.regions))
                        zcol[!is.na(x) & !is.na(y) & !is.na(z) &
                             at[i] <= z & z < at[i+1]] <- i
                    zcol <- zcol[subscripts]

                    if (any(subscripts)) {
                        ## the line-width used in grid.rect() inside
                        ## levelplot()'s panel for the *border* of the
                        ## rectangles: levelplot()panel has lwd=1e-5:

                        ## Here: use smart default !

                        if(is.null(lwd)) {
                            wh <- grid::current.viewport()[c("width", "height")]
                            ## wh : current viewport dimension in pixel
                            wh <- c(grid::convertWidth(wh$width, "inches",
                                                       valueOnly=TRUE),
                                    grid::convertHeight(wh$height, "inches",
                                                        valueOnly=TRUE)) *
                                                            par("cra") / par("cin")
                            pSize <- wh/di ## size of one matrix-entry in pixels
                            pA <- prod(pSize) # the "area"
                            p1 <- min(pSize)
                            lwd <- ## crude for now
                                if(p1 < 2 || pA < 6) 0.01 # effectively 0
                                else if(p1 >= 4) 1
                                else if(p1 > 3) 0.5 else 0.2
                            ## browser()
			    Matrix.msg("rectangle size ",
				       paste(round(pSize,1), collapse=" x "),
				       " [pixels];  --> lwd :", formatC(lwd))
                        } else stopifnot(is.numeric(lwd), all(lwd >= 0)) # allow 0

                        grid.rect(x = x, y = y, width = 1, height = 1,
                                  default.units = "native",
                                  gp = gpar(fill = col.regions[zcol],
                                  lwd = lwd, col = if(lwd < .01) NA))
                    }
                }, ...)
      })

## Uses the triplet convention of *adding* entries with same (i,j):
setMethod("+", signature(e1 = "dgTMatrix", e2 = "dgTMatrix"),
          function(e1, e2) {
              dimCheck(e1, e2)
              new("dgTMatrix", i = c(e1@i, e2@i), j = c(e1@j, e2@j),
                  x = c(e1@x, e2@x), Dim = e1@Dim)
          })


## setMethod("writeHB", signature(obj = "dgTMatrix"),
## 	  function(obj, file, ...) callGeneric(as(obj, "CsparseMatrix"), file, ...))
