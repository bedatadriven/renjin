## Utilities for the Harwell-Boeing and MatrixMarket formats

readone <- function(ln, iwd, nper, conv)
{
    ln <- gsub("D", "E", ln)
    inds <- seq(0, by = iwd, length = nper + 1)
    (conv)(substring(ln, 1 + inds[-length(inds)], inds[-1]))
}

readmany <- function(conn, nlines, nvals, fmt, conv)
{
    if (!grep("[[:digit:]]+[DEFGI][[:digit:]]+", fmt))
	stop("Not a valid format")
    Iind <- regexpr('[DEFGI]', fmt)
    nper <- as.integer(substr(fmt, regexpr('[[:digit:]]+[DEFGI]', fmt), Iind - 1))
    iwd <- as.integer(substr(fmt, Iind + 1, regexpr('[\\.\\)]', fmt) - 1))
    rem <- nvals %% nper
    full <- nvals %/% nper
    ans <- vector("list", nvals %/% nper)
    for (i in seq_len(full))
	ans[[i]] <- readone(readLines(conn, 1, ok = FALSE),
			    iwd, nper, conv)
    if (!rem) return(unlist(ans))
    c(unlist(ans),
      readone(readLines(conn, 1, ok = FALSE), iwd, rem, conv))
}

readHB <- function(file)
{
    if (is.character(file))
	file <- if (file == "") stdin() else file(file)
    if (!inherits(file, "connection"))
        stop("'file' must be a character string or connection")
    if (!isOpen(file)) {
        open(file)
        on.exit(close(file))
    }
    hdr <- readLines(file, 4, ok = FALSE)
    ## Title <- sub('[[:space:]]+$', '', substr(hdr[1], 1, 72))
    ## Key   <- sub('[[:space:]]+$', '', substr(hdr[1], 73, 80))
    ## totln <- as.integer(substr(hdr[2], 1, 14))
    ptrln <- as.integer(substr(hdr[2], 15, 28))
    indln <- as.integer(substr(hdr[2], 29, 42))
    valln <- as.integer(substr(hdr[2], 43, 56))
    rhsln <- as.integer(substr(hdr[2], 57, 70))
    if (!(t1 <- substr(hdr[3], 1, 1)) %in% c('C', 'R', 'P'))
	stop(gettextf("Invalid storage type: %s", t1), domain=NA)
    if (t1 != 'R') stop("Only numeric sparse matrices allowed")
    ## _FIXME: Patterns should also be allowed
    if (!(t2 <- substr(hdr[3], 2, 2)) %in% c('H', 'R', 'S', 'U', 'Z'))
	stop(gettextf("Invalid storage format: %s", t2), domain=NA)
    if (!(t3 <- substr(hdr[3], 3, 3)) %in% c('A', 'E'))
	stop(gettextf("Invalid assembled indicator: %s", t3), domain=NA)
    nr <- as.integer(substr(hdr[3], 15, 28))
    nc <- as.integer(substr(hdr[3], 29, 42))
    nz <- as.integer(substr(hdr[3], 43, 56))
    ## nel <- as.integer(substr(hdr[3], 57, 70))
    ptrfmt <- toupper(sub('[[:space:]]+$', '', substr(hdr[4], 1, 16)))
    indfmt <- toupper(sub('[[:space:]]+$', '', substr(hdr[4], 17, 32)))
    valfmt <- toupper(sub('[[:space:]]+$', '', substr(hdr[4], 33, 52)))
    ## rhsfmt <- toupper(sub('[[:space:]]+$', '', substr(hdr[4], 53, 72)))
    if (!is.na(rhsln) && rhsln > 0) readLines(file, 1, ok = FALSE) # h5
    ptr <- readmany(file, ptrln, nc + 1, ptrfmt, as.integer)
    ind <- readmany(file, indln, nz, indfmt, as.integer)
    vals <- readmany(file, valln, nz, valfmt, as.numeric)
    if (t2 == 'S')
        new("dsCMatrix", uplo = "L", p = ptr - 1L,
            i = ind - 1L, x = vals, Dim = c(nr, nc))
    else
        new("dgCMatrix", p = ptr - 1L,
            i = ind - 1L, x = vals, Dim = c(nr, nc))

}

readMM <- function(file)
{
    if (is.character(file))
	file <- if(file == "") stdin() else file(file)
    if (!inherits(file, "connection"))
	stop("'file' must be a character string or connection")
    if (!isOpen(file)) {
	open(file)
	on.exit(close(file))
    }
    scan1 <- function(what, ...)
	scan(file, nmax = 1, what = what, quiet = TRUE, ...)

    if (scan1(character()) != "%%MatrixMarket")# hdr
	stop("file is not a MatrixMarket file")
    if (!(typ <- tolower(scan1(character()))) %in% "matrix")
	stop(gettextf("type '%s' not recognized", typ), domain = NA)
    if (!(repr <- tolower(scan1(character()))) %in% c("coordinate", "array"))
	stop(gettextf("representation '%s' not recognized", repr), domain = NA)
    elt <- tolower(scan1(character()))
    if (!elt %in% c("real", "complex", "integer", "pattern"))
	stop(gettextf("element type '%s' not recognized", elt), domain = NA)

    sym <- tolower(scan1(character()))
    if (!sym %in% c("general", "symmetric", "skew-symmetric", "hermitian"))
	stop(gettextf("symmetry form '%s' not recognized", sym), domain = NA)
    nr <- scan1(integer(), comment.char = "%")
    nc <- scan1(integer())
    nz <- scan1(integer())
    checkIJ <- function(els) {
	if(els$i < 1 || els$i > nr)
	    stop("readMM(): row	 values 'i' are not in 1:nr", call.=FALSE)
	if(els$j < 1 || els$j > nc)
	    stop("readMM(): column values 'j' are not in 1:nc", call.=FALSE)
    }
    if (repr == "coordinate") {
	switch(elt,
	       "real" = ,
	       "integer" = {
		   ## TODO: the "integer" element type should be returned as
		   ##       an object of an "iMatrix" subclass--once there are
		   els <- scan(file, nmax = nz, quiet = TRUE,
			       what= list(i= integer(), j= integer(), x= numeric()))
                   checkIJ(els)
		   switch(sym,
			  "general" = {
			      new("dgTMatrix", Dim = c(nr, nc), i = els$i - 1L,
				  j = els$j - 1L, x = els$x)
			  },
			  "symmetric" = {
			      new("dsTMatrix", uplo = "L", Dim = c(nr, nc),
				  i = els$i - 1L, j = els$j - 1L, x = els$x)
			  },
			  "skew-symmetric" = {
			      stop("symmetry form 'skew-symmetric' not yet implemented for reading")
			      ## FIXME: use dgT... but must expand the (i,j,x) slots!
			      new("dgTMatrix", uplo = "L", Dim = c(nr, nc),
				  i = els$i - 1L, j = els$j - 1L, x = els$x)

			  },
			  "hermitian" = {
			      stop("symmetry form 'hermitian' not yet implemented for reading")
			  },
			  ## otherwise (not possible; just defensive programming):
			  stop(gettextf("symmetry form '%s' is not yet implemented",
					sym), domain = NA)
			  )
	       },
	       "pattern" = {
		   els <- scan(file, nmax = nz, quiet = TRUE,
			       what = list(i = integer(), j = integer()))
		   checkIJ(els)
		   switch(sym,
			  "general" = {
			      new("ngTMatrix", Dim = c(nr, nc),
				  i = els$i - 1L, j = els$j - 1L)
			  },
			  "symmetric" = {
			      new("nsTMatrix", uplo = "L", Dim = c(nr, nc),
				  i = els$i - 1L, j = els$j - 1L)
			  },
			  "skew-symmetric" = {
			      stop("symmetry form 'skew-symmetric' not yet implemented for reading")
			      ## FIXME: use dgT... but must expand the (i,j,x) slots!
			      new("ngTMatrix", uplo = "L", Dim = c(nr, nc),
				  i = els$i - 1L, j = els$j - 1L)

			  },
			  "hermitian" = {
			      stop("symmetry form 'hermitian' not yet implemented for reading")
			  },
			  ## otherwise (not possible; just defensive programming):
			  stop(gettextf("symmetry form '%s' is not yet implemented",
					sym), domain = NA)
			  )
	       },
	       "complex" = {
		   stop("element type 'complex' not yet implemented")
	       },
	       ## otherwise (not possible currently):
	       stop(gettextf("'%s()' is not yet implemented for element type '%s'",
			     "readMM", elt), domain = NA))
    }
    else
	stop(gettextf("'%s()' is not yet implemented for  representation '%s'",
		      "readMM", repr), domain = NA)
}
