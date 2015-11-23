#### All methods for expm() , the Matrix Exponential

setMethod("expm", signature(x = "dgeMatrix"),
	  function(x) .Call(dgeMatrix_exp, x))

setMethod("expm", signature(x = "Matrix"), function(x) expm(as(x, "dMatrix")))
setMethod("expm", signature(x = "dMatrix"),function(x) expm(as(x, "dgeMatrix")))
## but these trigger first:
expmSpec <- function(x, newClass) {
    r <- copyClass(x, newClass, c("uplo", "Dim", "Dimnames"))
    r@x <- expm(as(as(x, "dMatrix"),"generalMatrix"))@x
    r
}
setMethod("expm", signature(x = "triangularMatrix"),
          function(x) expmSpec(x, "dtrMatrix"))
setMethod("expm", signature(x = "symmetricMatrix"),
          function(x) expmSpec(x, "dsyMatrix"))

setMethod("expm", signature(x = "ddiMatrix"),
	  function(x) {
	      if(x@diag == "U") {
		  x@diag <- "N"
		  x@x <- rep.int(exp(1), x@Dim[1])
	      } else {
		  x@x <- exp(x@x)
	      }
	      x
	  })
## Not necessary (and there's no direct  ldi -> ddi coercion anyway:
## setMethod("expm", signature(x = "ldiMatrix"),
## 	  function(x) expm(as(x,"ddiMatrix")))

## As long as this is not "in R" :
setMethod("expm", signature(x = "matrix"), function(x) expm(Matrix(x)))

