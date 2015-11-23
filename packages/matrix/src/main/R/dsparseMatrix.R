### d(ouble)sparseMatrix methods :

setMethod("image", "dsparseMatrix",
	  function(x, ...) image(as(x, "dgTMatrix"), ...))

setMethod("chol", signature(x = "dsparseMatrix"),
	   function(x, pivot=FALSE, cache=TRUE, ...) {
	       nm <- if(pivot) "sPdCholesky" else "spdCholesky"
	       if(!is.null(ch <- x@factors[[nm]]))
		   return(ch) ## use the cache
	       px <- as(x, "symmetricMatrix")
	       if (isTRUE(validObject(px, test=TRUE))) {
		   if(cache)
                       .set.factors(x, nm,
                                    chol(as(px, "CsparseMatrix"), pivot=pivot, ...))
                   else chol(as(px, "CsparseMatrix"), pivot=pivot, ...)
               }
	       else stop("'x' is not positive definite -- chol() undefined.")
	   })

setMethod("determinant", signature(x = "dsparseMatrix", logarithm = "logical"),
          function(x, logarithm = TRUE, ...)
          determinant(as(x,"CsparseMatrix"), logarithm, ...))
##-> now dgC or dsC or dtC .. which *have* their methods

setMethod("lu", signature(x = "dsparseMatrix"),
	  function(x, cache=TRUE, ...)
	  if(cache) .set.factors(x, "lu", lu(as(x, "dgCMatrix"), ...))
	  else lu(as(x, "dgCMatrix"), ...))


setMethod("is.finite", signature(x = "dsparseMatrix"),
	  function(x) {
	      if(any(!is.finite(x@x))) {
                  r <- allTrueMat(x, packed = FALSE)
                  x <- as(as(as(x,"CsparseMatrix"), "dgCMatrix"),"dgTMatrix")
		  notF <- which(!is.finite(x@x))
		  r[cbind(x@i[notF], x@j[notF]) + 1L] <- FALSE
                  r
	      }
              else allTrueMat(x)
	  })
setMethod("is.infinite", signature(x = "dsparseMatrix"),
	  function(x) {
	      if(any((isInf <- is.infinite(x@x)))) {
		  cld <- getClassDef(class(x))
		  if(extends(cld, "triangularMatrix") && x@diag == "U")
		      isInf <- is.infinite((x <- .diagU2N(x, cld))@x)
		  r <- as(x, "lMatrix") # will be "lsparseMatrix" - *has* x slot
		  r@x <- if(length(isInf) == length(r@x)) isInf else is.infinite(r@x)
		  if(!extends(cld, "CsparseMatrix"))
		      r <- as(r, "CsparseMatrix")
		  as(.Call(Csparse_drop, r, 0), "nMatrix") # a 'pattern matrix
	      }
	      else is.na_nsp(x)
	  })

## Group Methods, see ?Arith (e.g.): "Ops" --> ./Ops.R, "Math" in ./Math.R, ...
## -----



