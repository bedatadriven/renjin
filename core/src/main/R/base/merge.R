#  File src/library/base/R/merge.R
#  Part of the R package, http://www.R-project.org
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  A copy of the GNU General Public License is available at
#  http://www.r-project.org/Licenses/

merge <- function(x, y, ...) UseMethod("merge")

merge.default <- function(x, y, ...)
    merge(as.data.frame(x), as.data.frame(y), ...)

merge.internal.replacement <- function(xinds, yinds, all.x, all.y) {
    indices <- do.call(rbind, lapply(which(xinds %in% yinds[yinds != 0L]), function(i) {
        yi <- which(yinds == xinds[i])
        data.frame(xi = i, yi = yi)
    }))

    xi <- if (is.null(indices)) integer(0) else indices$xi
    yi <- if (is.null(indices)) integer(0) else indices$yi
    x.alone <- if (all.x) which(xinds == 0L) else NULL
    y.alone <- if (all.y) which(yinds == 0L) else NULL

    list(xi = xi, yi = yi, x.alone = x.alone, y.alone = y.alone)
}

merge.data.frame <-
    function(x, y, by = intersect(names(x), names(y)), by.x = by, by.y = by,
             all = FALSE, all.x = all, all.y = all,
             sort = TRUE, suffixes = c(".x",".y"), incomparables = NULL,
             ...)
{
    fix.by <- function(by, df)
    {
        ## fix up 'by' to be a valid set of cols by number: 0 is row.names
        if(is.null(by)) by <- numeric()
        by <- as.vector(by)
        nc <- ncol(df)
        if(is.character(by))
            by <- match(by, c("row.names", names(df))) - 1L
        else if(is.numeric(by)) {
            if(any(by < 0L) || any(by > nc))
                stop("'by' must match numbers of columns")
        } else if(is.logical(by)) {
            if(length(by) != nc) stop("'by' must match number of columns")
            by <- seq_along(by)[by]
        } else stop("'by' must specify column(s) as numbers, names or logical")
        if(any(is.na(by))) stop("'by' must specify valid column(s)")
        unique(by)
    }

    nx <- nrow(x <- as.data.frame(x))
    ny <- nrow(y <- as.data.frame(y))

    by.x <- fix.by(by.x, x)
    by.y <- fix.by(by.y, y)

    if((l.b <- length(by.x)) != length(by.y)) {
        stop("'by.x' and 'by.y' specify different numbers of columns")
    }

    if(l.b == 0L) {
        ## was: stop("no columns to match on")
        ## return the cartesian product of x and y, fixing up common names
        nm <- nm.x <- names(x)
        nm.y <- names(y)
        has.common.nms <- any(cnm <- nm.x %in% nm.y)
        if(has.common.nms) {
            names(x)[cnm] <- paste(nm.x[cnm], suffixes[1L], sep="")
            cnm <- nm.y %in% nm
            names(y)[cnm] <- paste(nm.y[cnm], suffixes[2L], sep="")
        }
        if (nx == 0L || ny == 0L) {
            res <- cbind(x[FALSE, ], y[FALSE, ])
        } else {
            ij <- expand.grid(seq_len(nx), seq_len(ny))
            res <- cbind(x[ij[, 1L], , drop = FALSE], y[ij[, 2L], , drop = FALSE])
        }
    }
    else {
        fastres <- .Internal(merge(x, y, by.x, by.y, all.x, all.y, sort))
        if(!is.null(fastres)) {
            return(fastres);
        }

        if(any(by.x == 0L)) {
            x <- cbind(Row.names = I(row.names(x)), x)
            by.x <- by.x + 1L
        }
        if(any(by.y == 0L)) {
            y <- cbind(Row.names = I(row.names(y)), y)
            by.y <- by.y + 1L
        }
        row.names(x) <- NULL
        row.names(y) <- NULL
        ## create keys from 'by' columns:
        if(l.b == 1L) {                  # (be faster)
            bx <- x[, by.x]; if(is.factor(bx)) bx <- as.character(bx)
            by <- y[, by.y]; if(is.factor(by)) by <- as.character(by)
        } else {
            ## Do these together for consistency in as.character.
            ## Use same set of names.
            bx <- x[, by.x, drop=FALSE]; by <- y[, by.y, drop=FALSE]
            names(bx) <- names(by) <- paste("V", seq_len(ncol(bx)), sep="")
            bz <- do.call("paste", c(rbind(bx, by), sep = "\r"))
            bx <- bz[seq_len(nx)]
            by <- bz[nx + seq_len(ny)]
        }
        comm <- match(bx, by, 0L)
        bxy <- bx[comm > 0L]             # the keys which are in both
        xinds <- match(bx, bxy, 0L, incomparables)
        yinds <- match(by, bxy, 0L, incomparables)
        if(nx > 0L && ny > 0L) {
            #m <- .Internal(merge(xinds, yinds, all.x, all.y))
            # pure R replacement for internal merge function:
            m <- merge.internal.replacement(xinds, yinds, all.x, all.y)
            if (all.x) m$x.alone <- which(xinds == 0L)
            if (all.y) m$y.alone <- which(yinds == 0L)
        }
        else
            m <- list(xi = integer(), yi = integer(),
                      x.alone = seq_len(nx), y.alone = seq_len(ny))
        nm <- nm.x <- names(x)[-by.x]
        nm.by <- names(x)[by.x]
        nm.y <- names(y)[-by.y]
        ncx <- ncol(x)
        if(all.x) all.x <- (nxx <- length(m$x.alone)) > 0L
        if(all.y) all.y <- (nyy <- length(m$y.alone)) > 0L
        lxy <- length(m$xi)             # == length(m$yi)
        ## x = [ by | x ] :
        has.common.nms <- any(cnm <- nm.x %in% nm.y)
        if(has.common.nms)
            nm.x[cnm] <- paste(nm.x[cnm], suffixes[1L], sep="")
        x <- x[c(m$xi, if(all.x) m$x.alone),
               c(by.x, seq_len(ncx)[-by.x]), drop=FALSE]
        names(x) <- c(nm.by, nm.x)
        if(all.y) { ## add the 'y.alone' rows to x[]
            ## need to have factor levels extended as well -> using [cr]bind
            ya <- y[m$y.alone, by.y, drop=FALSE]
            names(ya) <- nm.by
            ## this used to use a logical matrix, but that is not good
            ## enough as x could be zero-row.
            ya <- cbind(ya, x[rep.int(NA_integer_, nyy), nm.x, drop=FALSE ])
            x <- rbind(x, ya)
            #x <- rbind(x, cbind(ya, matrix(NA, nyy, ncx-l.b,
            #                               dimnames=list(NULL,nm.x))))
        }
        ## y (w/o 'by'):
        if(has.common.nms) {
            cnm <- nm.y %in% nm
            nm.y[cnm] <- paste(nm.y[cnm], suffixes[2L], sep="")
        }
        y <- y[c(m$yi, if(all.x) rep.int(1L, nxx), if(all.y) m$y.alone),
               -by.y, drop = FALSE]
        if(all.x)
            for(i in seq_along(y))
                ## do it this way to invoke methods for e.g. factor
                is.na(y[[i]]) <- (lxy+1L):(lxy+nxx)

        if(has.common.nms) names(y) <- nm.y
        res <- cbind(x, y)

        if (sort)
            res <- res[if(all.x || all.y) ## does NOT work
                       do.call("order", x[, seq_len(l.b), drop=FALSE])
            else sort.list(bx[m$xi]),, drop=FALSE]
    }
    ## avoid a copy
    ## row.names(res) <- NULL
    attr(res, "row.names") <- .set_row_names(nrow(res))
    res
}




.sort.then.merge <- function(x, y, by.x, by.y, all.x, all.y) {

  # Create a set of index vectors that will be used to order the two
    # data.frames

    key.vector <- function(i, x) {
      if(i == 0) {
        row.names(x)
      } else {
        k <- x[[i]]
        if(is.factor(k)) {
          k <- as.character(k)
        }
        k
      }
    }

    # Create a list of key vectors for x and y
    nk <- length(by.x)
    kx <- lapply(by.x, key.vector, x)
    ky <- lapply(by.y, key.vector, y)

    # Order these key vectors so that we can walk the
    # rows in order
    kxi <- do.call(order, kx)
    kyi <- do.call(order, ky)


    # Allocate an array to hold the indices in the
    # original table for each row in the result table
    resix <- rep(NA_integer_, length.out = nrow(x) + nrow(y))
    resiy <- rep(NA_integer_, length.out = nrow(x) + nrow(y))

    # If we keep all.y, then we need to keep track of which
    # rows come from the right
    right <- rep(FALSE, length.out = nrow(x) + nrow(y))

    # Now both sets of keys are sorted, so we can walk
    # the rows in order

    rx <- 1L
    ry <- 1L
    ro <- 1L

    while(rx <= nrow(x) && ry <= nrow(y)) {

      # Compare the keys at the current position
      cmp <- 0L
      for(k in 1:nk) {
        kxv <- kx[[k]][kxi[rx]]
        kyv <- ky[[k]][kyi[ry]]
        if(kxv < kyv) {
          cmp <- -1L
          break;
        } else if(kxv > kyv) {
          cmp <- 1L
          break;
        }
      }

      if(cmp < 0L) {

        # Keys on the left are earlier in the ordering
        # that those on the right.

        # If all.x, then emit the left index, but the
        # right will stay NA

        if(all.x) {
          resix[ro] <- kxi[rx]
          ro <- ro + 1L
        }
        rx <- rx + 1L

      } else if(cmp > 0L) {

        # Keys are on the right are earlier in the ordering
        # than those on the left

        # if all.y, then emit the right index, but the
        # left will stay NA

        if(all.y) {
          resix[ro] <- kyi[ry]
          right[ro] <- TRUE
          ro <- ro + 1L
        }
        ry <- ry + 1L

      } else {

        # Keys at this point are equal,

        resix[ro] <- kxi[rx]
        resiy[ro] <- kyi[ry]
        rx <- rx + 1L
        ry <- ry + 1L
        ro <- ro + 1L
      }
    }

    # If there any all x keys left, add them to the output
    if(all.x) {
      while(rx <= nrow(x)) {
        resix[ro] <- kxi[rx]
        ro <- ro + 1L
        rx <- rx + 1L
      }
    }
    if(all.y) {
      while(ry <= nrow(y)) {
        resiy[ro] <- kyi[ry]
        right[ro] <- TRUE
        ro <- ro + 1L
        ry <- ry + 1L
      }
    }

    nout <- ro - 1

    stop("TODO")

}
