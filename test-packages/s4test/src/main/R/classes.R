
setClass("RGList",
#  Class to hold initial read-in two-color data
representation("list")
)


#MArray <- setClass("MArray",
#   slots = c(marray = "matrix", 
#    fmeta = "data.frame",
#     pmeta = "data.frame"))
#     
#     
#setMethod("show",
#    signature = "MArray",
#    definition = function(object) {
#     cat("An object of class ", class(object), "\n", sep = "")
#     cat(" ", nrow(object@marray), " features by ",
#     ncol(object@marray), " samples.\n", sep = "")
#     invisible(NULL)
#     })