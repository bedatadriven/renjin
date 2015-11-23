#### The "mother" of all  Matrix factorizations

## use a "fits all" bail-out method -- eventually this should not happen anymore
setMethod("expand", "MatrixFactorization",
          function(x, ...) .bail.out.1(.Generic, class(x)))

setMethod("show", "MatrixFactorization",
	  function(object) { ## cheap one -- can have better for sub-classes
	      ## cl <- class(object)
	      ## cat(sprintf("'MatrixFactorization' of class \"%s\"\n", cl))
	      cat("'MatrixFactorization' of ")
	      str(object)
	  })
setMethod("show", "BunchKaufman",
	  function(object) {
	      cat("'Bunch-Kaufman' factorization of ")
	      str(object)
	  })
setMethod("show", "pBunchKaufman",
	  function(object) {
	      cat("packed 'Bunch-Kaufman' factorization of ")
	      str(object)
	  })

setMethod("dim", "MatrixFactorization", function(x) x@Dim)

## e.g., for (CHMfactor, <num>):
setMethod("solve", signature(a = "MatrixFactorization", b = "numeric"),
	  function(a, b, ...) callGeneric(a, Matrix(b)))
## catch others, otherwise base::solve is.
setMethod("solve", signature(a = "MatrixFactorization", b = "ANY"),
	  function(a, b, ...) .bail.out.2("solve", class(a), class(b)))
setMethod("solve", signature(a = "MatrixFactorization", b = "missing"),
	  function(a, b, ...) .bail.out.1("solve", class(a)))
