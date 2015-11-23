####-----------  Minimal conversion utilities  <-->  "SparseM"

### I.  The  "natural pairs"  between the two packages:

setAs("matrix.csr", "dgRMatrix",
      function(from) {
	  new("dgRMatrix",
	      x = from@ra, j = from@ja - 1L, p = from@ia - 1L,
	      Dim = from@dimension)
      })
setAs("dgRMatrix", "matrix.csr",
      function(from) {
	  new("matrix.csr",
	      ra = from@x, ja = from@j + 1L, ia = from@p + 1L,
	      dimension = from@Dim)
      })


setAs("matrix.csc", "dgCMatrix",
      function(from) {
	  new("dgCMatrix",
	      x = from@ra, i = from@ja - 1L, p = from@ia - 1L,
	      Dim = from@dimension)
      })
setAs("dgCMatrix", "matrix.csc",
      function(from) {
	  new("matrix.csc",
	      ra = from@x, ja = from@i + 1L, ia = from@p + 1L,
	      dimension = from@Dim)
      })

setAs("matrix.coo", "dgTMatrix",
      function(from) {
	  new("dgTMatrix",
	      x = from@ra, i = from@ia - 1L, j = from@ja - 1L,
	      Dim = from@dimension)
      })
setAs("dgTMatrix", "matrix.coo",
      function(from) {
	  new("matrix.coo",
	      ra = from@x, ia = from@i + 1L, ja = from@j + 1L,
	      dimension = from@Dim)
      })

### II.  Enable coercion to the ``favorite'' of each package;
### ---         ----------------------------
###      i.e.,  "dgCMatrix" and  "matrix.csr"

setAs("dsparseMatrix", "matrix.csr",
      function(from) as(as(as(from, "RsparseMatrix"), "dgRMatrix"), "matrix.csr"))

##
setAs("matrix.csr", "dgCMatrix",
      function(from) as(as(from, "dgRMatrix"), "CsparseMatrix"))
setAs("matrix.coo", "dgCMatrix",
      function(from) as(as(from, "dgTMatrix"), "dgCMatrix"))

### also define the virtual coercions that we (should) advertize:
setAs("matrix.csr", "RsparseMatrix", function(from) as(from, "dgRMatrix"))
setAs("matrix.csc", "CsparseMatrix", function(from) as(from, "dgCMatrix"))
setAs("matrix.coo", "TsparseMatrix", function(from) as(from, "dgTMatrix"))
## to "Csparse*" and "Tsparse*" should work for all sparse:
setAs("matrix.csr", "CsparseMatrix",
      function(from) as(as(from, "dgRMatrix"), "CsparseMatrix"))
setAs("matrix.coo", "CsparseMatrix",
      function(from) as(as(from, "dgTMatrix"), "CsparseMatrix"))
setAs("matrix.csc", "TsparseMatrix",
      function(from) as(as(from, "dgCMatrix"), "TsparseMatrix"))
setAs("matrix.csr", "TsparseMatrix",
      function(from) as(as(from, "dgRMatrix"), "TsparseMatrix"))
## Also *from* (our favorite) Csparse should work to all 3 SparseM
setAs("CsparseMatrix", "matrix.csr",
      function(from) as(as(from, "RsparseMatrix"), "matrix.csr"))
setAs("CsparseMatrix", "matrix.coo",
      function(from) as(as(from, "TsparseMatrix"), "matrix.coo"))
setAs("CsparseMatrix", "matrix.csc",
      function(from) as(as(from, "dgCMatrix"), "matrix.csc"))

## Easy coercion: just always use as( <SparseM.mat>, "Matrix") :

setAs("matrix.csr", "Matrix", function(from) as(from, "CsparseMatrix")) # we favor!
setAs("matrix.coo", "Matrix", function(from) as(from, "TsparseMatrix"))
setAs("matrix.csc", "Matrix", function(from) as(from, "CsparseMatrix"))
