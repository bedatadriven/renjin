### Define Methods that can be inherited for all subclasses

##-> "dMatrix" <--> "lMatrix"   ---> ./lMatrix.R

## these two are parallel to "n <-> l" in the above :
setAs("nMatrix", "dMatrix",
      function(from) {
	  cld <- getClassDef(cl <- MatrixClass(class(from)))
	  isSp <- extends(cld, "sparseMatrix")
	  ## faster(not "nicer"): any(substr(cl,3,3) == c("C","T","R"))
	  sNams <- slotNames(cld)
	  r <- copyClass(from, sub("^n", "d", cl),
			 if(isSp) sNams else sNams[sNams != "x"])
	  r@x <- if(isSp) rep.int(1., nnzSparse(from)) else as.double(from@x)
	  r
      })

## NOTE: This is *VERY* parallel to  ("lMatrix" -> "nMatrix") in ./lMatrix.R :
setAs("dMatrix", "nMatrix",
      function(from) {
	  if(anyNA(from@x) && ((.w <- isTRUE(getOption("Matrix.warn"))) ||
				   isTRUE(getOption("Matrix.verbose")))) {
	      (if(.w) warning else message)(
		  "\"dMatrix\" object with NAs coerced to \"nMatrix\":  NA |-> TRUE")
	      from@x[is.na(from@x)] <- 1 # "TRUE"
	  }
	  cld <- getClassDef(cl <- MatrixClass(class(from)))
	  if(extends(cld, "diagonalMatrix")) # no "ndi*" class
	      ## should not happen, setAs(diagonalMatrix -> nMatrix) in ./diagMatrix.R:
	      return(di2nMat(from))
	  ## else
	  isSp <- extends(cld, "sparseMatrix")
	  if(isSp && any(from@x == 0)) {
	      from <- drop0(from) # was drop0(from, cld)
	      if(cl != (c. <- class(from)))
		  cld <- getClassDef(cl <- c.)
	  }
	  sNams <- slotNames(cld)
	  r <- copyClass(from, sub("^d", "n", cl), sNams[sNams != "x"])
	  if(!isSp) #  'x' slot |--> logical
	      r@x <- as.logical(from@x)
	  r
      })


## Group Methods:
## -----
## "Math", "Math2" in			--> ./Math.R
## "Summary"				--> ./Summary.R
## "Ops" ("Arith", "Compare", "Logic")	--> ./Ops.R



## Methods for single-argument transformations

setMethod("zapsmall", signature(x = "dMatrix"),
          function(x, digits = getOption("digits")) {
              x@x <- zapsmall(x@x, digits)
              x
          })

## -- end(single-argument transformations) ------


